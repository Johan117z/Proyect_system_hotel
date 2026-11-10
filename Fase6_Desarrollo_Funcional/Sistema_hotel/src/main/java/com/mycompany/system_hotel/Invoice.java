package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Factura generada en el check-out (FR-09). Todos los campos son final: BR-03 (inmutable).
 * Los valores se copian al emitirla para que un cambio posterior de tarifa no altere la factura.
 */
public final class Invoice {

    private final int invoiceId;
    private final Reservation reservation;
    private final long stayDays;
    private final BigDecimal nightRate;
    private final BigDecimal extraServiceFee;
    private final BigDecimal additionalServices;
    private final BigDecimal totalAmount;
    private final LocalDateTime issueDate;

    public Invoice(int invoiceId, Reservation reservation, BigDecimal additionalServices) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reserva es obligatoria para facturar.");
        }
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new BusinessRuleException("FR-09", "Solo se factura una reserva con check-out realizado.");
        }
        if (additionalServices == null || additionalServices.signum() < 0) {
            throw new IllegalArgumentException("Los servicios adicionales no pueden ser negativos.");
        }
        IRoom room = reservation.getRoom();
        this.invoiceId = invoiceId;
        this.reservation = reservation;
        this.stayDays = reservation.getStayDays();
        this.nightRate = room.getPricePerNight();
        this.extraServiceFee = room.getExtraServiceFee();
        this.additionalServices = additionalServices;
        // total = noches × tarifa + cargo extra de la habitación + servicios adicionales
        this.totalAmount = room.calculatePrice(stayDays).add(additionalServices);
        this.issueDate = LocalDateTime.now();
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public long getStayDays() {
        return stayDays;
    }

    public BigDecimal getNightRate() {
        return nightRate;
    }

    public BigDecimal getExtraServiceFee() {
        return extraServiceFee;
    }

    public BigDecimal getAdditionalServices() {
        return additionalServices;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    @Override
    public String toString() {
        return "FACTURA No. " + invoiceId + "\n"
                + "Fecha de emisión: " + ConsoleUtils.formatDate(issueDate.toLocalDate()) + "\n"
                + "Reserva: " + reservation.getReservationCode() + "\n"
                + "Huésped: " + reservation.getGuest().getFullName() + "\n"
                + "Habitación: " + reservation.getRoom().getNumber() + " (" + reservation.getRoom().getType() + ")\n"
                + "Noches: " + stayDays + " × " + ConsoleUtils.formatCop(nightRate) + "\n"
                + "Cargo extra habitación: " + ConsoleUtils.formatCop(extraServiceFee) + "\n"
                + "Servicios adicionales: " + ConsoleUtils.formatCop(additionalServices) + "\n"
                + "TOTAL: " + ConsoleUtils.formatCop(totalAmount);
    }
}
