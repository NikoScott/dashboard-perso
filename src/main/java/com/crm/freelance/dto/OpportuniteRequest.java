package com.crm.freelance.dto;

import com.crm.freelance.model.StatutOpportunite;
import com.crm.freelance.model.TypeOpportunite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Données reçues pour créer/modifier une opportunité.
 * On n'envoie PAS un objet Contact complet, juste son id (contactId) : le front
 * n'a pas à connaître la structure interne de l'entité Contact.
 */
public record OpportuniteRequest(
        @NotBlank(message = "Le titre est obligatoire")
        String titre,

        @NotNull(message = "Le type est obligatoire")
        TypeOpportunite type,

        StatutOpportunite statut, // facultatif à la création : défaut CONTACTE dans le service

        BigDecimal tjm,
        BigDecimal salaire,
        BigDecimal budget,

        String note,

        @NotNull(message = "Le contactId est obligatoire")
        Long contactId
) {
}
