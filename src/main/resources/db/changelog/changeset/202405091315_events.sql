--liquibase formatted sql

--changeset tukitoki:202405091317_events

create sequence events_sequence;

create table events
(
    id                      bigint       not null
        primary key default nextval('events_sequence'),
    content                 varchar(255) not null,
    end_date_time           timestamp(6),
    last_modified_date_time timestamp(6),
    publication_date_time   timestamp(6),
    start_date_time         timestamp(6),
    title                   varchar(255) not null
);