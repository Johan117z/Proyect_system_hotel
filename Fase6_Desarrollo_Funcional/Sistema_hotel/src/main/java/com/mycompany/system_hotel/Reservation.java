package com.mycompany.system_hotel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Reserva de una habitación (FR-07 a FR-10). Controla sus propias transiciones de estado.
 */
public class Reservation {

    private final String reservationCode;   // Formato RES-AAAA-NNN (reservations.reservation_code)
    private final IGuest guest;
    private final IRoom room;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
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
        this.status = ReservationStatus.PENDING;   // FR-07: estado inicial
    }

    /**
     * BR-01: dos rangos [entrada, salida) se cruzan si cada uno empieza antes de que el otro termine.
     * La salida es exclusiva: un huésped puede entrar el mismo día en que otro sale.
     */
    public boolean overlaps(LocalDate start, LocalDate end) {
        return status.blocksRoom() && start.isBefore(checkOutDate) && end.isAfter(checkInDate);
    }

    public long getStayDays() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /** FR-08 / BR-02: solo una reserva PENDING puede hacer check-in. */
    public void checkIn() {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessRuleException("BR-02",
                    "Solo se puede hacer check-in de reservas pendientes. Estado actual: " + status + ".");
        }
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new BusinessRuleException("BR-02",
                    "La habitación " + room.getNumber() + " no está disponible (" + room.getStatus() + ").");
        }
        status = ReservationStatus.ACTIVE;
        room.changeStatus(RoomStatus.OCCUPIED);
    }

    /** FR-09: ACTIVE -> COMPLETED y la habitación vuelve a AVAILABLE. */
    public void checkOut() {
        if (status != ReservationStatus.ACTIVE) {
            throw new BusinessRuleException("FR-09",
                    "Solo se puede hacer check-out de reservas activas. Estado actual: " + status + ".");
        }
        status = ReservationStatus.COMPLETED;
        room.changeStatus(RoomStatus.AVAILABLE);
    }

    /** FR-10: solo se cancelan reservas pendientes. La habitación no cambia porque aún no estaba ocupada. */
    public void cancel() {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessRuleException("FR-10",
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
