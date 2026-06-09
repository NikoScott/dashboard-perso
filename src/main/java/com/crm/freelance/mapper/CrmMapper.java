package com.crm.freelance.mapper;

import com.crm.freelance.dto.ContactResponse;
import com.crm.freelance.dto.OpportuniteResponse;
import com.crm.freelance.dto.RelanceResponse;
import com.crm.freelance.model.Contact;
import com.crm.freelance.model.Opportunite;
import com.crm.freelance.model.Relance;

import java.util.List;

/**
 * Conversion entités JPA -> DTOs de réponse.
 *
 * Mapping manuel volontaire : pas de dépendance externe, on voit exactement ce qui
 * est exposé. Pour un gros projet on utiliserait MapStruct (génération de code) ou
 * ModelMapper, mais ici la clarté prime.
 *
 * Classe utilitaire : constructeur privé, méthodes statiques.
 */
public final class CrmMapper {

    private CrmMapper() {
    }

    public static RelanceResponse toResponse(Relance r) {
        return new RelanceResponse(r.getId(), r.getDate(), r.getNote(), r.getStatut());
    }

    public static OpportuniteResponse toResponse(Opportunite o) {
        Contact c = o.getContact();
        List<RelanceResponse> relances = o.getRelances().stream()
                .map(CrmMapper::toResponse)
                .toList();

        return new OpportuniteResponse(
                o.getId(),
                o.getTitre(),
                o.getType(),
                o.getStatut(),
                o.getTjm(),
                o.getSalaire(),
                o.getBudget(),
                o.getDateCreation(),
                o.getDateDerniereAction(),
                o.getNote(),
                c != null ? c.getId() : null,   // on n'expose que l'id + le nom du contact,
                c != null ? c.getNom() : null,  // pas l'entité complète
                relances
        );
    }

    public static ContactResponse toResponse(Contact c) {
        List<OpportuniteResponse> opportunites = c.getOpportunites().stream()
                .map(CrmMapper::toResponse)
                .toList();

        return new ContactResponse(
                c.getId(),
                c.getNom(),
                c.getPrenom(),
                c.getEntreprise(),
                c.getEmail(),
                c.getTelephone(),
                c.getCanal(),
                opportunites
        );
    }
}
