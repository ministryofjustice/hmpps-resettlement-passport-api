alter table resettlement_assessment add column is_deleted boolean not null default false;
alter table resettlement_assessment add column deleted_date timestamp with time zone default null;
