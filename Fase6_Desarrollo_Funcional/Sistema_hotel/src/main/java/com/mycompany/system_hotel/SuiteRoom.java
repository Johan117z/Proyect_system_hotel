package com.mycompany.system_hotel;

import java.math.BigDecimal;

/**
 * Suite: suma un cargo fijo por estadía (rooms.extra_service_fee en la BD).
 */
public class SuiteRoom extends Room {

    private final BigDecimal extraServiceFee;

    public SuiteRoom(int number, int capacity, BigDecimal pricePerNight, BigDecimal extraServiceFee) {
        super(number, capacity, pricePerNight);
        if (extraServiceFee == null || extraServiceFee.signum() < 0) {
            throw new IllegalArgumentException("El cargo extra de la suite no puede ser negativo.");
        }
        this.extraServiceFee = extraServiceFee;
    }

    @Override
    public RoomType getType() {
        return RoomType.SUITE;
    }

    @Override
    public BigDecimal getExtraServiceFee() {
        return extraServiceFee;
    }
}
