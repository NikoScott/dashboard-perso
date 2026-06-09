package com.crm.freelance.service;

import com.crm.freelance.dto.OpportuniteRequest;
import com.crm.freelance.dto.OpportuniteResponse;
import com.crm.freelance.dto.RelanceRequest;
import com.crm.freelance.dto.RelanceResponse;
import com.crm.freelance.exception.ResourceNotFoundException;
import com.crm.freelance.mapper.CrmMapper;
import com.crm.freelance.model.*;
import com.crm.freelance.repository.ContactRepository;
import com.crm.freelance.repository.OpportuniteRepository;
import com.crm.freelance.repository.RelanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpportuniteService {

    private final OpportuniteRepository opportuniteRepository;
    private final ContactRepository contactRepository;
    private final RelanceRepository relanceRepository;

    /**
     * Délai (jours) sans action au-delà duquel une opportunité est "à relancer".
     * Externalisé en configuration (application.properties : crm.relance.delai-jours),
     * défaut 7. Évite le "nombre magique" en dur dans le code.
     */
    @Value("${crm.relance.delai-jours:7}")
    private int delaiRelanceJours;

    @Transactional
    public OpportuniteResponse creer(OpportuniteRequest req) {
        Contact contact = chargerContact(req.contactId());
        Opportunite opportunite = Opportunite.builder()
                .titre(req.titre())
                .type(req.type())
                .statut(req.statut() != null ? req.statut() : StatutOpportunite.CONTACTE)
                .tjm(req.tjm())
                .salaire(req.salaire())
                .budget(req.budget())
                .note(req.note())
                .contact(contact)
                .build();
        return CrmMapper.toResponse(opportuniteRepository.save(opportunite));
    }

    @Transactional(readOnly = true)
    public List<OpportuniteResponse> rechercher(StatutOpportunite statut, TypeOpportunite type) {
        return opportuniteRepository.rechercher(statut, type).stream()
                .map(CrmMapper::toResponse)
                .toList();
    }

    @Transactional
    public OpportuniteResponse modifier(Long id, OpportuniteRequest req) {
        Opportunite opportunite = getOrThrow(id);
        opportunite.setTitre(req.titre());
        opportunite.setType(req.type());
        if (req.statut() != null) {
            opportunite.setStatut(req.statut());
        }
        opportunite.setTjm(req.tjm());
        opportunite.setSalaire(req.salaire());
        opportunite.setBudget(req.budget());
        opportunite.setNote(req.note());

        boolean contactChange = opportunite.getContact() == null
                || !opportunite.getContact().getId().equals(req.contactId());
        if (req.contactId() != null && contactChange) {
            opportunite.setContact(chargerContact(req.contactId()));
        }
        return CrmMapper.toResponse(opportuniteRepository.save(opportunite));
    }

    /** Change UNIQUEMENT le statut + enregistre l'action (met à jour dateDerniereAction). */
    @Transactional
    public OpportuniteResponse changerStatut(Long id, StatutOpportunite statut) {
        Opportunite opportunite = getOrThrow(id);
        opportunite.setStatut(statut);
        opportunite.setDateDerniereAction(LocalDateTime.now());
        return CrmMapper.toResponse(opportuniteRepository.save(opportunite));
    }

    /**
     * Opportunités à relancer : dernière action il y a plus de {delaiRelanceJours} jours
     * ET statut non terminal. La règle elle-même est sur l'entité (estARelancer).
     */
    @Transactional(readOnly = true)
    public List<OpportuniteResponse> opportunitesARelancer() {
        LocalDateTime seuil = LocalDateTime.now().minusDays(delaiRelanceJours);
        return opportuniteRepository.findAll().stream()
                .filter(o -> o.estARelancer(seuil))
                .map(CrmMapper::toResponse)
                .toList();
    }

    /**
     * Ajoute une relance à une opportunité.
     * Règle métier : ajouter une relance EST une action -> on met à jour
     * dateDerniereAction, donc l'opportunité sort de la liste "à relancer".
     */
    @Transactional
    public RelanceResponse ajouterRelance(Long opportuniteId, RelanceRequest req) {
        Opportunite opportunite = getOrThrow(opportuniteId);

        Relance relance = Relance.builder()
                .note(req.note())
                .statut(req.statut() != null ? req.statut() : StatutRelance.EN_ATTENTE)
                .date(req.date() != null ? req.date() : LocalDateTime.now())
                .opportunite(opportunite)
                .build();

        opportunite.getRelances().add(relance);
        opportunite.setDateDerniereAction(LocalDateTime.now());

        // On persiste la relance via SON repository : save() renvoie l'entité managée
        // avec son id déjà généré (stratégie IDENTITY -> INSERT immédiat). C'est cette
        // instance qu'on mappe, sinon la réponse renverrait id=null (l'id n'arriverait
        // qu'au flush de fin de transaction, après le return).
        Relance relancePersistee = relanceRepository.save(relance);
        opportuniteRepository.save(opportunite); // met à jour dateDerniereAction

        return CrmMapper.toResponse(relancePersistee);
    }

    @Transactional(readOnly = true)
    public List<RelanceResponse> historiqueRelances(Long opportuniteId) {
        getOrThrow(opportuniteId); // 404 si l'opportunité n'existe pas
        return relanceRepository.findByOpportunite_IdOrderByDateDesc(opportuniteId).stream()
                .map(CrmMapper::toResponse)
                .toList();
    }

    private Opportunite getOrThrow(Long id) {
        return opportuniteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Opportunité", id));
    }

    private Contact chargerContact(Long contactId) {
        return contactRepository.findById(contactId)
                .orElseThrow(() -> ResourceNotFoundException.of("Contact", contactId));
    }
}
