INSERT INTO prisoner
(id, noms_id, creation_date, crn)
VALUES(1, 'G4161UF', '2023-10-16 12:21:38.709','NGRBG54');

INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES(1, 'G4161UF', 1, 1, 'Resettlement Assessment', '{"Key1":"Value1", "Key2":"Value2", "Key3":"Value3"}', 1, '2023-10-16 12:21:38.709','Prison Officer'),
      (1, 'G4161UF', 2, 2, 'Resettlement Assessment', '{"Key1":"Value1", "Key2":"Value2", "Key3":"Value3"}', 1, '2023-10-16 12:21:38.709','Prison Officer')
      (1, 'G4161UF', 3, 3, 'Resettlement Assessment', '{"Key1":"Value1", "Key2":"Value2", "Key3":"Value3"}', 1, '2023-10-16 12:21:38.709','Prison Officer')
      (1, 'G4161UF', 4, 2, 'Resettlement Assessment', '{"Key1":"Value1", "Key2":"Value2", "Key3":"Value3"}', 1, '2023-10-16 12:21:38.709','Prison Officer')
      (1, 'G4161UF', 5, 3, 'Resettlement Assessment', '{"Key1":"Value1", "Key2":"Value2", "Key3":"Value3"}', 1, '2023-10-16 12:21:38.709','Prison Officer')
      (1, 'G4161UF', 6, 1, 'Resettlement Assessment', '{"Key1":"Value1", "Key2":"Value2", "Key3":"Value3"}', 1, '2023-10-16 12:21:38.709','Prison Officer'),
      (1, 'G4161UF', 7, 1, 'Resettlement Assessment', '{"Key1":"Value1", "Key2":"Value2", "Key3":"Value3"}', 1, '2023-10-16 12:21:38.709','Prison Officer');
