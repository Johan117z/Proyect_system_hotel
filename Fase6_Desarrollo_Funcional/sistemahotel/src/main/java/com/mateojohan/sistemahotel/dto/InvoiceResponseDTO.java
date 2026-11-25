package com.mateojohan.sistemahotel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mateojohan.sistemahotel.models.Invoice;
import com.mateojohan.sistemahotel.models.Reservation;

/** Factura (HU-14). paid indica si la cuenta ya está saldada (RN-09). Moneda: COP. */
public record InvoiceResponseDTO(
        Long id,
        String reservationCode,
        String guestDocument,
        String guestName,
        Integer roomNumber,
        Integer stayDays,
        BigDecimal nightRate,
        BigDecimal extraServiceFee,
        BigDecimal additionalServices,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime issueDate,
        boolean paid) {

    public static InvoiceResponseDTO from(Invoice invoice, boolean paid) {
        Reservation reservation = invoice.getReservation();
        return new InvoiceResponseDTO(
                invoice.getId(),
                reservation.getReservationCode(),
                reservation.getGuest().getDocument(),
                reservation.getGuest().getFullName(),
                reservation.getRoom().getRoomNumber(),
                invoice.getStayDays(),
                invoice.getNightRate(),
                invoice.getExtraServiceFee(),
                invoice.getAdditionalServices(),
                invoice.getTotalAmount(),
                "COP",
                invoice.getIssueDate(),
                paid);
    }
}
