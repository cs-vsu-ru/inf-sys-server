--liquibase formatted sql

--changeset nagorniy:202604171200_add_student_disabled_column
ALTER TABLE students ADD COLUMN is_disabled BOOLEAN NOT NULL DEFAULT FALSE;