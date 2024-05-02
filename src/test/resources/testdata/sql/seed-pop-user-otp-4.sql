
DELETE from person_on_probation_user_otp;
DELETE from prisoner;

INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(1, 'G4161UF', '2023-05-17T12:21:44','CRN1', 'MDI', '2010-04-03');


INSERT INTO person_on_probation_user_otp
(id, prisoner_id, otp, expiry_date, creation_date, dob)
VALUES (1, 1, '1XYZ56','2024-02-18T23:59:59' , '2024-02-11T10:18:22','2000-01-01');

