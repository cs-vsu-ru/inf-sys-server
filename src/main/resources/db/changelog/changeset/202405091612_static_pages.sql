--liquibase formatted sql

--changeset tukitoki:202405091612_static_pages

create sequence static_pages_sequence;

create table static_pages
(
    id                   bigint not null
        primary key,
    content_about        text,
    content_confidential text,
    content_contacts     text,
    content_education    text,
    content_partners     text,
    content_students     text
);
