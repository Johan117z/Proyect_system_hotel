package com.mateojohan.sistemahotel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mateojohan.sistemahotel.enums.PaymentMethod;
import com.mateojohan.sistemahotel.models.Payment;

public record PaymentResponseDTO(
        Long id,
        Long invoiceId,
        BigDecimal amount,
        String currency,
        PaymentMethod method,
        String methodLabel,
        LocalDateTime paidAt) {

    public static PaymentResponseDTO from(Payment payment) {
        return new PaymentResponseDTO(payment.getId(), payment.getInvoice().getId(), payment.getAmount(), "COP",
                payment.getMethod(), payment.getMethod().getLabel(), payment.getPaidAt());
    }
}
