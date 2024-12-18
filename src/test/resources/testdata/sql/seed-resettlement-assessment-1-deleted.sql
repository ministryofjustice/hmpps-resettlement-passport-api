INSERT INTO prisoner
(id, noms_id, creation_date)
VALUES
(1, 'G4161UF', '2023-10-16 12:21:38.709');

INSERT INTO resettlement_assessment
(id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by)
VALUES
    (1, 1, 'ACCOMMODATION', 'NOT_STARTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer'),
    (2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'NOT_STARTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer'),
    (3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer'),
    (4, 1, 'DRUGS_AND_ALCOHOL', 'NOT_STARTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer'),
    (5, 1, 'EDUCATION_SKILLS_AND_WORK', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer'),
    (6, 1, 'FINANCE_AND_ID', 'NOT_STARTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer'),
    (7, 1, 'HEALTH', 'NOT_STARTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer');

INSERT INTO resettlement_assessment
(id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, is_deleted, deleted_date)
VALUES
    (8, 1, 'ACCOMMODATION', 'NOT_STARTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer', true, '2023-10-16 12:21:38.709'),
    (9, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'NOT_STARTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer', true, '2023-10-16 12:21:38.709'),
    (10, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer', true, '2023-10-16 12:21:38.709'),
    (11, 1, 'DRUGS_AND_ALCOHOL', 'NOT_STARTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer', true, '2023-10-16 12:21:38.709'),
    (12, 1, 'EDUCATION_SKILLS_AND_WORK', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer', true, '2023-10-16 12:21:38.709'),
    (13, 1, 'FINANCE_AND_ID', 'NOT_STARTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer', true, '2023-10-16 12:21:38.709'),
    (14, 1, 'HEALTH', 'NOT_STARTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer', true, '2023-10-16 12:21:38.709');