INSERT INTO prisoner_support_need (id, prisoner_id, support_need_id, other_detail, created_by, created_date, is_deleted, deleted_date) values
    (1, 1, 1, null, 'Someone', '2024-02-21T09:36:28.713421', true, '2024-02-21T09:37:28.713421'),
    (2, 1, 1, null, 'Someone', '2024-02-21T09:36:28.713421', false, null),
    (3, 1, 2, null, 'Someone', '2024-02-21T09:36:28.713421', false, null),
    (6, 1, 5, 'This is an other 1', 'Someone else', '2024-02-21T09:36:28.713421', false, null),
    (7, 1, 5, 'This is an other 2', 'Someone', '2024-02-21T09:36:28.713421', false, null);

INSERT INTO prisoner_support_need_update (id, prisoner_support_need_id, created_by, created_date, update_text, status, is_prison, is_probation, is_deleted, deleted_date) values
    (1, 2, 'A user', '2024-12-22T09:36:32.713421', 'This is an update 1', 'NOT_STARTED', true, false, true, null),
    (2, 2, 'A user', '2024-12-23T09:36:30.713421', 'This is an update 2', 'IN_PROGRESS', true, false, false, null),
    (3, 2, 'A user', '2024-12-25T09:36:31.713421', 'This is an update 3', 'MET', true, false, false, null),
    (4, 2, 'A user', '2024-12-24T09:36:29.713421', 'This is an update 4', 'DECLINED', true, false, false, null);