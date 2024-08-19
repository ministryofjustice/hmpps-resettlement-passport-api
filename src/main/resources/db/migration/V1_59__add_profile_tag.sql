create table profile_tag
(
    id              serial constraint profile_tag_pkey primary key,
    prisoner_id      integer not null references prisoner (id),
    profile_tags     jsonb not null default '[]',
    updated_date    timestamp with time zone not null default now()
);
