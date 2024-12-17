INSERT INTO prisoner (id, noms_id, creation_date, prison_id) VALUES
    (1, 'G4161UF', '2023-08-16 12:21:38.709', 'MDI');

INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version) VALUES
    (1, 1, 'ACCOMMODATION', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'John Smith', 'Case note related to accommodation', 'USER_1', 3);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version) VALUES
    (2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2023-01-09 19:02:45.000', 'John Smith', 'Case note related to Attitudes, thinking and behaviour', 'USER_1', 1);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version) VALUES
    (3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'Joe Blogs', 'Case note related to Children, family and communities', 'USER_2', 4);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version) VALUES
    (4, 1, 'DRUGS_AND_ALCOHOL', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'DONE', '2023-01-09 19:02:45.000', 'Joe Blogs', 'Case note related to Drugs and alcohol', 'USER_2', 2);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version) VALUES
    (5, 1, 'EDUCATION_SKILLS_AND_WORK', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'Joe Blogs', 'Case note related to education, skills and work', 'USER_2', 5);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version) VALUES
    (6, 1, 'FINANCE_AND_ID', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'IN_PROGRESS', '2023-01-09 19:02:45.000', 'Joe Blogs', 'Case note related to Finance and ID', 'USER_2', 8);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version) VALUES
    (7, 1, 'HEALTH', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-01-09 19:02:45.000', 'Joe Blogs', 'Case note related to Health', 'USER_2', 1);

INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version, is_deleted) VALUES
    (8, 1, 'ACCOMMODATION', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'John Smith', 'Case note related to accommodation', 'USER_1', 13, true);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version, is_deleted) VALUES
    (9, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2023-01-09 19:02:45.000', 'John Smith', 'Case note related to Attitudes, thinking and behaviour', 'USER_1', 11, true);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version, is_deleted) VALUES
    (10, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'Joe Blogs', 'Case note related to Children, family and communities', 'USER_2', 14, true);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version, is_deleted) VALUES
    (11, 1, 'DRUGS_AND_ALCOHOL', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'DONE', '2023-01-09 19:02:45.000', 'Joe Blogs', 'Case note related to Drugs and alcohol', 'USER_2', 12, true);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version, is_deleted) VALUES
    (12, 1, 'EDUCATION_SKILLS_AND_WORK', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'SUPPORT_NOT_REQUIRED', '2023-01-09 19:02:45.000', 'Joe Blogs', 'Case note related to education, skills and work', 'USER_2', 15, true);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version, is_deleted) VALUES
    (13, 1, 'FINANCE_AND_ID', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'IN_PROGRESS', '2023-01-09 19:02:45.000', 'Joe Blogs', 'Case note related to Finance and ID', 'USER_2', 18, true);
INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id, version, is_deleted) VALUES
    (14, 1, 'HEALTH', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-01-09 19:02:45.000', 'Joe Blogs', 'Case note related to Health', 'USER_2', 11, true);