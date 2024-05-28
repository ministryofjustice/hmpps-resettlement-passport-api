CREATE TABLE licence_conditions_change_audit
(
    id              serial primary key,
    prisoner_id           integer not null references prisoner (id),
    licence_conditions_json     text,
    creation_date   timestamp with time zone not null default now()
);
