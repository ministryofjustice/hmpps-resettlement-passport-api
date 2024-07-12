create index prisoner_prison_id on prisoner(prison_id);
create index prisoner_release_date on prisoner(release_date);

create index pathway_status_prisoner_id on pathway_status(prisoner_id);
