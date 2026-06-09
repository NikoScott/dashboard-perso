package com.crm.freelance.dto;

import com.crm.freelance.model.StatutRelance;

import java.time.LocalDateTime;

/**
 * Données reçues pour ajouter une relance à une opportunité.
 * date et statut sont facultatifs : valeurs par défaut posées dans le service
 * (date = maintenant, statut = EN_ATTENTE).
 */
public record RelanceRequest(
        String note,
        StatutRelance statut,
        LocalDateTime date
) {
}
