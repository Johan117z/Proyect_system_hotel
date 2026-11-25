package com.mateojohan.sistemahotel.dto;

import jakarta.validation.constraints.NotNull;

import com.mateojohan.sistemahotel.enums.PaymentMethod;

/** Pago total de una factura (RN-09). El monto lo toma el servidor de la factura. */
public record PaymentRequestDTO(

        @NotNull(message = "El medio de pago es obligatorio (CASH, CARD o TRANSFER).")
        PaymentMethod method) {
}
