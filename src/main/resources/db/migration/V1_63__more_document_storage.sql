alter table document_location add column is_deleted boolean not null default false;
alter table document_location add column deleted_date timestamp with time zone default null;
