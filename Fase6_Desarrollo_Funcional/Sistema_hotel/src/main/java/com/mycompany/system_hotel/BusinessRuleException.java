package com.mycompany.system_hotel;

/**
 * Violación de una regla de negocio (BR-xx) o de un estado inválido.
 * En la futura API REST se traducirá a HTTP 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    private final String ruleCode;

    public BusinessRuleException(String ruleCode, String message) {
        super(message);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }
}
