
DELETE from person_on_probation_user_otp;
DELETE from prisoner;

INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id, release_date)
VALUES(1, 'G4161UF', '2024-02-19T09:36:28.713421','abc', 'MDI', '2024-12-31');
