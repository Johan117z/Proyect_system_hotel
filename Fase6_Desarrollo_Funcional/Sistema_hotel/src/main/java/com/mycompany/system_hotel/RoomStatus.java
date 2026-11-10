package com.mycompany.system_hotel;

/**
 * Estado operativo de una habitación. Coincide con rooms.status en la BD.
 */
public enum RoomStatus {
    AVAILABLE,
    OCCUPIED,
    MAINTENANCE;

    public static RoomStatus fromText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("El estado de la habitación es obligatorio.");
        }
        try {
            return RoomStatus.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido. Use AVAILABLE, OCCUPIED o MAINTENANCE.");
        }
    }
}
