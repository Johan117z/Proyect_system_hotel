package com.mateojohan.sistemahotel.dto;

import jakarta.validation.constraints.NotNull;

import com.mateojohan.sistemahotel.enums.RoomStatus;

/** Cambio manual de estado AVAILABLE <-> MAINTENANCE (RN-15). */
public record RoomStatusDTO(

        @NotNull(message = "El estado es obligatorio (AVAILABLE o MAINTENANCE).")
        RoomStatus status) {
}
