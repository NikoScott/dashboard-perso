package com.crm.freelance.repository;

import com.crm.freelance.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * En étendant JpaRepository<Contact, Long>, on hérite GRATUITEMENT de
 * save / findById / findAll / deleteById / count... Spring génère l'implémentation
 * au démarrage : on n'écrit aucune requête pour le CRUD de base.
 */
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
