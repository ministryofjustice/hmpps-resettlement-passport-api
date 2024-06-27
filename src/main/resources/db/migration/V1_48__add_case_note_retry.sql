CREATE TABLE case_note_retry
(
    id                       serial primary key,
    prisoner_id              integer not null references prisoner (id),
    type                     varchar(50) not null,
    notes                    text not null,
    author                   varchar(100) not null,
    prison_code              varchar(10) not null,
    original_submission_date timestamp with time zone not null,
    retry_count              integer not null,
    next_runtime             timestamp with time zone not null
);
