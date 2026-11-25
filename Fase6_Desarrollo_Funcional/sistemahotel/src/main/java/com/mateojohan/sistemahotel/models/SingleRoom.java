package com.mateojohan.sistemahotel.models;

import java.math.BigDecimal;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import com.mateojohan.sistemahotel.enums.RoomType;

/** Habitación sencilla: sin cargo extra. */
@Entity
@DiscriminatorValue("SINGLE")
public class SingleRoom extends Room {

    protected SingleRoom() {
    }

    public SingleRoom(Integer roomNumber, Integer capacity, BigDecimal pricePerNight) {
        super(roomNumber, capacity, pricePerNight, BigDecimal.ZERO);
    }

    @Override
    public RoomType getType() {
        return RoomType.SINGLE;
    }
}
