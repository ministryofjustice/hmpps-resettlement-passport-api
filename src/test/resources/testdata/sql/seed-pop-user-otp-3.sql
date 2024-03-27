
DELETE from person_on_probation_user_otp;
DELETE from prisoner;

INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(1, 'A8229DY', '2023-05-17T12:21:44','CRN1', 'MDI', '2010-04-03');

INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(2, 'A8314DY', '2023-05-17T12:21:44','CRN2', 'MDI', '2027-11-09');

INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(3, 'G4161UF', '2023-05-17T12:21:44','CRN3', 'MDI', '2025-08-04');

INSERT INTO person_on_probation_user_otp
(id, prisoner_id, otp, expiry_date, creation_date, dob)
VALUES (1, 1, '1X3456','2024-02-18T23:59:59' , '2024-02-11T10:18:22','2000-01-01');

INSERT INTO person_on_probation_user_otp
(id, prisoner_id, otp, expiry_date, creation_date, dob)
VALUES (2, 2, '1Y3456','2024-02-26T23:59:59' , '2024-02-19T10:18:22','2000-02-01');

INSERT INTO person_on_probation_user_otp
(id, prisoner_id, otp, expiry_date, creation_date, dob)
VALUES (3, 3, '1Z3456','2024-02-26T23:59:59' , '2024-02-19T10:18:22','2000-03-01');
