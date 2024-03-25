
create table bank_application
(
    id              serial constraint bank_application_pkey primary key,
    prisoner_id     integer not null references prisoner (id),
    application_submitted_date timestamp with time zone not null,
    bank_response_date timestamp with time zone,
    status varchar(50),
    added_to_personal_items  bool,
    added_to_personal_items_date timestamp with time zone,
    when_created    timestamp with time zone not null default now(),
    is_deleted bool default false,
    deleted_at timestamp with time zone
);

create table id_application
(
    id              serial constraint id_application_pkey primary key,
    prisoner_id     integer not null references prisoner (id),
    application_submitted_date timestamp with time zone not null,
    id_type_id integer not null references id_type (id),
    cost_of_application numeric,
    have_gro bool,
    uk_national_born_overseas bool,
    country_born_in varchar(250),
    case_number varchar(250),
    court_details varchar(250),
    drivers_licence_type varchar(25),
    drivers_licence_application_made_at varchar(50),
    priority_application bool,
    status varchar(50) default 'pending',
    status_update_date timestamp with time zone,
    added_to_personal_items bool,
    added_to_personal_items_date timestamp with time zone,
    refund_amount numeric,
    when_created    timestamp with time zone not null default now(),
    is_deleted bool default false,
    deleted_at timestamp with time zone
);

create table bank_application_status_log
(
    id serial constraint bank_application_status_log_pkey primary key,
    bank_application_id integer references bank_application (id),
    status_changed_to varchar(50),
    changed_at    timestamp with time zone not null default now()
);

ALTER TABLE prisoner ADD UNIQUE (noms_id);
