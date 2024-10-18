INSERT INTO prisoner (id, noms_id, creation_date, crn, prison_id)
VALUES (1, 'ABC1234', '2023-08-17T12:21:38.709', '123', 'MDI');

INSERT INTO watchlist (id, prisoner_id, staff_username, creation_date)
VALUES (1, (SELECT id FROM prisoner WHERE noms_id = 'ABC1234'), 'RESETTLEMENTPASSPORT_ADM', '2023-08-17T12:21:38.709');
