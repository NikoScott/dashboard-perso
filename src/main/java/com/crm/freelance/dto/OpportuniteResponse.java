package com.crm.freelance.dto;

import com.crm.freelance.model.StatutOpportunite;
import com.crm.freelance.model.TypeOpportunite;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Vue API d'une opportunité.
 * Point clé d'entretien : au lieu d'exposer l'entité Contact complète (avec ses
 * propres opportunités, ce qui créerait une boucle et chargerait toute la base),
 * on n'expose que contactId + contactNom : exactement ce dont le front a besoin.
 */
public record OpportuniteResponse(
        Long id,
        String titre,
        TypeOpportunite type,
        StatutOpportunite statut,
        BigDecimal tjm,
        BigDecimal salaire,
        BigDecimal budget,
        LocalDateTime dateCreation,
        LocalDateTime dateDerniereAction,
        String note,
        Long contactId,
        String contactNom,
        List<RelanceResponse> relances
) {
}
