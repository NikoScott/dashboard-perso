package com.crm.freelance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Une opportunité = une piste commerciale rattachée à un contact
 * (une mission freelance, un poste CDI, un projet de site web...).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOpportunite type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutOpportunite statut;

    // Un seul de ces trois champs est rempli, selon le type (cf. montantPipeline()).
    private BigDecimal tjm;      // si MISSION_FREELANCE
    private BigDecimal salaire;  // si CDI
    private BigDecimal budget;   // si SITE_WEB

    private LocalDateTime dateCreation;
    private LocalDateTime dateDerniereAction;

    @Column(length = 2000)
    private String note;

    /**
     * Côté "propriétaire" de la relation : c'est cette table qui porte la colonne
     * contact_id (clé étrangère). LAZY = on ne charge le contact que si on y accède
     * réellement (évite des requêtes inutiles).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @OneToMany(mappedBy = "opportunite", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Relance> relances = new ArrayList<>();

    /**
     * Callbacks JPA : au moment de l'insertion en base, on initialise les deux dates.
     * Évite d'avoir à le faire à la main dans le service.
     */
    @PrePersist
    void onCreate() {
        LocalDateTime maintenant = LocalDateTime.now();
        if (dateCreation == null) {
            dateCreation = maintenant;
        }
        if (dateDerniereAction == null) {
            dateDerniereAction = maintenant;
        }
    }

    /**
     * Logique métier portée par l'entité : le montant qui compte pour le "pipeline"
     * dépend du type. Centralisé ici plutôt que dispersé dans les services.
     */
    public BigDecimal montantPipeline() {
        return switch (type) {
            case MISSION_FREELANCE -> tjm;
            case CDI -> salaire;
            case SITE_WEB -> budget;
        };
    }

    /**
     * Règle métier centrale : une opportunité est "à relancer" si elle n'est pas
     * terminée (ni GAGNE ni PERDU) ET que sa dernière action est antérieure au seuil
     * (typiquement maintenant - 7 jours). Pure (pas d'accès base) => testable seule.
     */
    public boolean estARelancer(LocalDateTime seuil) {
        return !statut.estTerminal()
                && dateDerniereAction != null
                && dateDerniereAction.isBefore(seuil);
    }
}
