CREATE TABLE assessment_skip
(
    id              serial primary key,
    prisoner_id     integer                  not null references prisoner (id),
    assessment_type varchar(50),

    reason          varchar(50),
    more_info       text,

    created_by      varchar(100)             not null,
    creation_date   timestamp with time zone not null default now()
);
