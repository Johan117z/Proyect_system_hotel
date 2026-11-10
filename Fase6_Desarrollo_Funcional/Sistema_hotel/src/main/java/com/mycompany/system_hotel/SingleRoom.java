package com.mycompany.system_hotel;

import java.math.BigDecimal;

public class SingleRoom extends Room {

    public SingleRoom(int number, int capacity, BigDecimal pricePerNight) {
        super(number, capacity, pricePerNight);
    }

    @Override
    public RoomType getType() {
        return RoomType.SINGLE;
    }
}
