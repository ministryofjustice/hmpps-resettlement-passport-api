INSERT INTO prisoner
(id, noms_id, creation_date, crn)
VALUES (1, 'G1458GV', '2023-10-16 12:21:38.709','NGRBG54');

INSERT INTO resettlement_assessment
(id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by)
VALUES (1, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'COMPLETE', 'BCST2', '{"assessment": [{"answer": {"@class": "StringAnswer","answer": "NO_ANSWER"},"questionId": "HELP_TO_MANAGE_ANGER"}]}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer');
