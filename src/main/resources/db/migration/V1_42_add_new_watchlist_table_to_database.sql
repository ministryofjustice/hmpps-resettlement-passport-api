CREATE TABLE watchlist (
                           id              primary key,
                           prisoner_id     integer not null references prisoner (id),
                           staff_user_id   text
);