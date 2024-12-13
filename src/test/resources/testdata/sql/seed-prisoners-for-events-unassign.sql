INSERT INTO prisoner
(id, noms_id, creation_date, crn, prison_id)
VALUES(1, 'A4092EA', '2023-08-16 12:21:38.709', '123', 'MDI');

INSERT INTO case_allocation
(id, prisoner_id, staff_id, staff_firstname, staff_lastname)
VALUES(1, 1, 123, 'Joe', 'Bloggs')