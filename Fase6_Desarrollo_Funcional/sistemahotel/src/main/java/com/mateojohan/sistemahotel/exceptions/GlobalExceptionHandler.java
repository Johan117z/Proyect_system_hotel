package com.mateojohan.sistemahotel.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.mateojohan.sistemahotel.dto.ApiResponse;

/**
 * Escudo global: convierte cada excepción en el wrapper ApiResponse con el código HTTP que
 * corresponde. Los controladores no tienen try-catch; solo manejan el flujo exitoso.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 400: uno o varios campos del DTO no cumplen sus anotaciones (@NotBlank, @Pattern, ...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Los datos enviados son inválidos.", details);
    }

    /** 400: validaciones que cruzan varios campos (rango de fechas, cargo extra solo para suite). */
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidData(InvalidDataException ex) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(),
                Map.of(ex.getField(), ex.getMessage()));
    }

    /** 400: JSON mal formado, fecha que no es ISO 8601 o valor de enum inexistente. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "El cuerpo de la petición no es un JSON válido o tiene valores con formato incorrecto "
                        + "(fechas en formato AAAA-MM-DD; estados y tipos en mayúscula, ej. SUITE).", null);
    }

    /** 400: parámetro de URL con tipo incorrecto (ej. ?checkIn=hoy) o parámetro obligatorio ausente. */
    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class })
    public ResponseEntity<ApiResponse<Void>> handleBadParameter(Exception ex) {
        String name = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName()
                : ((MissingServletRequestParameterException) ex).getParameterName();
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                "El parámetro '" + name + "' falta o tiene un formato inválido.",
                Map.of(name, "Valor ausente o con formato inválido."));
    }

    /** 404: el recurso no existe o está dado de baja. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), null);
    }

    /** 404: ruta inexistente. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoRoute(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND", "La ruta solicitada no existe.", null);
    }

    /** 409: regla de negocio violada. details.rule indica la RN/RF para la trazabilidad. */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION", ex.getMessage(),
                Map.of("rule", ex.getRuleCode()));
    }

    /** 409: la BD rechazó la operación (UNIQUE, CHECK o un trigger RN-09 / RN-12). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad en la BD: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "La operación viola una restricción de la base de datos.", null);
    }

    /** 500: cualquier fallo no controlado. No se expone el stack trace al cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error inesperado. Intente de nuevo más tarde.", null);
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String code, String message,
                                                    Map<String, String> details) {
        return ResponseEntity.status(status).body(ApiResponse.error(code, message, details));
    }
}
