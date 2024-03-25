create table prisoner
(
    id               serial
        constraint prisoner_pkey primary key,
    noms_id          varchar(7)               not null,
    creation_date    timestamp with time zone not null default now()
);
