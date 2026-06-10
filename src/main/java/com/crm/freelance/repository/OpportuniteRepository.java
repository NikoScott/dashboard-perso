package com.crm.freelance.repository;

import com.crm.freelance.model.Opportunite;
import com.crm.freelance.model.StatutOpportunite;
import com.crm.freelance.model.TypeOpportunite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpportuniteRepository extends JpaRepository<Opportunite, Long> {

    List<Opportunite> findByContact_Id(Long contactId);

    @Query(value = """
            SELECT o FROM Opportunite o
            WHERE (:statut IS NULL OR o.statut = :statut)
              AND (:type   IS NULL OR o.type   = :type)
            """,
            countQuery = """
            SELECT COUNT(o) FROM Opportunite o
            WHERE (:statut IS NULL OR o.statut = :statut)
              AND (:type   IS NULL OR o.type   = :type)
            """)
    Page<Opportunite> rechercher(@Param("statut") StatutOpportunite statut,
                                 @Param("type") TypeOpportunite type,
                                 Pageable pageable);

    // NB : la détection des opportunités "à relancer" n'est volontairement PAS une
    // requête ici. La règle vit sur l'entité (Opportunite.estARelancer) pour rester
    // testable sans base ; le service filtre en mémoire (volume faible pour un CRM perso).
}
