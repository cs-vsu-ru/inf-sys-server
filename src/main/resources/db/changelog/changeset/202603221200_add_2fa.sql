--liquibase formatted sql

--changeset vladimirkattsyn:202603221200_add_two_factor_enabled
ALTER TABLE users ADD COLUMN two_factor_enabled boolean NOT NULL DEFAULT false;

--changeset vladimirkattsyn:202603221200_create_verification_codes
CREATE TABLE verification_codes
(
    email      varchar(255) PRIMARY KEY,
    code       varchar(10)  NOT NULL,
    expires_at timestamp    NOT NULL,
    created_at timestamp    NOT NULL
);