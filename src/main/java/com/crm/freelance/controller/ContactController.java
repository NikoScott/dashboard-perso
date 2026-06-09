package com.crm.freelance.controller;

import com.crm.freelance.dto.ContactRequest;
import com.crm.freelance.dto.ContactResponse;
import com.crm.freelance.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Couche HTTP des contacts. Le controller est volontairement "bête" : il ne fait que
 *   1. recevoir la requête HTTP et la désérialiser (JSON -> DTO),
 *   2. déléguer au service (qui porte TOUTE la logique métier),
 *   3. renvoyer la réponse avec le bon code HTTP.
 *
 * Aucune règle métier ici : c'est ce qui rend le code testable et réutilisable.
 *
 * @Valid déclenche la validation Bean Validation du DTO (@NotBlank, @Email...) ;
 * en cas d'échec, le GlobalExceptionHandler renvoie un 400 détaillé.
 */
@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /** POST /contacts -> 201 Created + le contact créé. */
    @PostMapping
    public ResponseEntity<ContactResponse> creer(@Valid @RequestBody ContactRequest req) {
        ContactResponse cree = contactService.creer(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    /** GET /contacts -> liste de tous les contacts. */
    @GetMapping
    public List<ContactResponse> lister() {
        return contactService.lister();
    }

    /** GET /contacts/{id} -> détail du contact + ses opportunités (404 si absent). */
    @GetMapping("/{id}")
    public ContactResponse detail(@PathVariable Long id) {
        return contactService.detail(id);
    }

    /** PUT /contacts/{id} -> contact modifié (404 si absent). */
    @PutMapping("/{id}")
    public ContactResponse modifier(@PathVariable Long id, @Valid @RequestBody ContactRequest req) {
        return contactService.modifier(id, req);
    }

    /** DELETE /contacts/{id} -> 204 No Content (supprime aussi ses opportunités, cascade). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        contactService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
