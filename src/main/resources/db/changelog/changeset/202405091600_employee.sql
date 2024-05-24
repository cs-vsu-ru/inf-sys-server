--liquibase formatted sql

--changeset tukitoki:202405091600_employee

create sequence employee_sequence;

create table employees
(
    id                      bigint  not null
        primary key default nextval('employee_sequence'),
    has_lessons             boolean not null,
    created_at              timestamp(6),
    user_id                 bigint  not null
        unique
        constraint fk69x3vjuy1t5p18a5llb8h2fjx
            references users,
    image_url               varchar(255),
    academic_degree         varchar(255),
    academic_title          varchar(255),
    experience              date,
    patronymic              varchar(255),
    plan                    varchar(255),
    post                    varchar(255),
    professional_experience date,
    last_modified_at        timestamp(6),
    last_modified_by_id     bigint  not null,
    created_by_id           bigint  not null
);