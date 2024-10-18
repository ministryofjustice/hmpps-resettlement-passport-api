INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id)
VALUES(1, 'G4161UF', '2024-02-19T09:36:28.713421','abc', 'MDI');

INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id)
VALUES(2, 'A8229DY', '2023-05-17T12:21:44','CRN1', 'MDI');

insert into licence_conditions_change_audit(prisoner_id, licence_conditions_hash) values (1, 'a-real-hash-honest');
insert into licence_conditions_change_audit(prisoner_id, licence_conditions_hash) values (2, 'another-real-hash-honest');
