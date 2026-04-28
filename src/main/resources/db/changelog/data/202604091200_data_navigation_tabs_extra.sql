--liquibase formatted sql

--changeset nagorniy:202604091200_data_navigation_tabs_extra context:local

INSERT INTO navigation_tabs (name, url, sort_order, visible)
VALUES ('Адреса и контакты', '/contacts', 9, FALSE),
       ('Политика конфиденциальности', '/confident', 10, FALSE);
