# CRM Freelance

Mini-CRM pour suivre ses opportunités commerciales en freelance : contacts,
opportunités (missions / CDI / sites web), relances et statistiques.

Projet **Spring Boot 3 / Java 17**, architecture en couches, API REST, base H2 en mémoire.

---

## Lancer le projet

Prérequis : **JDK 17**.

```bash
# Si le JDK n'est pas dans le PATH (ex. installé via Homebrew) :
export JAVA_HOME=/opt/homebrew/opt/openjdk@17

# Lancer l'application
./mvnw spring-boot:run

# Lancer les tests
./mvnw test
```

- API : http://localhost:8080
- Console H2 : http://localhost:8080/h2-console (JDBC URL : `jdbc:h2:mem:crmdb`, user `sa`, pas de mot de passe)

---

## Modèle de données

```
Contact (1) ───< (N) Opportunite (1) ───< (N) Relance

Contact       : nom*, prénom, entreprise, email, téléphone, canal
Opportunite   : titre*, type*, statut*, tjm | salaire | budget,
                dateCreation, dateDerniereAction, note, contact
Relance       : date, note, statut, opportunite
```

Enums : `Canal`, `TypeOpportunite` (MISSION_FREELANCE / CDI / SITE_WEB),
`StatutOpportunite` (CONTACTE → … → GAGNE / PERDU), `StatutRelance`.

---

## Architecture en couches

```
controller/   reçoit le HTTP, valide (@Valid), délègue — aucune logique métier
service/      TOUTE la logique métier vit ici
repository/   accès base (Spring Data JPA)
model/        entités JPA + règles portées par l'entité (estARelancer, montantPipeline)
dto/          objets d'entrée/sortie de l'API (records)
mapper/       conversion entité → DTO
exception/    gestion centralisée des erreurs (@RestControllerAdvice)
```

---

## Endpoints REST

| Méthode | URL | Rôle |
|--------|-----|------|
| POST | `/contacts` | créer un contact |
| GET | `/contacts` | lister les contacts |
| GET | `/contacts/{id}` | détail + opportunités |
| PUT | `/contacts/{id}` | modifier |
| DELETE | `/contacts/{id}` | supprimer (cascade sur opportunités) |
| POST | `/opportunites` | créer une opportunité |
| GET | `/opportunites?statut=&type=` | lister avec filtres optionnels |
| PUT | `/opportunites/{id}` | modifier |
| PUT | `/opportunites/{id}/statut` | changer le statut uniquement |
| GET | `/opportunites/a-relancer` | opportunités sans action depuis N jours |
| POST | `/opportunites/{id}/relances` | ajouter une relance |
| GET | `/opportunites/{id}/relances` | historique des relances |
| GET | `/stats` | indicateurs globaux |

---

## Logique métier clé

- **Détection « à relancer »** : opportunité **non terminale** (ni GAGNE ni PERDU)
  dont la `dateDerniereAction` est antérieure à `now - délai`. Le délai est
  externalisé (`crm.relance.delai-jours`, défaut 7) pour éviter un nombre magique.
- **Mise à jour automatique de `dateDerniereAction`** à chaque relance ajoutée et à
  chaque changement de statut → l'opportunité sort de la liste « à relancer ».
- **Pipeline** = somme des montants (TJM / salaire / budget selon le type) des
  opportunités non terminées.

La règle « à relancer » est portée par l'entité (`Opportunite.estARelancer`), donc
**pure et testable sans base de données**.

---

## Choix techniques justifiés

**Pourquoi des DTOs séparés des entités JPA ?**
L'entité représente la table en base (annotations `@Entity`, relations `@ManyToOne`…).
L'exposer directement risque des chargements en cascade non voulus et des boucles
(Contact → opportunités → contact → …). Le DTO expose **exactement** ce dont l'API a
besoin : par exemple une opportunité renvoie `contactId` + `contactNom`, pas l'objet
Contact complet.

**Pourquoi H2 en mémoire ?**
Base recréée à chaque démarrage (`create-drop`) : zéro configuration, idéal pour le
développement et les tests. À remplacer par PostgreSQL en production.

**Pourquoi un seul enum de statut (vs héritage) ?**
Tous les statuts ne sont pas pertinents pour tous les types (pas de `DEVIS_ENVOYE`
pour un CDI). Compromis volontaire : un enum unique pour la simplicité et la
lisibilité. L'alternative « propre » serait un statut par type (héritage ou table de
statuts) — évolution possible.

**Injection par constructeur** (`@RequiredArgsConstructor` sur champs `final`) :
dépendances immuables, faciles à mocker en test, pas de `@Autowired` sur les champs.

---

## Tests

6 tests unitaires (Mockito) ciblant la **logique métier**, pas le CRUD :

- `OpportuniteServiceTest` — détection des opportunités à relancer (seuil de date),
  exclusion des statuts terminaux, mise à jour de `dateDerniereAction` à l'ajout d'une
  relance, changement de statut.
- `ContactServiceTest` — création refusée si champ obligatoire manquant, création
  valide renvoyant l'id.

---

## Ce qui manquerait en production

- **Authentification** : Spring Security + JWT.
- **Base persistante** : PostgreSQL (au lieu de H2 en mémoire).
- **Envoi d'email réel** pour les relances (JavaMail / SMTP).
- **Pagination** sur les listes (`Pageable`).
- Migrations de schéma versionnées (Flyway / Liquibase) au lieu de `create-drop`.
