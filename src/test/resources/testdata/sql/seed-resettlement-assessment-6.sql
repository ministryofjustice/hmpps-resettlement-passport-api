INSERT INTO prisoner
(id, noms_id, creation_date, crn)
VALUES (1, 'G1458GV', '2023-10-16 12:21:38.709','NGRBG54');

INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES (1, 1, 1, 3, 'BCST2', '{"assessment": [{"answer": {"@class": "StringAnswer","answer": "NO_ANSWER"},"questionId": "WHERE_DID_THEY_LIVE"}]}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
