package com.mateojohan.sistemahotel.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.mateojohan.sistemahotel.enums.PaymentMethod;

/**
 * Pago total de una factura (RN-09) -> tabla payments. Va en tabla aparte para que la factura
 * siga siendo inmutable (RN-12). El monto siempre es el total de la factura.
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, unique = true, updatable = false)
    private Invoice invoice;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, updatable = false)
    private PaymentMethod method;

    @Column(name = "paid_at", nullable = false, updatable = false)
    private LocalDateTime paidAt;

    protected Payment() {
    }

    public Payment(Invoice invoice, PaymentMethod method) {
        this.invoice = invoice;
        this.amount = invoice.getTotalAmount();
        this.method = method;
    }

    @PrePersist
    void onCreate() {
        paidAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
