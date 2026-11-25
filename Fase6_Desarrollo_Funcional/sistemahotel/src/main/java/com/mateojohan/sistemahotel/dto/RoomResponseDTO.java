package com.mateojohan.sistemahotel.dto;

import java.math.BigDecimal;

import com.mateojohan.sistemahotel.enums.RoomStatus;
import com.mateojohan.sistemahotel.enums.RoomType;
import com.mateojohan.sistemahotel.models.Room;

/** Habitación tal como la recibe el cliente Web/Móvil. */
public record RoomResponseDTO(
        Integer id,
        Integer roomNumber,
        RoomType type,
        Integer capacity,
        BigDecimal pricePerNight,
        BigDecimal extraServiceFee,
        RoomStatus status) {

    public static RoomResponseDTO from(Room room) {
        return new RoomResponseDTO(room.getId(), room.getRoomNumber(), room.getType(), room.getCapacity(),
                room.getPricePerNight(), room.getExtraServiceFee(), room.getStatus());
    }
}
