-- Note - temporary file to seed support needs. This file should be removed once RSP-1718 is done.
insert into support_need (id, pathway, section, title, hidden, exclude_from_count, allow_other_detail, created_date, is_deleted, deleted_date) values
  (1, 'ACCOMMODATION', 'Section A', 'Support Need 1', false, false, false, '2024-02-08T12:00:00', false, null),
  (2, 'ACCOMMODATION', 'Section A', 'Support Need 2', false, false, false, '2024-02-08T12:00:00', false, null),
  (3, 'ACCOMMODATION', 'Section A', 'Support Need 3', false, false, false, '2024-02-08T12:00:00', true, '2024-02-08T13:00:00'),
  (4, 'ACCOMMODATION', 'Section B', 'Support Need 4', true, true, false, '2024-02-08T12:00:00', false, null),
  (5, 'ACCOMMODATION', 'Section B', 'Other', false, false, true, '2024-02-08T12:00:00', false, null);