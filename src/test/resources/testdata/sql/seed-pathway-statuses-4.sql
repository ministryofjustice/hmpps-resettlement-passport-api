INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(1, 'G4274GN', '2023-08-16 12:21:38.709', '123', 'MDI', '2030-09-12');

INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(1, 1, 'ACCOMMODATION', 'NOT_STARTED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'IN_PROGRESS', '2023-08-17 12:28:10.466');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'SUPPORT_NOT_REQUIRED', '2023-08-17 12:28:10.475');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(4, 1, 'DRUGS_AND_ALCOHOL', 'SUPPORT_DECLINED', '2023-08-17 12:28:10.479');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(5, 1, 'EDUCATION_SKILLS_AND_WORK', 'DONE', '2023-08-17 12:28:10.483');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(6, 1, 'FINANCE_AND_ID', 'IN_PROGRESS', '2023-08-17 12:28:10.490');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(7, 1, 'HEALTH', 'SUPPORT_NOT_REQUIRED', '2023-08-17 12:28:10.493');