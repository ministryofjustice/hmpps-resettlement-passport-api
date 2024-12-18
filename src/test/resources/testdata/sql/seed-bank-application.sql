DELETE from pathway_status;
DELETE from assessment_id_type;
DELETE from assessment;
DELETE from bank_application_status_log;
DELETE from bank_application;
DELETE from prisoner;

INSERT INTO prisoner
(id, noms_id, creation_date)
VALUES(1, '123', '2023-08-16 12:21:38.709');
