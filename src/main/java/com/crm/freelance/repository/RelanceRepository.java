package com.crm.freelance.repository;

import com.crm.freelance.model.Relance;
import com.crm.freelance.model.StatutRelance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelanceRepository extends JpaRepository<Relance, Long> {

    /** Historique des relances d'une opportunité, de la plus récente à la plus ancienne. */
    List<Relance> findByOpportunite_IdOrderByDateDesc(Long opportuniteId);

    /** Pour les stats : nombre de relances dans un statut donné (ex. EN_ATTENTE). */
    long countByStatut(StatutRelance statut);
}
