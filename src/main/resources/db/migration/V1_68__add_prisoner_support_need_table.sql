create table prisoner_support_need
(
    id              serial constraint prisoner_support_need_pkey primary key,
    prisoner_id     integer not null references prisoner (id),
    support_need_id integer not null references support_need (id),
    other_detail    text,
    created_by      varchar(200) not null,
    created_date    timestamptz not null,
    is_deleted      boolean not null,
    deleted_date    timestamptz
);
