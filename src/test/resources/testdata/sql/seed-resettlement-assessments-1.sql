INSERT INTO prisoner (id, noms_id, creation_date, crn, prison_id, release_date) VALUES
    (1, 'ABC1234', '2023-08-16 12:21:38.709', '123', 'MDI', '2030-09-12');

INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text) VALUES
    (1, 1, 1, 1, 'RESETTLEMENT_PLAN', '{}'::jsonb, NULL, '2023-01-09 19:02:45.000', 'A User', NULL);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text) VALUES
    (2, 1, 1, 1, 'RESETTLEMENT_PLAN', '{}'::jsonb, NULL, '2023-02-09 10:01:23.000', 'A User', NULL);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text) VALUES
    (3, 1, 1, 1, 'RESETTLEMENT_PLAN', '{}'::jsonb, NULL, '2023-01-28 09:56:42.000', 'A User', NULL);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text) VALUES
    (4, 1, 2, 2, 'RESETTLEMENT_PLAN', '{}'::jsonb, NULL, '2023-01-10 23:34:00.000', 'A User', NULL);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text) VALUES
    (5, 1, 3, 2, 'RESETTLEMENT_PLAN', '{}'::jsonb, NULL, '2023-01-11 21:12:23.000', 'A User', NULL);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text) VALUES
    (6, 1, 4, 1, 'RESETTLEMENT_PLAN', '{}'::jsonb, NULL, '2023-01-13 16:09:01.000', 'A User', NULL);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text) VALUES
    (7, 1, 5, 3, 'RESETTLEMENT_PLAN', '{}'::jsonb, NULL, '2023-01-12 04:32:12.000', 'A User', NULL);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text) VALUES
    (8, 1, 7, 3, 'RESETTLEMENT_PLAN', '{}'::jsonb, NULL, '2023-01-17 16:43:49.000', 'A User', NULL);