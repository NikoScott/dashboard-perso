package com.crm.freelance.dto;

import com.crm.freelance.model.StatutOpportunite;
import jakarta.validation.constraints.NotNull;

/**
 * Corps minimal pour PUT /opportunites/{id}/statut : on ne change QUE le statut.
 * Un DTO dédié plutôt que de réutiliser OpportuniteRequest : l'intention est claire
 * et on ne risque pas de modifier d'autres champs par effet de bord.
 */
public record ChangementStatutRequest(
        @NotNull(message = "Le statut est obligatoire")
        StatutOpportunite statut
) {
}
