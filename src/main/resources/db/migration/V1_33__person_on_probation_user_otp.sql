create table person_on_probation_user_otp
(
    id               serial
        constraint person_on_probation_user_otp_pkey primary key,
    prisoner_id       integer               not null          references prisoner (id),
    otp               numeric               not null,
    expiry_date     timestamp with time zone not null default now(),
    creation_date    timestamp with time zone not null default now()
);