create table resettlement_assessment
(
    id                    serial constraint resettlement_assessment_pkey primary key,
    prisoner_id           integer not null references prisoner (id),
    pathway_id            integer not null references pathway (id),
    assessment_type       varchar(50) not null,
    assessment            jsonb,
    status_changed_to_status_id integer references status (id),
    created_date          timestamp with time zone not null,
    created_by            varchar(200) not null
);