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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/opportunites")
@RequiredArgsConstructor
public class OpportuniteController {

    private final OpportuniteService opportuniteService;

    @PostMapping
    public ResponseEntity<OpportuniteResponse> creer(@Valid @RequestBody OpportuniteRequest req) {
        OpportuniteResponse creee = opportuniteService.creer(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }

    /**
     * GET /opportunites?statut=&type=&page=0&size=20
     * Filtres et pagination combinés.
     */
    @GetMapping
    public Page<OpportuniteResponse> rechercher(
            @RequestParam(required = false) StatutOpportunite statut,
            @RequestParam(required = false) TypeOpportunite type,
            @PageableDefault(size = 20) Pageable pageable) {
        return opportuniteService.rechercher(statut, type, pageable);
    }

    @GetMapping("/a-relancer")
    public List<OpportuniteResponse> aRelancer() {
        return opportuniteService.opportunitesARelancer();
    }

    @PutMapping("/{id}")
    public OpportuniteResponse modifier(@PathVariable Long id,
                                        @Valid @RequestBody OpportuniteRequest req) {
        return opportuniteService.modifier(id, req);
    }

    @PutMapping("/{id}/statut")
    public OpportuniteResponse changerStatut(@PathVariable Long id,
                                             @Valid @RequestBody ChangementStatutRequest req) {
        return opportuniteService.changerStatut(id, req.statut());
    }

    @PostMapping("/{id}/relances")
    public ResponseEntity<RelanceResponse> ajouterRelance(@PathVariable Long id,
                                                          @RequestBody RelanceRequest req) {
        RelanceResponse ajoutee = opportuniteService.ajouterRelance(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ajoutee);
    }

    @GetMapping("/{id}/relances")
    public List<RelanceResponse> historiqueRelances(@PathVariable Long id) {
        return opportuniteService.historiqueRelances(id);
    }
}
