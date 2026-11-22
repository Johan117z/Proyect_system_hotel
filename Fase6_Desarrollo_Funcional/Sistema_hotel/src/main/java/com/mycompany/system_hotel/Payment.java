package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago total de una factura. Con él la cuenta queda saldada y se permite el check-out (HU-13 / RN-09).
 * Se guarda aparte de la factura para que esta siga siendo inmutable (RN-12).
 * Corresponde a la tabla payments.
 */
public final class Payment {

    private final Invoice invoice;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private final LocalDateTime paidAt;

    public Payment(Invoice invoice, PaymentMethod method) {
        if (invoice == null) {
            throw new IllegalArgumentException("La factura es obligatoria para registrar el pago.");
        }
        if (method == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio.");
        }
        this.invoice = invoice;
        this.amount = invoice.getTotalAmount();   // pago total: la cuenta queda en cero
        this.method = method;
        this.paidAt = LocalDateTime.now();
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

    @Override
    public String toString() {
        return "Pago de factura No. " + invoice.getInvoiceId() + ": " + ConsoleUtils.formatCop(amount)
                + " (" + method.getLabel() + ", " + ConsoleUtils.formatDate(paidAt.toLocalDate()) + ")";
    }
}
