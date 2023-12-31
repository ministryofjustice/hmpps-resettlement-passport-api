create table status
(
    id              numeric
                    constraint status_pkey primary key,
    name            varchar(30)           not null,
    active          bool                  not null default true,
    when_created    timestamp with time zone not null default now()
);
