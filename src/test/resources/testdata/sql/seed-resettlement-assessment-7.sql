INSERT INTO prisoner
(id, noms_id, creation_date, crn)
VALUES
(1, 'G4161UF', '2023-10-16 12:21:38.709','NGRBG54');

INSERT INTO resettlement_assessment
(id, prisoner_id, pathway_id, assessment_status_id, assessment_type, assessment, status_changed_to_status_id, created_date, created_by)
VALUES
    (1, 1, 1, 4, 'BCST2', '{
      "assessment": [
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "NO_ANSWER"
          },
          "questionId": "WHERE_DID_THEY_LIVE"
        },
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "MOVE_TO_NEW_ADDRESS"
          },
          "questionId": "WHERE_WILL_THEY_LIVE_2"
        },
        {
          "answer": {
            "@class": "MapAnswer",
            "answer": [
              {
                "addressLine1_MOVE_TO_NEW_ADDRESS": "123 The Street"
              },
              {
                "addressLine2_MOVE_TO_NEW_ADDRESS": ""
              },
              {
                "addressTown_MOVE_TO_NEW_ADDRESS": "Leeds"
              },
              {
                "addressCounty_MOVE_TO_NEW_ADDRESS": "West Yorkshire"
              },
              {
                "addressPostcode_MOVE_TO_NEW_ADDRESS": "LS1 1AA"
              }
            ]
          },
          "questionId": "WHERE_WILL_THEY_LIVE_ADDRESS"
        },
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "SUPPORT_NOT_REQUIRED"
          },
          "questionId": "SUPPORT_NEEDS"
        },
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "Some case notes text."
          },
          "questionId": "CASE_NOTE_SUMMARY"
        }
      ]
    }'::jsonb, 1, '2023-10-10 15:54:02.235', 'Prison Officer'),
    (2, 1, 2, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709', 'Prison Officer'),
    (3, 1, 3, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709', 'Prison Officer'),
    (4, 1, 4, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709', 'Prison Officer'),
    (5, 1, 5, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709', 'Prison Officer'),
    (6, 1, 6, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709', 'Prison Officer'),
    (7, 1, 7, 4, 'BCST2', '{"assessment": []}'::jsonb, 1, '2023-10-16 12:21:38.709', 'Prison Officer');

INSERT INTO public.pathway_status (id, prisoner_id, pathway_id, status_id, updated_date) VALUES (1, 1, 1, 5, '2024-04-02 09:25:01.126170 +00:00');
