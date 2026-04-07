--liquibase formatted sql

--changeset antonkiselev:202604061201
ALTER TABLE students ADD COLUMN department_id BIGINT REFERENCES department(id)