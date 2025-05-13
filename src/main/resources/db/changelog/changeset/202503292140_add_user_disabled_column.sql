--liquibase formatted sql

--changeset dmitrys:202503292140_add_user_disabled_column.sql
ALTER TABLE employees ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

--changeset dmitrys:202503292140_rename_is_deleted_to_is_disabled
ALTER TABLE employees RENAME COLUMN is_deleted TO is_disabled;