package com.mycompany.system_hotel;

import java.math.BigDecimal;

public class DoubleRoom extends Room {

    public DoubleRoom(int number, int capacity, BigDecimal pricePerNight) {
        super(number, capacity, pricePerNight);
    }

    @Override
    public RoomType getType() {
        return RoomType.DOUBLE;
    }
}
