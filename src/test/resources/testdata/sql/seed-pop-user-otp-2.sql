
DELETE from person_on_probation_user_otp;
DELETE from prisoner;

INSERT INTO prisoner
(id, noms_id, creation_date, crn)
VALUES(1, 'G4161UF', '2024-02-19T09:36:28.713421','abc');

INSERT INTO person_on_probation_user_otp
(id, prisoner_id, otp, expiry_date, creation_date)
VALUES(1,1,'123456','2024-02-24T09:36:28.713421', '2024-02-17T09:36:28.713421')