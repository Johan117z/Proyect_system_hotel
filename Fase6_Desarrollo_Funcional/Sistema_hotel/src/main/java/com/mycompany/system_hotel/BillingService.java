package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Épica 5 — Facturación (HU-14): servicios adicionales de la estadía, factura y pago.
 */
public class BillingService {

    private final HotelStore store;

    public BillingService(HotelStore store) {
        this.store = store;
    }

    /** Registra un servicio consumido durante la estadía. No se admiten después de facturar (RN-12). */
    public AdditionalService addService(String reservationCode, String description, BigDecimal amount) {
        Reservation reservation = store.findReservation(reservationCode);
        if (store.findInvoiceFor(reservation).isPresent()) {
            throw new BusinessRuleException("RN-12", "La estadía ya fue facturada; no se pueden agregar más servicios.");
        }
        AdditionalService service = new AdditionalService(description, amount);
        reservation.addService(service);
        return service;
    }

    /** HU-14 / RN-11: factura de una estadía en curso. Una reserva tiene como máximo una factura (RN-12). */
    public Invoice generateInvoice(String reservationCode) {
        Reservation reservation = store.findReservation(reservationCode);
        if (store.findInvoiceFor(reservation).isPresent()) {
            throw new BusinessRuleException("RN-12", "La estadía " + reservation.getReservationCode() + " ya fue facturada.");
        }
        Invoice invoice = new Invoice(store.nextInvoiceNumber(), reservation);
        store.addInvoice(invoice);
        return invoice;
    }

    /** Registra el pago total de una factura; con esto la cuenta queda saldada (RN-09). */
    public Payment registerPayment(int invoiceId, PaymentMethod method) {
        Invoice invoice = store.findInvoice(invoiceId);
        if (store.isPaid(invoice)) {
            throw new BusinessRuleException("RN-09", "La factura No. " + invoiceId + " ya está pagada.");
        }
        Payment payment = new Payment(invoice, method);
        store.addPayment(payment);
        return payment;
    }

    public Invoice findInvoice(int invoiceId) {
        return store.findInvoice(invoiceId);
    }

    public Optional<Invoice> findInvoiceFor(Reservation reservation) {
        return store.findInvoiceFor(reservation);
    }

    public boolean isPaid(Invoice invoice) {
        return store.isPaid(invoice);
    }

    public List<Invoice> getAllInvoices() {
        return store.getInvoices();
    }

    public List<Payment> getAllPayments() {
        return store.getPayments();
    }
}
