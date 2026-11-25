package com.mateojohan.sistemahotel.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** HU-09: datos para crear una reserva. Fechas en ISO 8601 (AAAA-MM-DD). */
public record ReservationRequestDTO(

        @NotBlank(message = "El documento del huésped es obligatorio.")
        @Pattern(regexp = "^[0-9]{6,10}$", message = "El documento debe tener entre 6 y 10 dígitos.")
        String guestDocument,

        @NotNull(message = "El número de habitación es obligatorio.")
        @Min(value = 1, message = "El número de habitación debe ser mayor que cero.")
        Integer roomNumber,

        @NotNull(message = "La fecha de entrada es obligatoria.")
        @FutureOrPresent(message = "La fecha de entrada no puede ser anterior a hoy (RN-13).")
        LocalDate checkInDate,

        @NotNull(message = "La fecha de salida es obligatoria.")
        LocalDate checkOutDate) {
}
