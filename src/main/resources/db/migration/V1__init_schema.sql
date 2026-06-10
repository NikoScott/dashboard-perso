-- Schéma initial du CRM Freelance
-- Flyway gère les migrations ; Hibernate est configuré en ddl-auto=none.

CREATE TABLE contact (
    id         BIGSERIAL    PRIMARY KEY,
    nom        VARCHAR(255) NOT NULL,
    prenom     VARCHAR(255),
    entreprise VARCHAR(255),
    email      VARCHAR(255),
    telephone  VARCHAR(255),
    canal      VARCHAR(50)
);

CREATE TABLE opportunite (
    id                   BIGSERIAL      PRIMARY KEY,
    titre                VARCHAR(255)   NOT NULL,
    type                 VARCHAR(50)    NOT NULL,
    statut               VARCHAR(50)    NOT NULL,
    tjm                  NUMERIC(38, 2),
    salaire              NUMERIC(38, 2),
    budget               NUMERIC(38, 2),
    date_creation        TIMESTAMP,
    date_derniere_action TIMESTAMP,
    note                 VARCHAR(2000),
    contact_id           BIGINT         REFERENCES contact (id)
);

CREATE TABLE relance (
    id             BIGSERIAL    PRIMARY KEY,
    date           TIMESTAMP    NOT NULL,
    note           VARCHAR(1000),
    statut         VARCHAR(50)  NOT NULL,
    opportunite_id BIGINT       REFERENCES opportunite (id)
);

CREATE TABLE app_user (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL DEFAULT 'USER'
);
