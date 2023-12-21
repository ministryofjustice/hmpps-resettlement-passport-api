INSERT INTO prisoner (id, noms_id, creation_date, crn, prison_id) VALUES (1, 'G1458GV', '2023-05-17 12:21:44.0', 'CRN1', 'xyz');

INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(1, 1, 'ACCOMMODATION', 'CASE_NOTE', '2023-12-05 17:44:30.091', NULL, NULL, 'This is some case note text.', 'John Williams');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(2, 1, 'BENEFITS', 'APPOINTMENT', '2023-12-20 14:01:23.001', '2024-01-12 13:05:00', 120, E'###\nAppointment Title: Appointment 1\nContact: Person\nOrganisation: Org ABC\nLocation:\n  Building Name: Office\n  Building Number: 100\n  Street Name: Main Street\n  District: \n  Town: Bradford\n  County: West Yorkshire\n  Postcode: BD1 1AA\n###\nNo notes\n###', 'Maria Wilson');
INSERT INTO delius_contact
(id, prisoner_id, category, contact_type, created_date, appointment_date, appointment_duration, notes, created_by)
VALUES(3, 1, 'ACCOMMODATION', 'APPOINTMENT', '2019-12-20 14:09:59.924', '2020-06-01 17:00:00', 30, E'###\nAppointment Title: Appointment 2\nContact: Another person\nOrganisation: Org DEF\nLocation:\n  Building Name: \n  Building Number: 200\n  Street Name: High Street\n  District: Legal District\n  Town: York\n  County: North Yorkshire\n  Postcode: YO99 9ZZ\n###\nsome custom notes\n123\n###', 'Maria Wilson');