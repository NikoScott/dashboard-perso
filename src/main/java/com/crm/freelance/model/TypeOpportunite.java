package com.crm.freelance.model;

/**
 * Nature de l'opportunité. Détermine quel champ de montant est pertinent :
 * MISSION_FREELANCE -> tjm, CDI -> salaire, SITE_WEB -> budget.
 */
public enum TypeOpportunite {
    MISSION_FREELANCE,
    CDI,
    SITE_WEB
}
