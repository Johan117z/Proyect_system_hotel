package com.mycompany.system_hotel;

/**
 * Ciclo de vida de una reserva (RF-09 a RF-10). Coincide con reservations.status en la BD.
 *
 * PENDING --checkIn--> ACTIVE --checkOut--> COMPLETED
 * PENDING --cancel---> CANCELLED
 */
public enum ReservationStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    CANCELLED;

    /** Una reserva en estos estados bloquea la habitación para sus fechas (RN-05). */
    public boolean blocksRoom() {
        return this == PENDING || this == ACTIVE;
    }
}
