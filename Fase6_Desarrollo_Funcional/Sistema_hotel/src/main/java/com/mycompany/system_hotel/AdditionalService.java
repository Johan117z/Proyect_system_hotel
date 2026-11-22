package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio adicional consumido durante la estadía (minibar, lavandería, etc.).
 * Se suma a la factura (HU-14 / RF-14). Inmutable: una vez registrado no cambia.
 * Corresponde a la tabla additional_services.
 */
public final class AdditionalService {

    public static final int MAX_DESCRIPTION_LENGTH = 80;

    private final String description;
    private final BigDecimal amount;
    private final LocalDateTime registeredAt;

    public AdditionalService(String description, BigDecimal amount) {
        if (description == null || description.isBlank() || description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("La descripción del servicio es obligatoria y admite máximo "
                    + MAX_DESCRIPTION_LENGTH + " caracteres.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("El valor del servicio debe ser mayor que cero.");
        }
        this.description = description.trim();
        this.amount = amount;
        this.registeredAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    @Override
    public String toString() {
        return description + ": " + ConsoleUtils.formatCop(amount);
    }
}
