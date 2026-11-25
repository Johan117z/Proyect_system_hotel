package com.mateojohan.sistemahotel.models;

import java.math.BigDecimal;

import com.mateojohan.sistemahotel.enums.RoomType;

/** Patrón Factory: crea la subclase de Room que corresponde al tipo recibido. */
public final class RoomFactory {

    private RoomFactory() {
    }

    public static Room create(RoomType type, Integer roomNumber, Integer capacity,
                              BigDecimal pricePerNight, BigDecimal extraServiceFee) {
        return switch (type) {
            case SINGLE -> new SingleRoom(roomNumber, capacity, pricePerNight);
            case DOUBLE -> new DoubleRoom(roomNumber, capacity, pricePerNight);
            case SUITE -> new SuiteRoom(roomNumber, capacity, pricePerNight, extraServiceFee);
        };
    }
}
