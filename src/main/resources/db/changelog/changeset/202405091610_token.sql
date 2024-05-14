--liquibase formatted sql

--changeset tukitoki:202405091610_token

create sequence token_sequence;

create table token
(
    id         bigint  not null
        primary key default nextval('token_sequence'),
    expired    boolean not null,
    revoked    boolean not null,
    token      varchar(255)
        constraint uk_pddrhgwxnms2aceeku9s2ewy5
            unique,
    token_type varchar(255)
        constraint token_token_type_check
            check ((token_type)::text = 'BEARER'::text),
    user_id    bigint
        constraint fkj8rfw4x0wjjyibfqq566j4qng
            references users
);