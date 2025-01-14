create table support_need
(
    id                 serial constraint support_need_pkey primary key,
    pathway            varchar(100) not null,
    section            text,
    title              text not null,
    hidden             boolean not null,
    exclude_from_count boolean not null,
    allow_other_detail boolean not null,
    created_date       timestamptz not null,
    is_deleted         boolean not null,
    deleted_date       timestamptz
);