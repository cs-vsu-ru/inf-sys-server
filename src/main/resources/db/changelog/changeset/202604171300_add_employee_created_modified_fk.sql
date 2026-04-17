--liquibase formatted sql

--changeset qudest:202604171300_employee_fk

-- Снимаем NOT NULL ограничения перед добавлением FK с ON DELETE SET NULL
ALTER TABLE employees
    ALTER COLUMN created_by_id DROP NOT NULL;

ALTER TABLE employees
    ALTER COLUMN last_modified_by_id DROP NOT NULL;

-- Добавляем внешние ключи на created_by_id и last_modified_by_id с ON DELETE SET NULL
ALTER TABLE employees
    ADD CONSTRAINT fk_employees_created_by
        FOREIGN KEY (created_by_id) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE employees
    ADD CONSTRAINT fk_employees_last_modified_by
        FOREIGN KEY (last_modified_by_id) REFERENCES users (id) ON DELETE SET NULL;
