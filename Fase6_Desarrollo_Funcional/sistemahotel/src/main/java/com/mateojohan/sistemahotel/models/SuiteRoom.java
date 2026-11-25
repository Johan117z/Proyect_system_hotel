package com.mateojohan.sistemahotel.models;

import java.math.BigDecimal;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import com.mateojohan.sistemahotel.enums.RoomType;

/** Suite: suma un cargo fijo por estadía (rooms.extra_service_fee). */
@Entity
@DiscriminatorValue("SUITE")
public class SuiteRoom extends Room {

    protected SuiteRoom() {
    }

    public SuiteRoom(Integer roomNumber, Integer capacity, BigDecimal pricePerNight, BigDecimal extraServiceFee) {
        super(roomNumber, capacity, pricePerNight, extraServiceFee);
    }

    @Override
    public RoomType getType() {
        return RoomType.SUITE;
    }
}
