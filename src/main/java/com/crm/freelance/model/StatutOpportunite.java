package com.crm.freelance.model;

/**
 * Étape de l'opportunité dans le pipeline commercial.
 *
 * Compromis volontaire : un seul enum pour tous les types d'opportunité, même si
 * certains statuts ne sont pas pertinents partout (ex. DEVIS_ENVOYE n'a pas de sens
 * pour un CDI). On gagne en simplicité ; l'alternative "propre" serait un statut par
 * type (héritage ou table de statuts) — à mentionner comme évolution possible.
 *
 * GAGNE et PERDU sont les deux statuts "terminaux" : une opportunité dans ces états
 * n'est plus à relancer.
 */
public enum StatutOpportunite {
    CONTACTE,
    EN_DISCUSSION,
    ENTRETIEN_PREVU,
    ENTRETIEN_PASSE,
    DEVIS_ENVOYE,
    OFFRE_RECUE,
    GAGNE,
    PERDU;

    /** Vrai si le statut est terminal (plus aucune action de relance attendue). */
    public boolean estTerminal() {
        return this == GAGNE || this == PERDU;
    }
}
