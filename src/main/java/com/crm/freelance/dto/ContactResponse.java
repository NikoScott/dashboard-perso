package com.crm.freelance.dto;

import com.crm.freelance.model.Canal;

import java.util.List;

/**
 * Vue API d'un contact, avec la liste de ses opportunités.
 * Sert pour GET /contacts/{id} (détail + opportunités).
 */
public record ContactResponse(
        Long id,
        String nom,
        String prenom,
        String entreprise,
        String email,
        String telephone,
        Canal canal,
        List<OpportuniteResponse> opportunites
) {
}
