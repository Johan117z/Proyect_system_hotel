package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Reserva de una habitación (HU-09 a HU-13). Controla sus propias transiciones de estado
 * y los servicios adicionales consumidos durante la estadía.
 */
public class Reservation {

    private final String reservationCode;   // Formato RES-AAAA-NNN (reservations.reservation_code)
    private final IGuest guest;
    private final IRoom room;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final List<AdditionalService> services = new ArrayList<>();
    private ReservationStatus status;

    public Reservation(String reservationCode, IGuest guest, IRoom room,
                       LocalDate checkInDate, LocalDate checkOutDate) {
        if (reservationCode == null || reservationCode.isBlank()) {
            throw new IllegalArgumentException("El código de la reserva es obligatorio.");
        }
        if (guest == null) {
            throw new IllegalArgumentException("El huésped es obligatorio.");
        }
        if (room == null) {
            throw new IllegalArgumentException("La habitación es obligatoria.");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida son obligatorias.");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada.");
        }
        this.reservationCode = reservationCode;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = ReservationStatus.PENDING;   // RF-09: estado inicial
    }

    /**
     * RN-05: dos rangos [entrada, salida) se cruzan si cada uno empieza antes de que el otro termine.
     * La salida es exclusiva: un huésped puede entrar el mismo día en que otro sale.
     */
    public boolean overlaps(LocalDate start, LocalDate end) {
        return status.blocksRoom() && start.isBefore(checkOutDate) && end.isAfter(checkInDate);
    }

    public long getStayDays() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /**
     * RF-12 / RN-08: solo una reserva PENDING puede hacer check-in, y solo dentro de su periodo:
     * desde la fecha de entrada hasta la noche anterior a la salida (US-04: reserva para hoy).
     */
    public void checkIn(LocalDate today) {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessRuleException("RN-08",
                    "Solo se puede hacer check-in de reservas pendientes. Estado actual: " + status + ".");
        }
        if (today.isBefore(checkInDate)) {
            throw new BusinessRuleException("RN-08",
                    "El check-in de esta reserva es a partir del " + ConsoleUtils.formatDate(checkInDate) + ".");
        }
        if (!today.isBefore(checkOutDate)) {
            throw new BusinessRuleException("RN-08",
                    "El periodo de la reserva terminó el " + ConsoleUtils.formatDate(checkOutDate) + ".");
        }
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new BusinessRuleException("RN-08",
                    "La habitación " + room.getNumber() + " no está disponible (" + room.getStatus() + ").");
        }
        status = ReservationStatus.ACTIVE;
        room.changeStatus(RoomStatus.OCCUPIED);
    }

    /**
     * HU-13 / RN-10: ACTIVE -> COMPLETED y la habitación pasa a MAINTENANCE (Mantenimiento/Limpieza).
     * Que la cuenta esté saldada (RN-09) lo verifica Hotel.checkOut antes de llamar este método.
     */
    public void checkOut() {
        if (status != ReservationStatus.ACTIVE) {
            throw new BusinessRuleException("RF-13",
                    "Solo se puede hacer check-out de reservas activas. Estado actual: " + status + ".");
        }
        status = ReservationStatus.COMPLETED;
        room.changeStatus(RoomStatus.MAINTENANCE);
    }

    /** Registra un servicio consumido. Solo durante la estadía (reserva ACTIVE). */
    public void addService(AdditionalService service) {
        if (service == null) {
            throw new IllegalArgumentException("El servicio es obligatorio.");
        }
        if (status != ReservationStatus.ACTIVE) {
            throw new BusinessRuleException("RF-14",
                    "Solo se registran servicios durante la estadía (reserva activa). Estado actual: " + status + ".");
        }
        services.add(service);
    }

    public List<AdditionalService> getServices() {
        return List.copyOf(services);
    }

    public BigDecimal getServicesTotal() {
        return services.stream().map(AdditionalService::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** RF-10: solo se cancelan reservas pendientes. La habitación no cambia porque aún no estaba ocupada. */
    public void cancel() {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessRuleException("RF-10",
                    "Solo se pueden cancelar reservas pendientes. Estado actual: " + status + ".");
        }
        status = ReservationStatus.CANCELLED;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public IGuest getGuest() {
        return guest;
    }

    public IRoom getRoom() {
        return room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return reservationCode + " | " + guest.getFullName() + " | Hab. " + room.getNumber()
                + " | " + ConsoleUtils.formatDate(checkInDate) + " -> " + ConsoleUtils.formatDate(checkOutDate)
                + " | " + status;
    }
}
