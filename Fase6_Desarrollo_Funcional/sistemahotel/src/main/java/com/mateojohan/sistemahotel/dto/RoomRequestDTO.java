package com.mateojohan.sistemahotel.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.mateojohan.sistemahotel.enums.RoomType;

/** HU-01: datos para registrar una habitación. */
public record RoomRequestDTO(

        @NotNull(message = "El número de habitación es obligatorio.")
        @Min(value = 1, message = "El número de habitación debe ser mayor que cero.")
        @Max(value = 65535, message = "El número de habitación admite máximo 65535.")
        Integer roomNumber,

        @NotNull(message = "El tipo es obligatorio (SINGLE, DOUBLE o SUITE).")
        RoomType type,

        @NotNull(message = "La capacidad es obligatoria.")
        @Min(value = 1, message = "La capacidad debe ser de al menos 1 huésped.")
        @Max(value = 255, message = "La capacidad admite máximo 255 huéspedes.")
        Integer capacity,

        @NotNull(message = "La tarifa por noche es obligatoria.")
        @Positive(message = "La tarifa por noche debe ser mayor que cero.")
        @Digits(integer = 8, fraction = 2, message = "La tarifa admite máximo 8 enteros y 2 decimales.")
        BigDecimal pricePerNight,

        @PositiveOrZero(message = "El cargo extra no puede ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "El cargo extra admite máximo 8 enteros y 2 decimales.")
        BigDecimal extraServiceFee) {
}
