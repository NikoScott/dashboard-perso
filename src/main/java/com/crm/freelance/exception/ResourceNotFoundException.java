package com.crm.freelance.exception;

/**
 * Levée quand une ressource demandée (contact, opportunité...) n'existe pas.
 * Traduite en HTTP 404 par le GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entite, Long id) {
        return new ResourceNotFoundException(entite + " introuvable (id=" + id + ")");
    }
}
