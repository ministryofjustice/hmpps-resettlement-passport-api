drop table pathway_status;

create table pathway_status
(
    id                serial
        constraint pathway_status_pkey primary key,
    prisoner_id       integer               not null          references prisoner (id),
    pathway_id        numeric               not null          references pathway (id),
    status_id         numeric               not null          references status (id),
    creation_date     timestamp with time zone not null default now()
);
