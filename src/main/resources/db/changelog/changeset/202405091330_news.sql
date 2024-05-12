--liquibase formatted sql

--changeset tukitoki:202405091330_news

create sequence news_sequence;

create table news
(
    id             bigint       not null
        primary key default nextval('news_sequence'),
    content        varchar(255) not null,
    image_link     varchar(255) not null,
    publication_at timestamp(6),
    title          varchar(255) not null,
    updated_at     timestamp(6)
);