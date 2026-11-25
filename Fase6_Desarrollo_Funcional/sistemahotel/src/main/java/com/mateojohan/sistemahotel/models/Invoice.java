package com.mateojohan.sistemahotel.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Factura de una estadía (HU-14) -> tabla invoices. Se emite con la reserva ACTIVE, antes del
 * check-out. Copia la tarifa y los cargos al emitirse y es inmutable (RN-12): @Immutable evita
 * cualquier UPDATE desde Hibernate y los triggers de la BD bloquean UPDATE y DELETE.
 */
@Entity
@Immutable
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(name = "stay_days", nullable = false)
    private Integer stayDays;

    @Column(name = "night_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal nightRate;

    @Column(name = "extra_service_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraServiceFee;

    @Column(name = "additional_services", nullable = false, precision = 10, scale = 2)
    private BigDecimal additionalServices;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    protected Invoice() {
    }

    private Invoice(Reservation reservation) {
        Room room = reservation.getRoom();
        long nights = reservation.getStayDays();
        this.reservation = reservation;
        this.stayDays = Math.toIntExact(nights);
        this.nightRate = room.getPricePerNight();
        this.extraServiceFee = room.getExtraServiceFee();
        this.additionalServices = reservation.getServicesTotal();
        // RN-11: noches × tarifa + cargo extra (suite) + servicios adicionales (= CHECK ck_invoices_total)
        this.totalAmount = room.calculatePrice(nights).add(additionalServices);
        this.issueDate = LocalDateTime.now();
    }

    /** HU-14: emite la factura de una estadía en curso. */
    public static Invoice issue(Reservation reservation) {
        reservation.ensureActive("RF-14",
                "No hay una estadía en curso para facturar: la reserva debe tener check-in.");
        return new Invoice(reservation);
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Integer getStayDays() {
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
}
