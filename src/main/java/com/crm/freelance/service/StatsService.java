package com.crm.freelance.service;

import com.crm.freelance.dto.StatsResponse;
import com.crm.freelance.model.Opportunite;
import com.crm.freelance.model.StatutOpportunite;
import com.crm.freelance.model.StatutRelance;
import com.crm.freelance.model.TypeOpportunite;
import com.crm.freelance.repository.OpportuniteRepository;
import com.crm.freelance.repository.RelanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final OpportuniteRepository opportuniteRepository;
    private final RelanceRepository relanceRepository;

    @Transactional(readOnly = true)
    public StatsResponse calculer() {
        List<Opportunite> opportunites = opportuniteRepository.findAll();

        // 1. Nombre d'opportunités par statut : groupingBy + counting
        Map<StatutOpportunite, Long> parStatut = opportunites.stream()
                .collect(Collectors.groupingBy(Opportunite::getStatut, Collectors.counting()));

        // 2. Relances en attente : compté directement en base
        long relancesEnAttente = relanceRepository.countByStatut(StatutRelance.EN_ATTENTE);

        // 3. TJM moyen des missions freelance NON terminées
        List<BigDecimal> tjms = opportunites.stream()
                .filter(o -> o.getType() == TypeOpportunite.MISSION_FREELANCE)
                .filter(o -> !o.getStatut().estTerminal())
                .map(Opportunite::getTjm)
                .filter(Objects::nonNull)
                .toList();
        BigDecimal tjmMoyen = tjms.isEmpty()
                ? BigDecimal.ZERO
                : tjms.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(tjms.size()), 2, RoundingMode.HALF_UP);

        // 4. Pipeline total : somme des montants des opportunités NON terminées
        BigDecimal pipelineTotal = opportunites.stream()
                .filter(o -> !o.getStatut().estTerminal())
                .map(Opportunite::montantPipeline)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new StatsResponse(parStatut, relancesEnAttente, tjmMoyen, pipelineTotal);
    }
}
