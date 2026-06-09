package com.crm.freelance.repository;

import com.crm.freelance.model.Opportunite;
import com.crm.freelance.model.StatutOpportunite;
import com.crm.freelance.model.TypeOpportunite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpportuniteRepository extends JpaRepository<Opportunite, Long> {

    /**
     * STYLE 1 — Requête dérivée : Spring lit le nom de la méthode et génère le SQL.
     * findByContact_Id => WHERE contact_id = ?
     */
    List<Opportunite> findByContact_Id(Long contactId);

    /**
     * STYLE 2 — JPQL (@Query) avec filtres optionnels.
     * Si :statut est null, le filtre est ignoré (idem pour :type). Permet de gérer
     * GET /opportunites?statut=&type= avec une seule requête, paramètres facultatifs.
     */
    @Query("""
            SELECT o FROM Opportunite o
            WHERE (:statut IS NULL OR o.statut = :statut)
              AND (:type   IS NULL OR o.type   = :type)
            """)
    List<Opportunite> rechercher(@Param("statut") StatutOpportunite statut,
                                 @Param("type") TypeOpportunite type);

    // NB : la détection des opportunités "à relancer" n'est volontairement PAS une
    // requête ici. La règle vit sur l'entité (Opportunite.estARelancer) pour rester
    // testable sans base ; le service filtre en mémoire (volume faible pour un CRM perso).
}
