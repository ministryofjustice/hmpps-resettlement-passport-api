INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(1, '123', '2023-08-16 12:21:38.709','abc', 'xyz', '2025-01-23');
INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(2, '456', '2023-08-17 12:25:45.306', 'def', 'xyz', '2024-02-02');
INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(3, '789', '2023-08-17 12:26:03.441', 'ghi', 'xyz', '2026-10-30');

INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(1, 1, 1, 1, '2023-08-16 12:21:44.234');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(2, 1, 2, 2, '2023-08-17 12:28:10.466');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(3, 1, 3, 3, '2023-08-17 12:28:10.475');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(4, 1, 4, 4, '2023-08-17 12:28:10.479');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(5, 1, 5, 5, '2023-08-17 12:28:10.483');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(6, 1, 6, 2, '2023-08-17 12:28:10.490');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(7, 1, 7, 3, '2023-08-17 12:28:10.493');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(8, 2, 1, 2, '2023-08-17 12:30:03.834');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(9, 2, 2, 3, '2023-08-17 12:30:03.841');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(10, 2, 3, 4, '2023-08-17 12:30:03.846');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(11, 2, 4, 5, '2023-08-17 12:30:03.849');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(12, 2, 5, 1, '2023-08-17 12:30:03.851');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(13, 2, 6, 2, '2023-08-17 12:30:03.855');
INSERT INTO pathway_status
(id, prisoner_id, pathway_id, status_id, updated_date)
VALUES(14, 2, 7, 3, '2023-08-17 12:30:03.858');

INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(1, 1, 1, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(2, 1, 2, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(3, 1, 3, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(4, 1, 4, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(5, 1, 5, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(6, 1, 6, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(7, 1, 7, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');

INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(8, 1, 1, 4, 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(9, 1, 2, 4, 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(11, 1, 3, 4, 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(12, 1, 4, 4, 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(13, 1, 5, 4, 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(14, 1, 6, 4, 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(15, 1, 7, 4, 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709','Prison Officer');
