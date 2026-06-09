package com.crm.freelance.model;

/**
 * État d'une relance individuelle.
 */
public enum StatutRelance {
    EN_ATTENTE,   // relance planifiée, pas encore envoyée
    ENVOYEE,      // relance envoyée, en attente de réponse
    SANS_REPONSE  // relance restée sans réponse
}
