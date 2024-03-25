create table delius_contact
(
    id                    serial constraint delius_contact_pkey primary key,
    prisoner_id           integer not null references prisoner (id),
    category              varchar(40) not null,
    contact_type          varchar(20) not null,
    created_date          timestamp with time zone not null,
    appointment_date      timestamp with time zone,
    appointment_duration  int,
    notes                 text not null,
    created_by            varchar(200) not null
);
