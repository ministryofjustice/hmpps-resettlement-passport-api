alter table licence_conditions_change_audit add column licence_conditions jsonb default '{}'::jsonb;
alter table licence_conditions_change_audit add column seen boolean not null default false;
alter table licence_conditions_change_audit drop constraint licence_conditions_change_audit_prisoner_id_key;
create index licence_conditions_change_audit_pid on licence_conditions_change_audit(prisoner_id);

create sequence license_conditions_ver_seq;
alter table licence_conditions_change_audit add column version int not null default nextval('license_conditions_ver_seq');
create unique index licence_conditions_change_audit_ver on licence_conditions_change_audit(prisoner_id, version);
alter table licence_conditions_change_audit alter column version drop default;
drop sequence license_conditions_ver_seq;
