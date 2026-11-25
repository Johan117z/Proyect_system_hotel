package com.mateojohan.sistemahotel.exceptions;

/**
 * Datos de entrada inválidos que dependen de más de un campo (por ejemplo, un rango de fechas)
 * y por eso no se pueden validar con una sola anotación del DTO.
 * GlobalExceptionHandler la traduce a HTTP 400 Bad Request.
 */
public class InvalidDataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String field;

    public InvalidDataException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
