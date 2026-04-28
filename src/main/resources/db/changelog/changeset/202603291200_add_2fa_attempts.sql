--liquibase formatted sql

--changeset vladimirkattsyn:202603291200_add_2fa_attempts
ALTER TABLE verification_codes ADD COLUMN attempt_count integer NOT NULL DEFAULT 0;
ALTER TABLE verification_codes ADD COLUMN blocked_until timestamp;