INSERT INTO prisoner (id, noms_id, creation_date, crn, prison_id) VALUES
    (1, 'ABC1234', '2023-08-16 12:21:38.709', '123', 'MDI');

INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (1, 1, 'ACCOMMODATION', 'NOT_STARTED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'NOT_STARTED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'NOT_STARTED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (4, 1, 'DRUGS_AND_ALCOHOL', 'NOT_STARTED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (5, 1, 'EDUCATION_SKILLS_AND_WORK', 'NOT_STARTED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (6, 1, 'FINANCE_AND_ID', 'NOT_STARTED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (7, 1, 'HEALTH', 'NOT_STARTED', '2023-08-16 12:21:44.234');

INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (1, 1, 'ACCOMMODATION', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (4, 1, 'DRUGS_AND_ALCOHOL', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'DONE', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (5, 1, 'EDUCATION_SKILLS_AND_WORK', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (6, 1, 'FINANCE_AND_ID', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'IN_PROGRESS', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (7, 1, 'HEALTH', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1');