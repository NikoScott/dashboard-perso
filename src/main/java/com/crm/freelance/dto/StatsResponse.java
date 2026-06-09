package com.crm.freelance.dto;

import com.crm.freelance.model.StatutOpportunite;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Indicateurs globaux renvoyés par GET /stats.
 */
public record StatsResponse(
        Map<StatutOpportunite, Long> nbOpportunitesParStatut,
        long nbRelancesEnAttente,
        BigDecimal tjmMoyenMissionsEnCours,
        BigDecimal pipelineTotal
) {
}
