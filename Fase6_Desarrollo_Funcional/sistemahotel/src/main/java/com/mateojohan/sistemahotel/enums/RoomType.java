package com.mateojohan.sistemahotel.enums;

/**
 * Tipo de habitación (HU-01). Coincide con rooms.type, que es la columna discriminadora
 * de la herencia Room -> SingleRoom / DoubleRoom / SuiteRoom.
 */
public enum RoomType {
    SINGLE,
    DOUBLE,
    SUITE
}
