INSERT INTO prisoner
(id, noms_id, creation_date, prison_id)
VALUES(1, '123', '2023-08-16 12:21:38.709', 'xyz');
INSERT INTO prisoner
(id, noms_id, creation_date, prison_id)
VALUES(2, '456', '2023-08-17 12:25:45.306', 'xyz');
INSERT INTO prisoner
(id, noms_id, creation_date, prison_id)
VALUES(3, '789', '2023-08-17 12:26:03.441', 'xyz');


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
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(8, 2, 'ACCOMMODATION', 'IN_PROGRESS', '2023-08-17 12:30:03.834');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(9, 2, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'SUPPORT_NOT_REQUIRED', '2023-08-17 12:30:03.841');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(10, 2, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'SUPPORT_DECLINED', '2023-08-17 12:30:03.846');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(11, 2, 'DRUGS_AND_ALCOHOL', 'DONE', '2023-08-17 12:30:03.849');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(12, 2, 'EDUCATION_SKILLS_AND_WORK', 'NOT_STARTED', '2023-08-17 12:30:03.851');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(13, 2, 'FINANCE_AND_ID', 'IN_PROGRESS', '2023-08-17 12:30:03.855');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(14, 2, 'HEALTH', 'SUPPORT_NOT_REQUIRED', '2023-08-17 12:30:03.858');

INSERT INTO  profile_tag
(id, prisoner_id,profile_tags,updated_date)
VALUES(1, 1, '{"tags": ["KEEP_THEIR_HOME", "GAMBLING_ISSUE", "MEET_CHILDREN", "COMMUNITY_ORG_SUPPORT", "CARE_HEALTH_SUPPORT", "MANAGE_DEBT_ARREARS"]}', now())
