package com.crm.freelance.controller;

import com.crm.freelance.dto.StatsResponse;
import com.crm.freelance.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Couche HTTP des statistiques. Un seul endpoint de lecture qui agrège les
 * indicateurs globaux (cf. StatsService).
 */
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** GET /stats -> indicateurs globaux (nb par statut, relances en attente, TJM moyen, pipeline). */
    @GetMapping("/stats")
    public StatsResponse stats() {
        return statsService.calculer();
    }
}
