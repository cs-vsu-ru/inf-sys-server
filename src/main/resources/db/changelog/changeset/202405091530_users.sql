--liquibase formatted sql

--changeset tukitoki:202405091530_users

create sequence users_sequence;

create table users
(
    id         bigint
        primary key default nextval('users_sequence'),
    email      varchar(255),
    first_name varchar(255),
    last_name  varchar(255),
    login      varchar(255),
    password   varchar(255),
    role       varchar(255)
        constraint users_role_check
            check ((role)::text = ANY
        ((ARRAY ['USER':: character varying, 'MODERATOR':: character varying, 'ADMIN':: character varying])::text[])
)
    );

create unique index uniq_users_email on users (email);