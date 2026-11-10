package com.mycompany.system_hotel;

/**
 * Tipos de habitación (FR-01). Coincide con rooms.type en la BD.
 */
public enum RoomType {
    SINGLE,
    DOUBLE,
    SUITE;

    /** Convierte texto ingresado por el usuario (ignora mayúsculas y espacios). */
    public static RoomType fromText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("El tipo de habitación es obligatorio.");
        }
        try {
            return RoomType.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de habitación inválido. Use SINGLE, DOUBLE o SUITE.");
        }
    }
}
