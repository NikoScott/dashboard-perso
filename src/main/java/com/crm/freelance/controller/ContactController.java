package com.crm.freelance.controller;

import com.crm.freelance.dto.ContactRequest;
import com.crm.freelance.dto.ContactResponse;
import com.crm.freelance.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactResponse> creer(@Valid @RequestBody ContactRequest req) {
        ContactResponse cree = contactService.creer(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    /**
     * GET /contacts?page=0&size=20&sort=nom,asc
     * Retourne une page de contacts (metadata de pagination incluse dans la réponse JSON).
     */
    @GetMapping
    public Page<ContactResponse> lister(
            @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return contactService.lister(pageable);
    }

    @GetMapping("/{id}")
    public ContactResponse detail(@PathVariable Long id) {
        return contactService.detail(id);
    }

    @PutMapping("/{id}")
    public ContactResponse modifier(@PathVariable Long id, @Valid @RequestBody ContactRequest req) {
        return contactService.modifier(id, req);
    }

    @GetMapping("/entreprises")
    public List<String> entreprises() {
        return contactService.listerEntreprises();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        contactService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
