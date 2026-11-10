package com.mycompany.system_hotel;

import java.math.BigDecimal;

/**
 * Crea la subclase correcta según el tipo. Evita que la UI conozca las clases concretas
 * (Principio Abierto/Cerrado: un tipo nuevo solo toca esta fábrica y su subclase).
 */
public final class RoomFactory {

    private RoomFactory() {
    }

    public static Room create(RoomType type, int number, int capacity,
                              BigDecimal pricePerNight, BigDecimal extraServiceFee) {
        if (type == null) {
            throw new IllegalArgumentException("El tipo de habitación es obligatorio.");
        }
        return switch (type) {
            case SINGLE -> new SingleRoom(number, capacity, pricePerNight);
            case DOUBLE -> new DoubleRoom(number, capacity, pricePerNight);
            case SUITE -> new SuiteRoom(number, capacity, pricePerNight,
                    extraServiceFee == null ? BigDecimal.ZERO : extraServiceFee);
        };
    }
}
