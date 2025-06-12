INSERT INTO prisoner (id, noms_id, creation_date, prison_id) VALUES (1, 'G4161UF', '2023-05-17 12:21:44.0', 'MDI');

INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES (1, 1, 'ACCOMMODATION', 'NOT_STARTED', '2023-05-17 12:21:44.0');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES (2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'IN_PROGRESS', '2023-05-18 12:21:44.0');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES (3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'SUPPORT_NOT_REQUIRED', '2023-05-19 12:21:44.0');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES (4, 1, 'DRUGS_AND_ALCOHOL', 'SUPPORT_DECLINED', '2023-05-20 12:21:44.0');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES (5, 1, 'EDUCATION_SKILLS_AND_WORK', 'DONE', '2023-05-21 12:21:44.0');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES (6, 1, 'FINANCE_AND_ID', 'NOT_STARTED', '2023-05-22 12:21:44.0');
INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES (7, 1, 'HEALTH', 'IN_PROGRESS', '2023-05-23 12:21:44.0');

INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by)
VALUES(1, 1, 'ACCOMMODATION', 'COMPLETE', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709','Prison Officer');

INSERT INTO case_allocation (id, prisoner_id, staff_id, staff_firstname, staff_lastname, is_deleted, when_created, deleted_at) VALUES (1, 1, 456769, 'PSO1 Firstname', 'PSO1 Lastname', false,  '2023-05-17 16:21:44.0',  null);