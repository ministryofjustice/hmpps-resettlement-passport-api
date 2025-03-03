INSERT INTO prisoner (id, noms_id, creation_date, prison_id) VALUES
    (1, 'ABC1234', '2023-08-16 12:21:38.709', 'MDI');

INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (1, 1, 'ACCOMMODATION', 'IN_PROGRESS', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'NOT_STARTED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'SUPPORT_REQUIRED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (4, 1, 'DRUGS_AND_ALCOHOL', 'DONE', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (5, 1, 'EDUCATION_SKILLS_AND_WORK', 'SUPPORT_NOT_REQUIRED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (6, 1, 'FINANCE_AND_ID', 'IN_PROGRESS', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES
    (7, 1, 'HEALTH', 'DONE', '2023-08-16 12:21:44.234');

INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (1, 1, 'ACCOMMODATION', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'A User', 'Case note related to accommodation', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2023-01-09 19:02:45.000', 'A User', 'Case note related to Attitudes, thinking and behaviour', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'A User', 'Case note related to Children, family and communities', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (4, 1, 'DRUGS_AND_ALCOHOL', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'DONE', '2023-01-09 19:02:45.000', 'A User', 'Case note related to Drugs and alcohol', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (5, 1, 'EDUCATION_SKILLS_AND_WORK', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'A User', 'Case note related to education, skills and work', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (6, 1, 'FINANCE_AND_ID', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'IN_PROGRESS', '2023-01-09 19:02:45.000', 'A User', 'Case note related to Finance and ID', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (7, 1, 'HEALTH', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-01-09 19:02:45.000', 'A User', 'Case note related to Health', 'USER_1');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (8, 1, 'HEALTH', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-01-09 20:02:45.000', 'B User', 'Case note related to Health', 'USER_2');
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (9, 1, 'ACCOMMODATION', 'COMPLETE', 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-12 10:23:59.000', 'C User', 'Case note related to Accommodation', 'USER_3');

INSERT INTO prisoner_support_need (id, prisoner_id, support_need_id, other_detail, created_by, created_date, is_deleted, deleted_date, latest_update_id) values
    (1, 1, 1, null, 'Someone', '2024-02-21T09:36:28.713421', true, '2024-02-21T09:37:28.713421', null),
    (2, 1, 1, null, 'Someone', '2024-02-21T09:36:28.713421', false, null, null),
    (3, 1, 7, null, 'Someone', '2024-02-21T09:36:28.713421', false, null, null);

INSERT INTO prisoner_support_need_update (id, prisoner_support_need_id, created_by, created_date, update_text, status, is_prison, is_probation, is_deleted, deleted_date) values
    (101, 2, 'A user', '2024-02-22T09:36:32.713421', 'This is an update 1', 'MET', true, true, true, null),
    (102, 2, 'A user', '2024-02-22T09:36:30.713421', 'This is an update 2', 'IN_PROGRESS', true, false, false, null);
