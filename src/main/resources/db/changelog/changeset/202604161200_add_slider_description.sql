--liquibase formatted sql

--changeset annagureva:202604161200_add_slider_description

alter table sliders add column description varchar(255);