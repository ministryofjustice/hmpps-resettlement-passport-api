INSERT INTO prisoner (id, noms_id, creation_date, crn, prison_id, release_date) VALUES
    (1, 'ABC1234', '2023-08-16 12:21:38.709', '123', 'MDI', '2030-09-12');

INSERT INTO resettlement_assessment (id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by, case_note_text) VALUES
    (2, 1, 1, 3, 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, NULL, '2023-01-09 19:02:45.000', 'A User', NULL);