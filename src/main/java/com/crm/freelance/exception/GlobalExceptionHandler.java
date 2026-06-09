package com.crm.freelance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestion centralisée des erreurs : au lieu de gérer les try/catch dans chaque
 * controller, on attrape les exceptions ici et on renvoie une réponse JSON cohérente
 * avec le bon code HTTP. C'est le rôle de @RestControllerAdvice.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Ressource introuvable -> 404 Not Found. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Règle métier violée (ex. champ obligatoire manquant côté service) -> 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Validation Bean Validation (@Valid) échouée -> 400 avec le détail par champ.
     * Exemple : { "nom": "Le nom est obligatoire", "email": "Email invalide" }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erreursChamps = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> erreursChamps.put(err.getField(), err.getDefaultMessage()));

        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validation échouée");
        body.put("champs", erreursChamps);
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(baseBody(status, message));
    }

    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
