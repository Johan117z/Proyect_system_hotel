package com.mycompany.system_hotel;

/**
 * La entidad solicitada no existe. En la futura API REST se traducirá a HTTP 404 Not Found.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
