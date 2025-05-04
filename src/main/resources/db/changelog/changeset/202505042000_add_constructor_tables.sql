--liquibase formatted sql

--changeset dmitrys:202505042000_add_constructor_tables

-- SEQUENCES
create sequence pages_sequence increment by 1 start with 1;
create sequence page_blocks_sequence increment by 1 start with 1;
create sequence page_elements_sequence increment by 1 start with 1;

-- TABLE: pages
create table pages (
    id     bigint       not null primary key default nextval('pages_sequence'),
    name   varchar(255) not null unique,
    title  varchar(255)
);

-- TABLE: page_blocks
create table page_blocks (
    id            bigint       not null primary key default nextval('page_blocks_sequence'),
    type          varchar(255),
    elements_type jsonb        not null,
    needs_image   boolean,
    position      integer,
    page_id       bigint       not null,
    constraint fk_page_block_page
        foreign key (page_id)
            references pages(id)
            on delete cascade
);

-- INDEX on foreign key
create index idx_page_blocks_page_id on page_blocks(page_id);

-- TABLE: page_elements
create table page_elements (
    id        bigint       not null primary key default nextval('page_elements_sequence'),
    value     jsonb        not null,
    position  integer,
    block_id  bigint       not null,
    constraint fk_page_element_block
        foreign key (block_id)
            references page_blocks(id)
            on delete cascade
);

-- INDEX on foreign key
create index idx_page_elements_block_id on page_elements(block_id);