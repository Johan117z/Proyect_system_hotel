package com.mateojohan.sistemahotel.enums;

/**
 * Medio de pago de una factura. Coincide con payments.method en la BD.
 */
public enum PaymentMethod {
    CASH("Efectivo"),
    CARD("Tarjeta"),
    TRANSFER("Transferencia");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
