CREATE TABLE document_location (
                           id              serial constraint document_location_pkey primary key,
                           prisoner_id     integer not null references prisoner (id),
                           document_key  varchar(100) not null,
                           creation_date   timestamp with time zone not null default now()

);