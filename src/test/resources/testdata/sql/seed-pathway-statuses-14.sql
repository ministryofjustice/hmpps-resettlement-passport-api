INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id)
VALUES(1, 'G4274GN', '2023-08-16 12:21:38.709', '123', 'MDI');

INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(1, 1, 'ACCOMMODATION', 'NOT_STARTED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'IN_PROGRESS', '2023-08-17 12:28:10.466');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'SUPPORT_NOT_REQUIRED', '2023-08-17 12:28:10.475');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(4, 1, 'DRUGS_AND_ALCOHOL', 'SUPPORT_DECLINED', '2023-08-17 12:28:10.479');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(5, 1, 'EDUCATION_SKILLS_AND_WORK', 'DONE', '2023-08-17 12:28:10.483');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(6, 1, 'FINANCE_AND_ID', 'IN_PROGRESS', '2023-08-17 12:28:10.490');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(7, 1, 'HEALTH', 'SUPPORT_NOT_REQUIRED', '2023-08-17 12:28:10.493');

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

INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, is_deleted, deleted_date) VALUES
(1, 1, 'ACCOMMODATION', 'SUBMITTED', 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2024-07-30 19:02:45.000', 'A User', 'Some accom case notes 1', 'STURNER_GEN', false, null),
(2, 1, 'ACCOMMODATION', 'SUBMITTED', 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2024-07-30 19:02:45.000', 'A User', 'Some accom case notes 2', 'STURNER_GEN', false, null),
(3, 1, 'ACCOMMODATION', 'SUBMITTED', 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2024-07-30 19:02:45.000', 'A User', 'Some accom case notes 3', 'STURNER_GEN', false, null),
(4, 1, 'ACCOMMODATION', 'SUBMITTED', 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2024-07-30 19:02:45.000', 'A User', 'Some accom case notes 4', 'STURNER_GEN', true, '2024-07-30 19:02:45.000'),
(5, 1, 'ACCOMMODATION', 'SUBMITTED', 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2024-07-30 19:02:45.000', 'A User', 'Some accom case notes 5', 'STURNER_GEN', true, '2024-07-30 19:02:45.000'),
(6, 1, 'ACCOMMODATION', 'SUBMITTED', 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2024-07-30 19:02:45.000', 'A User', 'Some accom case notes 6', 'STURNER_GEN', true, '2024-07-30 19:02:45.000'),
(7, 1, 'ACCOMMODATION', 'SUBMITTED', 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2024-07-30 19:02:45.000', 'A User', 'Some accom case notes 7', 'STURNER_GEN', true, '2024-07-30 19:02:45.000')