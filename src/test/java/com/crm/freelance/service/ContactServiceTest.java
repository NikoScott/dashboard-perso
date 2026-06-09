package com.crm.freelance.service;

import com.crm.freelance.dto.ContactRequest;
import com.crm.freelance.dto.ContactResponse;
import com.crm.freelance.model.Canal;
import com.crm.freelance.model.Contact;
import com.crm.freelance.repository.ContactRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests UNITAIRES de la logique métier des contacts.
 * On vérifie la garde métier (nom obligatoire) et le cas nominal de création.
 */
@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock private ContactRepository contactRepository;

    @InjectMocks private ContactService contactService;

    @Test
    @DisplayName("Créer un contact sans nom lève une IllegalArgumentException (garde métier)")
    void creationSansNomLeveUneException() {
        ContactRequest sansNom = new ContactRequest(null, "Jean", "ACME", "jean@acme.fr", "0600000000", Canal.LINKEDIN);

        // La validation @NotBlank du DTO est court-circuitée en appel direct :
        // le service refuse quand même via sa propre garde. On teste cette garde.
        assertThatThrownBy(() -> contactService.creer(sansNom))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom");
    }

    @Test
    @DisplayName("Créer un contact valide renvoie le contact avec son id")
    void creationValideRenvoieLeContactAvecId() {
        ContactRequest req = new ContactRequest("Durand", "Marie", "ACME", "marie@acme.fr", "0611111111", Canal.RECOMMANDATION);

        // Le repository simule la persistance : il attribue l'id 42 comme le ferait la base.
        when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(42L);
            return c;
        });

        ContactResponse res = contactService.creer(req);

        assertThat(res.id()).isEqualTo(42L);
        assertThat(res.nom()).isEqualTo("Durand");
        assertThat(res.canal()).isEqualTo(Canal.RECOMMANDATION);
    }
}
