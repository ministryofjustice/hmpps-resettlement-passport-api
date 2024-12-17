INSERT INTO prisoner (id, noms_id, creation_date, prison_id) VALUES
    (1, 'ABC1234', '2023-08-16 12:21:38.709', 'MDI');

INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by, case_note_text, created_by_user_id) VALUES
    (2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'COMPLETE', 'RESETTLEMENT_PLAN', '{"assessment": []}'::jsonb, 'SUPPORT_DECLINED', '2023-01-09 19:02:45.000', 'A User', 'Some case notes', 'JSMITH_GEN');