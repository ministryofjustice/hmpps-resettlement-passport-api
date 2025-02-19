INSERT INTO prisoner (id, noms_id, creation_date, prison_id) VALUES
    (1, 'G4161UF', '2024-02-19T09:36:28.713421', 'MDI'),
    (2, 'G4161UG', '2024-02-20T09:36:28.713421', 'MDI');

INSERT INTO prisoner_support_need (id, prisoner_id, support_need_id, other_detail, created_by, created_date, is_deleted, deleted_date, latest_update_id) values
    (1, 1, 1, null, 'Someone', '2024-02-21T09:36:28.713421', true, '2024-02-21T09:37:28.713421', null),
    (2, 1, 1, null, 'Someone', '2024-02-21T09:36:28.713421', false, null, null),
    (3, 1, 7, null, 'Someone', '2024-02-21T09:36:28.713421', false, null, null),
    (5, 2, 6, 'This is an other', 'Someone else', '2024-02-21T09:36:28.713421', false, null, null),
    (6, 1, 6, 'This is an other 1', 'Someone else', '2024-02-21T09:36:28.713421', false, null, null);

INSERT INTO prisoner_support_need_update (id, prisoner_support_need_id, created_by, created_date, update_text, status, is_prison, is_probation, is_deleted, deleted_date) values
    (1, 2, 'A user', '2024-02-22T09:36:32.713421', 'This is an update 1', 'MET', true, true, true, null),
    (2, 2, 'A user', '2024-02-22T09:36:30.713421', 'This is an update 2', 'IN_PROGRESS', true, false, false, null),
    (3, 2, 'A user', '2024-02-22T09:36:31.713421', 'This is an update 3', 'MET', false, true, false, null),
    (4, 2, 'A user', '2024-02-22T09:36:29.713421', 'This is an update 4', 'DECLINED', false, false, false, null);

UPDATE prisoner_support_need set latest_update_id = 3 where id = 2;