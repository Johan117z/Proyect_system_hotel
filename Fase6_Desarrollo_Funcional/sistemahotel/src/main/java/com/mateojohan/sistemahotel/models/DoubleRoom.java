package com.mateojohan.sistemahotel.models;

import java.math.BigDecimal;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import com.mateojohan.sistemahotel.enums.RoomType;

/** Habitación doble: sin cargo extra. */
@Entity
@DiscriminatorValue("DOUBLE")
public class DoubleRoom extends Room {

    protected DoubleRoom() {
    }

    public DoubleRoom(Integer roomNumber, Integer capacity, BigDecimal pricePerNight) {
        super(roomNumber, capacity, pricePerNight, BigDecimal.ZERO);
    }

    @Override
    public RoomType getType() {
        return RoomType.DOUBLE;
    }
}
