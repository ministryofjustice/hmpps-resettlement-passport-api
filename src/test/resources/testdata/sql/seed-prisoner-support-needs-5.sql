INSERT INTO prisoner (id, noms_id, creation_date, prison_id) VALUES
    (1, 'G4161UF', '2024-02-19T09:36:28.713421', 'MDI');

INSERT INTO prisoner_support_need (id, prisoner_id, support_need_id, other_detail, created_by, created_date, is_deleted, deleted_date) values
    (101, 1, 1,  null, 'Someone', '2024-02-21T09:36:28.713421', false, null),
    (102, 1, 2,  null, 'Someone', '2024-02-21T09:36:28.713421', true, '2024-02-22T09:36:28.713421'),
    (103, 1, 3,  null, 'Someone', '2024-02-21T09:36:28.713421', false, null),
    (104, 1, 5,  'Other 1', 'Someone', '2024-02-21T09:36:28.713421', false, null),
    (105, 1, 5,  'Other 2', 'Someone', '2024-02-21T09:36:28.713421', false, null),
    (106, 1, 15,  null, 'Someone', '2024-02-21T09:36:28.713421', false, null);

INSERT INTO prisoner_support_need_update (id, prisoner_support_need_id, created_by, created_date, update_text, status, is_prison, is_probation, is_deleted, deleted_date) values
    (101, 101, 'User A', '2024-02-01T09:36:32.713421', 'This is an update 1', 'NOT_STARTED', false, false, false, null),
    (102, 101, 'User A', '2024-02-01T09:36:32.713421', 'Deleted update', 'DECLINED', false, true, true, '2024-02-01T09:36:32.713421'),
    (103, 101, 'User B', '2024-02-03T09:36:32.713421', 'This is an update 2', 'IN_PROGRESS', true, false, false,  null),
    (104, 101, 'User C', '2024-02-02T09:36:32.713421', 'This is an update 3', 'MET', true, true, false,  null),
    (105, 103, 'User C', '2024-02-02T09:36:32.713421', 'This is an update 4', 'IN_PROGRESS', true, false, false,  null),
    (106, 103, 'User C', '2024-02-05T09:36:32.713421', 'This is an update 5', 'NOT_STARTED', true, true, false,  null),
    (107, 104, 'User A', '2024-02-03T10:36:32.713421', 'This is an update 6', 'MET', true, false, false,  null),
    (108, 104, 'User C', '2024-02-03T09:36:32.713421', 'This is an update 7', 'IN_PROGRESS', true, true, false,  null),
    (109, 105, 'User B', '2024-02-11T09:36:32.713421', 'This is an update 8', 'DECLINED', false, false, false,  null);

UPDATE prisoner_support_need set latest_update_id = 103 where id = 101;
UPDATE prisoner_support_need set latest_update_id = 106 where id = 103;
UPDATE prisoner_support_need set latest_update_id = 107 where id = 104;
UPDATE prisoner_support_need set latest_update_id = 109 where id = 105;