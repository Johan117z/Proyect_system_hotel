package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Factura de una estadía (HU-14 / RF-14). Se emite mientras la reserva está ACTIVE, antes del check-out,
 * porque el check-out exige la cuenta saldada (HU-13 / RN-09).
 * <p>
 * Total = noches × tarifa por noche + cargo extra de la habitación (solo Suite) + servicios adicionales (RN-11).
 * Todos los campos son final y se copian al emitirla: RN-12 (factura inmutable).
 */
public final class Invoice {

    private final int invoiceId;
    private final Reservation reservation;
    private final long stayDays;
    private final BigDecimal nightRate;
    private final BigDecimal extraServiceFee;
    private final List<AdditionalService> services;
    private final BigDecimal additionalServices;
    private final BigDecimal totalAmount;
    private final LocalDateTime issueDate;

    public Invoice(int invoiceId, Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reserva es obligatoria para facturar.");
        }
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessRuleException("RF-14",
                    "No hay una estadía en curso para facturar: la reserva debe tener check-in (estado actual: "
                            + reservation.getStatus() + ").");
        }
        IRoom room = reservation.getRoom();
        this.invoiceId = invoiceId;
        this.reservation = reservation;
        this.stayDays = reservation.getStayDays();
        this.nightRate = room.getPricePerNight();
        this.extraServiceFee = room.getExtraServiceFee();
        this.services = List.copyOf(reservation.getServices());
        this.additionalServices = reservation.getServicesTotal();
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

    public List<AdditionalService> getServices() {
        return services;
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
        StringBuilder detail = new StringBuilder()
                .append("FACTURA No. ").append(invoiceId).append('\n')
                .append("Fecha de emisión: ").append(ConsoleUtils.formatDate(issueDate.toLocalDate())).append('\n')
                .append("Reserva: ").append(reservation.getReservationCode()).append('\n')
                .append("Huésped: ").append(reservation.getGuest().getFullName())
                .append(" (").append(reservation.getGuest().getDocument()).append(")\n")
                .append("Habitación: ").append(reservation.getRoom().getNumber())
                .append(" (").append(reservation.getRoom().getType()).append(")\n")
                .append("Noches: ").append(stayDays).append(" × ").append(ConsoleUtils.formatCop(nightRate)).append('\n')
                .append("Cargo extra habitación: ").append(ConsoleUtils.formatCop(extraServiceFee)).append('\n')
                .append("Servicios adicionales: ").append(ConsoleUtils.formatCop(additionalServices)).append('\n');
        for (AdditionalService service : services) {
            detail.append("  · ").append(service).append('\n');
        }
        return detail.append("TOTAL: ").append(ConsoleUtils.formatCop(totalAmount)).toString();
    }
}
