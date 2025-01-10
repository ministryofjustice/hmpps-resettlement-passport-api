create table prisoner_support_need_update
(
    id                       serial constraint prisoner_support_need_update_pkey primary key,
    prisoner_support_need_id integer not null references prisoner (id),
    created_by               varchar(200) not null,
    created_date             timestamptz not null,
    update_text              text,
    status                   varchar(100),
    is_prison                boolean not null,
    is_probation             boolean not null,
    is_deleted               boolean not null,
    deleted_date             timestamptz
);