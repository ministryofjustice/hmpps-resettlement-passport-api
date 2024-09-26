create table todo_item
(
    id             uuid primary key,
    prisoner_id    bigint                   not null references prisoner (id),

    title          text,
    notes          text,
    due_date       date,
    completed      boolean,

    created_by_urn varchar(255),
    updated_by_urn varchar(255),
    creation_date  timestamp with time zone not null default now(),
    updated_at     timestamp with time zone not null default now()
);

create index todo_prisoner_id on todo_item (prisoner_id);
