create table assessment_status
(
    id              numeric
        constraint assessment_status_pkey primary key,
    name            varchar(30)           not null,
    active          bool                  not null default true,
    creation_date    timestamp with time zone not null default now()
);

insert into assessment_status (id, name, active ) values (1, 'Not Started', true);
insert into assessment_status (id, name, active ) values (2, 'In Progress', true);
insert into assessment_status (id, name, active ) values (3, 'Complete', true);

drop table resettlement_assessment;

create table resettlement_assessment
(
    id                    serial constraint resettlement_assessment_pkey primary key,
    prisoner_id           integer not null references prisoner (id),
    pathway_id            integer not null references pathway (id),
    assessment_status_id  numeric not null references assessment_status (id),
    assessment_type       varchar(50) not null,
    assessment            jsonb,
    status_changed_to_status_id integer references status (id),
    created_date          timestamp with time zone not null,
    created_by            varchar(200) not null

);