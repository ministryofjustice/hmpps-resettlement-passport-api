alter table assessment add is_deleted bool not null default false;
alter table assessment add deleted_at timestamp with time zone;
