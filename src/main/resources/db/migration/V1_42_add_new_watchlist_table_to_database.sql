CREATE TABLE watchlist (
                           id              serial constraint watchlist_pkey primary key,
                           prisoner_id     integer not null references prisoner (id),
                           staff_username  varchar(100),
                           creation_date   timestamp with time zone not null default now()

);