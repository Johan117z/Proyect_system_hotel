package com.mateojohan.sistemahotel.exceptions;

/**
 * Violación de una regla de negocio (RN-xx) o de una transición de estado inválida (RF-xx).
 * GlobalExceptionHandler la traduce a HTTP 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String ruleCode;

    public BusinessRuleException(String ruleCode, String message) {
        super(message);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }
}
