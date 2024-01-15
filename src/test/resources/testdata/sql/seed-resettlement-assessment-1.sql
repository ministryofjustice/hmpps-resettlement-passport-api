INSERT INTO prisoner
(id, noms_id, creation_date, crn)
VALUES
(1, 'G4161UF', '2023-10-16 12:21:38.709','NGRBG54');

INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES
    (1, 1, 1, 1, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer'),
    (2, 1, 2, 1, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer'),
    (3, 1, 3, 3, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer'),
    (4, 1, 4, 1, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer'),
    (5, 1, 5, 3, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer'),
    (6, 1, 6, 1, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer'),
    (7, 1, 7, 1, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
