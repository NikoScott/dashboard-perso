package com.crm.freelance.dto;

import com.crm.freelance.model.Canal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Données reçues pour créer/modifier un contact.
 * La validation (Bean Validation) se fait ICI, à la frontière de l'API,
 * pas dans l'entité : on refuse les données invalides avant de toucher au métier.
 */
public record ContactRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        String prenom,
        String entreprise,

        @Email(message = "Email invalide")
        String email,

        String telephone,

        @NotNull(message = "Le canal est obligatoire")
        Canal canal
) {
}
