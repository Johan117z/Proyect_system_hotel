package com.mateojohan.sistemahotel.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** HU-02: nueva tarifa por noche. No altera facturas ya emitidas (RN-12). */
public record RoomRateDTO(

        @NotNull(message = "La tarifa por noche es obligatoria.")
        @Positive(message = "La tarifa por noche debe ser mayor que cero.")
        @Digits(integer = 8, fraction = 2, message = "La tarifa admite máximo 8 enteros y 2 decimales.")
        BigDecimal pricePerNight) {
}
