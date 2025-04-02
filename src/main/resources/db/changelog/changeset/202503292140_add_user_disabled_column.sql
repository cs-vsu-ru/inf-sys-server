--liquibase formatted sql

--changeset dmitrys:202503292140_add_user_disabled_column.sql

ALTER TABLE employees ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;