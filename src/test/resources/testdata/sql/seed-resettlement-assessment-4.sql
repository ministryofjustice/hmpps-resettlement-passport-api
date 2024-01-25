INSERT INTO prisoner (id, noms_id, creation_date, crn, prison_id, release_date) VALUES
    (1, 'ABC1234', '2023-08-16 12:21:38.709', '123', 'MDI', '2030-09-12');

INSERT INTO pathway_status (id, prisoner_id, pathway_id, status_id, updated_date) VALUES
    (1, 1, 1, 1, '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway_id, status_id, updated_date) VALUES
    (2, 1, 2, 1, '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway_id, status_id, updated_date) VALUES
    (3, 1, 3, 1, '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway_id, status_id, updated_date) VALUES
    (4, 1, 4, 1, '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway_id, status_id, updated_date) VALUES
    (5, 1, 5, 1, '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway_id, status_id, updated_date) VALUES
    (6, 1, 6, 1, '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway_id, status_id, updated_date) VALUES
    (7, 1, 7, 1, '2023-08-16 12:21:44.234');

INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (1, 1, 1, 3, 'BCST2', '{"assessment": []}'::jsonb, 3, '2023-01-09 19:02:45.000', 'A User', 'Case note related to accommodation', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (2, 1, 2, 3, 'BCST2', '{"assessment": []}'::jsonb, 4, '2023-01-09 19:02:45.000', 'A User', 'Case note related to Attitudes, thinking and behaviour', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (3, 1, 3, 3, 'BCST2', '{"assessment": []}'::jsonb, 3, '2023-01-09 19:02:45.000', 'A User', 'Case note related to Children, family and communities', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (4, 1, 4, 3, 'BCST2', '{"assessment": []}'::jsonb, 5, '2023-01-09 19:02:45.000', 'A User', 'Case note related to Drugs and alcohol', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (5, 1, 5, 3, 'BCST2', '{"assessment": []}'::jsonb, 3, '2023-01-09 19:02:45.000', 'A User', 'Case note related to education, skills and work', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (6, 1, 6, 3, 'BCST2', '{"assessment": []}'::jsonb, 2, '2023-01-09 19:02:45.000', 'A User', 'Case note related to Finance and ID', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (7, 1, 7, 3, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-01-09 19:02:45.000', 'A User', 'Case note related to Health', 'USER_1');