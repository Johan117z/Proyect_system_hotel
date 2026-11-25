package com.mateojohan.sistemahotel.exceptions;

/**
 * El recurso buscado (habitación, huésped, reserva, factura) no existe o está dado de baja.
 * GlobalExceptionHandler la traduce a HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
