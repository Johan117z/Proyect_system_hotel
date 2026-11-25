package com.mateojohan.sistemahotel.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Servicio adicional consumido durante la estadía (HU-14). */
public record AdditionalServiceRequestDTO(

        @NotBlank(message = "La descripción del servicio es obligatoria.")
        @Size(max = 80, message = "La descripción admite máximo 80 caracteres.")
        String description,

        @NotNull(message = "El valor del servicio es obligatorio.")
        @Positive(message = "El valor del servicio debe ser mayor que cero.")
        @Digits(integer = 8, fraction = 2, message = "El valor admite máximo 8 enteros y 2 decimales.")
        BigDecimal amount) {
}
