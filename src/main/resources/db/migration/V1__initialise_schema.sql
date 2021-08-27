create schema if not exists example;
set schema 'example';

create table pet
(
    id   serial primary key,
    name text not null
)
