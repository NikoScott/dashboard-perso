package com.crm.freelance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Une relance = une action de suivi sur une opportunité
 * (ex. "relancé par mail le 12/03, en attente de réponse").
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Relance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(length = 1000)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutRelance statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunite_id")
    private Opportunite opportunite;

    @PrePersist
    void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
    }
}
