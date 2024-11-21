CREATE TABLE case_allocation
(
    id                              serial primary key,
    prisoner_id                     integer not null references prisoner (id),
    staff_id                        integer not null,
    staff_firstname                 varchar(100) not null,
    staff_lastname                  varchar(100) not null,
    is_deleted                      boolean not null default false,
    when_created                    timestamp with time zone not null default now(),
    deleted_at                      timestamp with time zone
);
