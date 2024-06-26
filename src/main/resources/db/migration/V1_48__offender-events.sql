create table offender_event
(
    id            uuid                     not null primary key,

    prisoner_id   bigint                   not null references prisoner (id),
    noms_id       varchar(7)               not null,
    occurred_at   timestamp with time zone not null,
    type          varchar(50)              not null,

    reason        varchar(50),
    reason_code   varchar(50),

    creation_date timestamp with time zone not null default now()
);
