
DELETE from person_on_probation_user_otp;
DELETE from prisoner;

INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(1, 'G4161UF', '2023-05-17T12:21:44','CRN1', 'MDI', '2024-05-10');

