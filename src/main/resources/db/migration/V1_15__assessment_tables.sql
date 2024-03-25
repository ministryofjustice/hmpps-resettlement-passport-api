create table assessment
(
    id              serial constraint assessment_pkey primary key,
    prisoner_id     integer not null references prisoner (id),
    assessment_date timestamp with time zone not null,
    is_bank_account_required bool,
    is_id_required  bool,
    when_created    timestamp with time zone not null default now()
);

create table id_type
(
    id  numeric constraint id_type_pkey primary key,
    name            varchar(50)           not null
);

insert into id_type (id, name) values (1, 'Birth certificate');
insert into id_type (id, name) values (2, 'Marriage certificate');
insert into id_type (id, name) values (3, 'Civil partnership certificate');
insert into id_type (id, name) values (4, 'Adoption certificate');
insert into id_type (id, name) values (5, 'Divorce decree absolute certificate');
insert into id_type (id, name) values (6, 'Driving licence');
insert into id_type (id, name) values (7, 'Biometric residence permit');
insert into id_type (id, name) values (8, 'Deed poll certificate');

create table assessment_id_type
(
    id  serial constraint assessment_id_type_pkey primary key,
    id_type_id integer not null references id_type (id),
    assessment_id integer not null references assessment (id)
);
