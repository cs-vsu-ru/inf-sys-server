--liquibase formatted sql

--changeset qudest:202604171300_employee_fk

ALTER TABLE employees
    ADD CONSTRAINT fk_employees_created_by
        FOREIGN KEY (created_by_id) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE employees
    ADD CONSTRAINT fk_employees_last_modified_by
        FOREIGN KEY (last_modified_by_id) REFERENCES users (id) ON DELETE SET NULL;
