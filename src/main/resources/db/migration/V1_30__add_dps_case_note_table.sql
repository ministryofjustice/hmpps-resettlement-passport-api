create table dps_case_note
(
    id                    serial constraint dps_case_note_pkey primary key,
    prisoner_id           integer not null references prisoner (id),
    pathway_id            integer not null references pathway (id),
    created_date          timestamp with time zone not null,
    notes                 text not null,
    created_by            varchar(200) not null
);