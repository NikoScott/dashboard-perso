package com.crm.freelance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${crm.email.destinataire}")
    private String destinataire;

    /**
     * Envoie une notification quand une relance est ajoutée sur une opportunité.
     * L'échec d'envoi est loggé sans propager d'exception : la relance est quand même
     * persistée en base, l'email est "best effort".
     */
    public void envoyerNotificationRelance(String titreOpportunite, String contactNom, String note) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject("Relance ajoutée – " + titreOpportunite);
            message.setText(
                    "Une relance a été enregistrée pour l'opportunité : " + titreOpportunite
                    + "\nContact : " + contactNom
                    + (note != null && !note.isBlank() ? "\nNote : " + note : "")
            );
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Impossible d'envoyer l'email de notification pour '{}' : {}", titreOpportunite, e.getMessage());
        }
    }
}
