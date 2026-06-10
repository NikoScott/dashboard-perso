package com.crm.freelance.service;

import com.crm.freelance.dto.ContactRequest;
import com.crm.freelance.dto.ContactResponse;
import com.crm.freelance.exception.ResourceNotFoundException;
import com.crm.freelance.mapper.CrmMapper;
import com.crm.freelance.model.Contact;
import com.crm.freelance.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    @Transactional
    public ContactResponse creer(ContactRequest req) {
        validerObligatoires(req);
        Contact contact = Contact.builder()
                .nom(req.nom())
                .prenom(req.prenom())
                .entreprise(req.entreprise())
                .email(req.email())
                .telephone(req.telephone())
                .canal(req.canal())
                .build();
        return CrmMapper.toResponse(contactRepository.save(contact));
    }

    @Transactional(readOnly = true)
    public Page<ContactResponse> lister(Pageable pageable) {
        return contactRepository.findAll(pageable).map(CrmMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ContactResponse detail(Long id) {
        return CrmMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public ContactResponse modifier(Long id, ContactRequest req) {
        validerObligatoires(req);
        Contact contact = getOrThrow(id);
        contact.setNom(req.nom());
        contact.setPrenom(req.prenom());
        contact.setEntreprise(req.entreprise());
        contact.setEmail(req.email());
        contact.setTelephone(req.telephone());
        contact.setCanal(req.canal());
        return CrmMapper.toResponse(contactRepository.save(contact));
    }

    @Transactional
    public void supprimer(Long id) {
        Contact contact = getOrThrow(id);
        contactRepository.delete(contact);
    }

    private Contact getOrThrow(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Contact", id));
    }

    private void validerObligatoires(ContactRequest req) {
        if (req.nom() == null || req.nom().isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
    }
}
