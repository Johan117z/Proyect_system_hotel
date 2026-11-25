package com.mateojohan.sistemahotel.enums;

/**
 * Estado operativo de una habitación. Coincide con rooms.status en la BD.
 * MAINTENANCE = "Mantenimiento/Limpieza", estado posterior al check-out (RN-10).
 */
public enum RoomStatus {
    AVAILABLE,
    OCCUPIED,
    MAINTENANCE
}
