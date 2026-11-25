package com.mateojohan.sistemahotel.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Contenedor universal de todas las respuestas de la API: { success, data, error, meta }.
 * La raíz del JSON nunca cambia, sea un objeto, una lista o un error.
 *
 * @param <T> tipo del recurso que viaja en data (un DTO o una lista de DTO)
 */
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ApiError error;
    private final ApiMeta meta = new ApiMeta();

    private ApiResponse(boolean success, T data, ApiError error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    /** Respuesta exitosa: success = true, error = null. */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /** Respuesta fallida: success = false, data = null. */
    public static <T> ApiResponse<T> error(String code, String message, Map<String, String> details) {
        return new ApiResponse<>(false, null, new ApiError(code, message, details));
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ApiError getError() {
        return error;
    }

    public ApiMeta getMeta() {
        return meta;
    }

    /** Detalle del error: código técnico, mensaje para el usuario y campos que fallaron. */
    public record ApiError(String code, String message, Map<String, String> details) {
    }

    /** Datos de contexto de la respuesta. */
    public static class ApiMeta {

        private final String timestamp = Instant.now().toString();
        private final String apiVersion = "v1.0";

        public String getTimestamp() {
            return timestamp;
        }

        public String getApiVersion() {
            return apiVersion;
        }
    }
}
