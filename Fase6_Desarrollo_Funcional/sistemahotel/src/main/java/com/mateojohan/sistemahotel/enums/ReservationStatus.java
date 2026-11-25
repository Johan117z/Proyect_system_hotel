package com.mateojohan.sistemahotel.enums;

import java.util.List;

/**
 * Ciclo de vida de una reserva (RF-09 a RF-13). Coincide con reservations.status en la BD.
 *
 * PENDING --checkIn--> ACTIVE --checkOut--> COMPLETED
 * PENDING --cancel---> CANCELLED
 */
public enum ReservationStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    CANCELLED;

    /** Estados que bloquean la habitación para sus fechas (RN-05, RN-07). */
    public static final List<ReservationStatus> BLOCKING = List.of(PENDING, ACTIVE);

    public boolean blocksRoom() {
        return BLOCKING.contains(this);
    }
}
