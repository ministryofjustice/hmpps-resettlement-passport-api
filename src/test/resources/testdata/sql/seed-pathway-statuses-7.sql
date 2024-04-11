INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(1, 'G4274GN', '2023-08-16 12:21:38.709', '123', 'MDI', '2030-09-12');

INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(1, 1, 1, 1, '2023-08-16 12:21:44.234');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(2, 1, 2, 2, '2023-08-17 12:28:10.466');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(3, 1, 3, 3, '2023-08-17 12:28:10.475');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(4, 1, 4, 4, '2023-08-17 12:28:10.479');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(5, 1, 5, 5, '2023-08-17 12:28:10.483');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(6, 1, 6, 2, '2023-08-17 12:28:10.490');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(7, 1, 7, 3, '2023-08-17 12:28:10.493');

INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(1, 1, 'ACCOMMODATION', 'CASE_NOTE', '2023-12-05 17:44:30.091', NULL, NULL, 'This is some case note text.', 'John Williams');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'CASE_NOTE', '2023-11-05 15:02:23.081', NULL, NULL, 'Some text related to case note.', 'Simon Johnson');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'CASE_NOTE', '2023-10-01 09:53:01.001', NULL, NULL, 'Random string.', 'Julie Davis');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(4, 1, 'DRUGS_AND_ALCOHOL', 'CASE_NOTE', '2021-09-30 11:13:59.673', NULL, NULL, 'Some text. Some text. Some text.', 'System User');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(5, 1, 'EDUCATION_SKILLS_AND_WORK', 'CASE_NOTE', '2022-02-12 21:01:11.234', NULL, NULL, 'Insert case note information here', 'Probation User 1');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(6, 1, 'FINANCE_AND_ID', 'CASE_NOTE', '2023-12-08 16:33:09.109', NULL, NULL, 'text text text', 'Probation User 2');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(7, 1, 'HEALTH', 'CASE_NOTE', '2020-01-09 02:09:23.476', NULL, NULL, 'string test string', 'Jim White');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(8, 1, 'HEALTH', 'CASE_NOTE', '2020-01-09 02:09:23.476', NULL, NULL, 'string test string - another health case note', 'Liz Murphy');

-- Duplicates which should all be filtered out
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(9, 1, 'ACCOMMODATION', 'CASE_NOTE', '2023-12-05 17:44:30.091', NULL, NULL, 'This is some case note text.', 'John Williams');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(10, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'CASE_NOTE', '2023-11-05 10:00:00.001', NULL, NULL, 'Some text related to case note.', 'Simon Johnson');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(11, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'CASE_NOTE', '2023-10-01 09:53:01.001', NULL, NULL, 'Random string.', 'Julie Davis');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(12, 1, 'DRUGS_AND_ALCOHOL', 'CASE_NOTE', '2021-09-30 12:00:00', NULL, NULL, 'Some text. Some text. Some text.', 'System User');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(13, 1, 'EDUCATION_SKILLS_AND_WORK', 'CASE_NOTE', '2022-02-12 22:00:00', NULL, NULL, 'Insert case note information here', 'Probation User 1');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(14, 1, 'FINANCE_AND_ID', 'CASE_NOTE', '2023-12-08 16:33:09.109', NULL, NULL, 'text text text', 'Probation User 2');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(15, 1, 'HEALTH', 'CASE_NOTE', '2020-01-09 02:09:23.476', NULL, NULL, 'string test string', 'Jim White');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(16, 1, 'HEALTH', 'CASE_NOTE', '2020-01-09 08:02:02', NULL, NULL, 'string test string - another health case note', 'Liz Murphy');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(17, 1, 'ACCOMMODATION', 'CASE_NOTE', '2023-09-05 08:02:02', NULL, NULL, 'Test case note', 'James Smith');
