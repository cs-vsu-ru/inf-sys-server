--liquibase formatted sql

--changeset tukitoki:202405091555_sliders

create sequence slider_sequence;

create table sliders
(
    id         bigint not null
        primary key default nextval('slider_sequence'),
    create_at  timestamp(6),
    updated_at timestamp(6),
    imageurl   varchar(255),
    title      varchar(255),
    url_to     varchar(255)
);
