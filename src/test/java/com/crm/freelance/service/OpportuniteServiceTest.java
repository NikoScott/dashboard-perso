package com.crm.freelance.service;

import com.crm.freelance.dto.OpportuniteResponse;
import com.crm.freelance.dto.RelanceRequest;
import com.crm.freelance.model.Contact;
import com.crm.freelance.model.Opportunite;
import com.crm.freelance.model.StatutOpportunite;
import com.crm.freelance.model.TypeOpportunite;
import com.crm.freelance.repository.ContactRepository;
import com.crm.freelance.repository.OpportuniteRepository;
import com.crm.freelance.repository.RelanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Tests UNITAIRES de la logique métier des opportunités.
 *
 * Pourquoi des mocks (Mockito) et pas une vraie base ?
 *  - On teste UNE règle métier à la fois, isolée de JPA / H2 / Spring.
 *  - C'est rapide (pas de contexte Spring à démarrer) et ça pointe précisément la
 *    faute si un test casse : c'est le service qui est en cause, pas l'infra.
 *
 * @Mock crée de faux repositories ; @InjectMocks les injecte dans le service réel.
 */
@ExtendWith(MockitoExtension.class)
class OpportuniteServiceTest {

    @Mock private OpportuniteRepository opportuniteRepository;
    @Mock private ContactRepository contactRepository;
    @Mock private RelanceRepository relanceRepository;

    @InjectMocks private OpportuniteService opportuniteService;

    @BeforeEach
    void setUp() {
        // delaiRelanceJours est normalement injecté via @Value depuis application.properties.
        // En test unitaire (sans contexte Spring), on le fixe à la main à 7.
        ReflectionTestUtils.setField(opportuniteService, "delaiRelanceJours", 7);
    }

    private Opportunite opportunite(StatutOpportunite statut, LocalDateTime derniereAction) {
        return Opportunite.builder()
                .id(1L)
                .titre("Mission test")
                .type(TypeOpportunite.MISSION_FREELANCE)
                .statut(statut)
                .dateDerniereAction(derniereAction)
                .contact(Contact.builder().id(1L).nom("Durand").build())
                .build();
    }

    @Test
    @DisplayName("À relancer : une opportunité active inactive depuis > 7 jours remonte, une récente non")
    void detecteLesOpportunitesARelancer() {
        Opportunite vieille = opportunite(StatutOpportunite.EN_DISCUSSION, LocalDateTime.now().minusDays(10));
        Opportunite recente = opportunite(StatutOpportunite.EN_DISCUSSION, LocalDateTime.now().minusDays(2));
        when(opportuniteRepository.findAll()).thenReturn(List.of(vieille, recente));

        List<OpportuniteResponse> aRelancer = opportuniteService.opportunitesARelancer();

        // Seule la vieille (10 jours > seuil de 7) doit remonter.
        assertThat(aRelancer).hasSize(1);
        assertThat(aRelancer.get(0).statut()).isEqualTo(StatutOpportunite.EN_DISCUSSION);
    }

    @Test
    @DisplayName("À relancer : une opportunité GAGNE ou PERDU (terminale) ne remonte jamais")
    void uneOpportuniteTerminaleNeRemontePas() {
        Opportunite gagnee = opportunite(StatutOpportunite.GAGNE, LocalDateTime.now().minusDays(30));
        Opportunite perdue = opportunite(StatutOpportunite.PERDU, LocalDateTime.now().minusDays(30));
        Opportunite active = opportunite(StatutOpportunite.EN_DISCUSSION, LocalDateTime.now().minusDays(30));
        when(opportuniteRepository.findAll()).thenReturn(List.of(gagnee, perdue, active));

        List<OpportuniteResponse> aRelancer = opportuniteService.opportunitesARelancer();

        // Malgré 30 jours d'inactivité, GAGNE et PERDU sont exclus : seule l'active reste.
        assertThat(aRelancer).hasSize(1);
        assertThat(aRelancer.get(0).statut()).isEqualTo(StatutOpportunite.EN_DISCUSSION);
    }

    @Test
    @DisplayName("Ajouter une relance met à jour dateDerniereAction (l'opportunité sort des 'à relancer')")
    void ajouterRelanceMetAJourLaDerniereAction() {
        Opportunite opportunite = opportunite(StatutOpportunite.EN_DISCUSSION, LocalDateTime.now().minusDays(20));
        when(opportuniteRepository.findById(anyLong())).thenReturn(java.util.Optional.of(opportunite));
        when(opportuniteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(relanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime avant = LocalDateTime.now();
        opportuniteService.ajouterRelance(1L, new RelanceRequest("Relancé par mail", null, null));

        // La relance a bien été rattachée...
        assertThat(opportunite.getRelances()).hasSize(1);
        // ...et la dernière action a été rafraîchie à "maintenant" (>= avant l'appel).
        assertThat(opportunite.getDateDerniereAction()).isAfterOrEqualTo(avant);
    }

    @Test
    @DisplayName("Changer le statut applique le nouveau statut et enregistre l'action")
    void changerStatutFonctionne() {
        Opportunite opportunite = opportunite(StatutOpportunite.EN_DISCUSSION, LocalDateTime.now().minusDays(5));
        when(opportuniteRepository.findById(anyLong())).thenReturn(java.util.Optional.of(opportunite));
        when(opportuniteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OpportuniteResponse res = opportuniteService.changerStatut(1L, StatutOpportunite.GAGNE);

        assertThat(res.statut()).isEqualTo(StatutOpportunite.GAGNE);
        assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.GAGNE);
    }
}
