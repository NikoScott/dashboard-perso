package com.crm.freelance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Un contact = une personne avec qui on est en relation (prospect, client, recruteur...).
 * Un contact porte une ou plusieurs opportunités.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String prenom;
    private String entreprise;
    private String email;
    private String telephone;

    @Enumerated(EnumType.STRING) // stocke "LINKEDIN" en base, pas 0/1/2 (lisible + robuste si on réordonne l'enum)
    private Canal canal;

    /**
     * Côté "inverse" de la relation : Contact ne possède PAS la clé étrangère,
     * c'est Opportunite.contact qui la porte (mappedBy = "contact").
     * cascade = ALL + orphanRemoval : supprimer un contact supprime ses opportunités.
     */
    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Opportunite> opportunites = new ArrayList<>();
}
