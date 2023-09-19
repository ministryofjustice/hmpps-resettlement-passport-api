INSERT INTO prisoner
(id, noms_id, creation_date, crn)
VALUES(1, '123', '2023-08-16 12:21:38.709','abc');

INSERT INTO assessment (id, prisoner_id, assessment_date, is_bank_account_required, is_id_required, when_created, is_deleted)
VALUES(1, 1, '2023-08-16 12:21:38.709',true, true, '2023-08-16 12:21:38.709', false);
