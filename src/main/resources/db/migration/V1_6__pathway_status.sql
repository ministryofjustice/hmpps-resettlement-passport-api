create table pathway_status
(
    id              numeric  PRIMARY KEY
        constraint pathway_status_pkey primary key,
    pathway_id        numeric               not null,
    status_id         numeric               not null,
    when_created    timestamp with time zone not null default now()
);

Drop Table offender_info;