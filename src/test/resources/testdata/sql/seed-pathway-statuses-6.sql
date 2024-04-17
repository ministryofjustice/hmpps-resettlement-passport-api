INSERT INTO prisoner (id, noms_id, creation_date, crn, prison_id) VALUES (1, 'G1458GV', '2023-05-17 12:21:44.0', 'CRN1', 'xyz');

INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(1, 1, 'ACCOMMODATION', 'CASE_NOTE', '2023-12-05 17:44:30.091', NULL, NULL, 'This is some case note text.', 'John Williams');
