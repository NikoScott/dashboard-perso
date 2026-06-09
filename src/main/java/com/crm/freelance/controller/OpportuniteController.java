package com.crm.freelance.controller;

import com.crm.freelance.dto.ChangementStatutRequest;
import com.crm.freelance.dto.OpportuniteRequest;
import com.crm.freelance.dto.OpportuniteResponse;
import com.crm.freelance.dto.RelanceRequest;
import com.crm.freelance.dto.RelanceResponse;
import com.crm.freelance.model.StatutOpportunite;
import com.crm.freelance.model.TypeOpportunite;
import com.crm.freelance.service.OpportuniteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Couche HTTP des opportunités. Regroupe aussi les routes de relances, car une
 * relance n'existe jamais seule : elle est toujours rattachée à une opportunité
 * (/opportunites/{id}/relances) — c'est le principe des ressources imbriquées REST.
 */
@RestController
@RequestMapping("/opportunites")
@RequiredArgsConstructor
public class OpportuniteController {

    private final OpportuniteService opportuniteService;

    /** POST /opportunites -> 201 + l'opportunité créée. */
    @PostMapping
    public ResponseEntity<OpportuniteResponse> creer(@Valid @RequestBody OpportuniteRequest req) {
        OpportuniteResponse creee = opportuniteService.creer(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }

    /**
     * GET /opportunites?statut=&type= -> liste filtrée.
     * required = false : les deux filtres sont facultatifs (un filtre null = ignoré,
     * cf. la requête JPQL du repository).
     */
    @GetMapping
    public List<OpportuniteResponse> rechercher(
            @RequestParam(required = false) StatutOpportunite statut,
            @RequestParam(required = false) TypeOpportunite type) {
        return opportuniteService.rechercher(statut, type);
    }

    /**
     * GET /opportunites/a-relancer -> opportunités sans action depuis le délai configuré.
     * Déclaré AVANT toute route /{...} dynamique pour qu'il ne soit pas confondu
     * avec un id (ici pas de GET /{id}, mais c'est le bon réflexe).
     */
    @GetMapping("/a-relancer")
    public List<OpportuniteResponse> aRelancer() {
        return opportuniteService.opportunitesARelancer();
    }

    /** PUT /opportunites/{id} -> opportunité modifiée (404 si absente). */
    @PutMapping("/{id}")
    public OpportuniteResponse modifier(@PathVariable Long id,
                                        @Valid @RequestBody OpportuniteRequest req) {
        return opportuniteService.modifier(id, req);
    }

    /**
     * PUT /opportunites/{id}/statut -> change UNIQUEMENT le statut.
     * Endpoint dédié : l'intention est explicite et on enregistre l'action
     * (mise à jour de dateDerniereAction côté service).
     */
    @PutMapping("/{id}/statut")
    public OpportuniteResponse changerStatut(@PathVariable Long id,
                                             @Valid @RequestBody ChangementStatutRequest req) {
        return opportuniteService.changerStatut(id, req.statut());
    }

    /** POST /opportunites/{id}/relances -> 201 + la relance ajoutée (404 si opportunité absente). */
    @PostMapping("/{id}/relances")
    public ResponseEntity<RelanceResponse> ajouterRelance(@PathVariable Long id,
                                                          @RequestBody RelanceRequest req) {
        RelanceResponse ajoutee = opportuniteService.ajouterRelance(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ajoutee);
    }

    /** GET /opportunites/{id}/relances -> historique des relances (plus récente d'abord). */
    @GetMapping("/{id}/relances")
    public List<RelanceResponse> historiqueRelances(@PathVariable Long id) {
        return opportuniteService.historiqueRelances(id);
    }
}
