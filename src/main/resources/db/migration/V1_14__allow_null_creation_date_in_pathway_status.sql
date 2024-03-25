alter table pathway_status alter column updated_date drop not null;
alter table pathway_status alter column updated_date drop default;
