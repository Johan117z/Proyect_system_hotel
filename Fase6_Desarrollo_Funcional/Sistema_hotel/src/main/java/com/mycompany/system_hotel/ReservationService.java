package com.mycompany.system_hotel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Épicas 3 y 4 — Reservas y estadía (HU-09 a HU-13): crear, cancelar y consultar reservas y disponibilidad,
 * check-in y check-out.
 */
public class ReservationService {

    private final HotelStore store;

    public ReservationService(HotelStore store) {
        this.store = store;
    }

    /** HU-09 / RN-05 (sin cruce de fechas) y RN-13 (no reservar en mantenimiento ni en el pasado). */
    public Reservation create(String guestDocument, int roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        Guest guest = store.findGuest(guestDocument);
        IRoom room = store.findRoom(roomNumber);

        validateRange(checkInDate, checkOutDate);
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("RN-13", "La fecha de entrada no puede ser anterior a hoy.");
        }
        if (!room.isBookable()) {
            throw new BusinessRuleException("RN-13", "La habitación " + roomNumber + " está en mantenimiento.");
        }
        if (isBookedBetween(room, checkInDate, checkOutDate)) {
            throw new BusinessRuleException("RN-05", "La habitación no está disponible en el rango de fechas seleccionado.");
        }

        // Código RES-AAAA-NNN; el consecutivo solo avanza si la reserva se guarda
        String code = String.format("RES-%d-%03d", checkInDate.getYear(), store.nextReservationNumber());
        Reservation reservation = new Reservation(code, guest, room, checkInDate, checkOutDate);
        store.addReservation(reservation);
        return reservation;
    }

    /**
     * HU-11 / RN-07: habitaciones sin cruce de fechas con reservas pendientes o activas en el rango.
     * Se excluyen las que están en mantenimiento, porque no se pueden reservar (RN-13).
     */
    public List<IRoom> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        validateRange(checkInDate, checkOutDate);
        return store.getActiveRooms().stream()
                .filter(IRoom::isBookable)
                .filter(r -> !isBookedBetween(r, checkInDate, checkOutDate))
                .toList();
    }

    public Reservation find(String reservationCode) {
        return store.findReservation(reservationCode);
    }

    public List<Reservation> getAll() {
        return store.getReservations();
    }

    /** HU-10 / RN-06: cancela una reserva pendiente y libera sus fechas. */
    public Reservation cancel(String reservationCode) {
        Reservation reservation = store.findReservation(reservationCode);
        reservation.cancel();
        return reservation;
    }

    /** HU-12 / RN-08: la reserva pasa a ACTIVE y la habitación a OCCUPIED. */
    public Reservation checkIn(String reservationCode) {
        Reservation reservation = store.findReservation(reservationCode);
        reservation.checkIn(LocalDate.now());
        return reservation;
    }

    /**
     * HU-13 / RN-09 y RN-10: solo con la cuenta saldada (factura emitida y pagada).
     * La reserva pasa a COMPLETED y la habitación a MAINTENANCE (Mantenimiento/Limpieza).
     */
    public Reservation checkOut(String reservationCode) {
        Reservation reservation = store.findReservation(reservationCode);
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessRuleException("RF-13",
                    "Solo se puede hacer check-out de reservas activas. Estado actual: " + reservation.getStatus() + ".");
        }
        Optional<Invoice> invoice = store.findInvoiceFor(reservation);
        if (invoice.isEmpty()) {
            throw new BusinessRuleException("RN-09",
                    "La cuenta tiene saldo pendiente: genere la factura y registre el pago antes del check-out.");
        }
        if (!store.isPaid(invoice.get())) {
            throw new BusinessRuleException("RN-09",
                    "La cuenta tiene un saldo pendiente de " + ConsoleUtils.formatCop(invoice.get().getTotalAmount())
                            + ". Registre el pago antes del check-out.");
        }
        reservation.checkOut();
        return reservation;
    }

    private boolean isBookedBetween(IRoom room, LocalDate checkInDate, LocalDate checkOutDate) {
        return store.getReservations().stream()
                .anyMatch(r -> r.getRoom() == room && r.overlaps(checkInDate, checkOutDate));
    }

    private static void validateRange(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida son obligatorias.");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada.");
        }
    }
}
