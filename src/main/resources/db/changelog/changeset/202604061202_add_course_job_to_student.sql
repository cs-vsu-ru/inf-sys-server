--liquibase formatted sql

--changeset antonkiselev:202604061202
ALTER TABLE students ADD COLUMN course_job TEXT;
ALTER TABLE students ADD COLUMN scientific_supervisor BIGINT REFERENCES employees(id)