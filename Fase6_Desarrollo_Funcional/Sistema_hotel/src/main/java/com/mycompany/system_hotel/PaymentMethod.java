package com.mycompany.system_hotel;

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

    /** Acepta el nombre en inglés (CASH) o en español (Efectivo), sin distinguir mayúsculas. */
    public static PaymentMethod fromText(String text) {
        if (text != null) {
            String clean = text.trim();
            for (PaymentMethod method : values()) {
                if (method.name().equalsIgnoreCase(clean) || method.label.equalsIgnoreCase(clean)) {
                    return method;
                }
            }
        }
        throw new IllegalArgumentException("Medio de pago inválido. Use Efectivo, Tarjeta o Transferencia.");
    }
}
