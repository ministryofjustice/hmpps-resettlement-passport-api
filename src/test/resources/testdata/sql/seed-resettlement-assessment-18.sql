INSERT INTO prisoner (id, noms_id, creation_date, prison_id) VALUES
    (1, 'ABC1234', '2023-08-16 12:21:38.709', 'MDI');

INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, submission_date) VALUES
    (1, 1, 'ACCOMMODATION', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1', '2023-01-09 20:02:45.000'),
    (2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1', '2023-01-09 20:02:45.000'),
    (3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1', '2023-01-09 20:02:45.000'),
    (4, 1, 'DRUGS_AND_ALCOHOL', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'DONE', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1', '2023-01-09 20:02:45.000'),
    (5, 1, 'EDUCATION_SKILLS_AND_WORK', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1', '2023-01-09 20:02:45.000'),
    (6, 1, 'FINANCE_AND_ID', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'IN_PROGRESS', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1', '2023-01-09 20:02:45.000'),
    (7, 1, 'HEALTH', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1', '2023-01-09 19:02:45.000'),
    (8, 1, 'HEALTH', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1', '2023-01-09 20:02:45.000'),
    (9, 1, 'HEALTH', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-01-09 19:02:45.000', 'A User', null, 'USER_1', '2023-01-09 21:02:45.000'),
    (10, 1, 'HEALTH', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-01-09 20:02:45.000', 'A User', null, 'USER_1', null);