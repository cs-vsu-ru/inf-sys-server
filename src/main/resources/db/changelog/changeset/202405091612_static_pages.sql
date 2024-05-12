--liquibase formatted sql

--changeset tukitoki:202405091612_static_pages

create sequence static_pages_sequence;

create table static_pages
(
    id                   bigint not null
        primary key,
    content_about        varchar(255),
    content_confidential varchar(255),
    content_contacts     varchar(255),
    content_education    varchar(255),
    content_partners     varchar(255),
    content_students     varchar(255)
);
