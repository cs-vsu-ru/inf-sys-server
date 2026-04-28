--liquibase formatted sql

--changeset qudest:202603221201_data_navigation_tabs context:local

INSERT INTO navigation_tabs (name, url, sort_order, visible)
VALUES ('О кафедре', '/about', 1, TRUE),
       ('Образование', '/education', 2, TRUE),
       ('Расписание', '/full-schedule', 3, TRUE),
       ('Учебный процесс', '/exams', 4, TRUE),
       ('Студентам', '/students', 5, TRUE),
       ('Сотрудники', '/teachers', 6, TRUE),
       ('Важное', '/important', 7, TRUE),
       ('Разное', '/miscellaneous', 8, TRUE);
