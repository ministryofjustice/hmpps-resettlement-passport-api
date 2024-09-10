INSERT INTO prisoner
(id, noms_id, creation_date, crn)
VALUES
(1, 'G4161UF', '2023-10-16 12:21:38.709','NGRBG54');

INSERT INTO resettlement_assessment
(id, prisoner_id, pathway, assessment_status, assessment_type, assessment, status_changed_to, created_date, created_by)
VALUES
    (1, 1, 'ACCOMMODATION', 'SUBMITTED', 'BCST2', '{
      "assessment": [
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
            "answer": "NO_ANSWER"
          },
          "questionId": "WHERE_DID_THEY_LIVE"
        },
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "Some case notes text."
          },
          "questionId": "CASE_NOTE_SUMMARY"
        }
      ]
    }'::jsonb, 'NOT_STARTED', '2023-10-10 15:54:02.235', 'Prison Officer'),
    (2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'SUBMITTED', 'BCST2', '{
      "assessment": [
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "YES"
          },
          "questionId": "ISSUES_WITH_GAMBLING"
        },
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "NO_ANSWER"
          },
          "questionId": "HELP_TO_MANAGE_ANGER"
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
    }'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709', 'Prison Officer'),
    (3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709', 'Prison Officer'),
    (4, 1, 'DRUGS_AND_ALCOHOL', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709', 'Prison Officer'),
    (5, 1, 'EDUCATION_SKILLS_AND_WORK', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709', 'Prison Officer'),
    (6, 1, 'FINANCE_AND_ID', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709', 'Prison Officer'),
    (7, 1, 'HEALTH', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709', 'Prison Officer'),
    (8, 1, 'ACCOMMODATION', 'SUBMITTED', 'BCST2', '{"assessment": []}'::jsonb, 'NOT_STARTED', '2023-10-17 12:21:38.709', 'Prison Officer'),
    (9, 1, 'ACCOMMODATION', 'SUBMITTED', 'BCST2',
     '{
      "assessment": [
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "SOCIAL_HOUSING"
          },
          "questionId": "WHERE_DID_THEY_LIVE"
        },
        {
          "answer": {
            "@class": "MapAnswer",
            "answer": [
              {
                "addressLine1_MOVE_TO_NEW_ADDRESS": "12 New address Street"
              },
              {
                "addressLine2_MOVE_TO_NEW_ADDRESS": "abcd"
              },
              {
                "addressTown_MOVE_TO_NEW_ADDRESS": "Bradford"
              },
              {
                "addressCounty_MOVE_TO_NEW_ADDRESS": "West Yorkshire"
              },
              {
                "addressPostcode_MOVE_TO_NEW_ADDRESS": "BD1 1AB"
              }
            ]
          },
          "questionId": "WHERE_WILL_THEY_LIVE_ADDRESS"
        },
        {
          "answer": {
            "@class": "MapAnswer",
            "answer": [
              {
                "addressLine1_SOCIAL_HOUSING": "12 Street Lane"
              },
              {
                "addressLine2_SOCIAL_HOUSING": ""
              },
              {
                "addressTown_SOCIAL_HOUSING": "Leeds"
              },
              {
                "addressCounty_SOCIAL_HOUSING": ""
              },
              {
                "addressPostcode_SOCIAL_HOUSING": "LS1 2AB"
              }
            ]
          },
          "questionId": "WHERE_DID_THEY_LIVE_ADDRESS"
        },
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "NO"
          },
          "questionId": "HELP_TO_KEEP_HOME"
        },
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "MOVE_TO_NEW_ADDRESS"
          },
          "questionId": "WHERE_WILL_THEY_LIVE_1"
        },
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "SUPPORT_REQUIRED"
          },
          "questionId": "SUPPORT_NEEDS"
        },
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "sdfsdfds"
          },
          "questionId": "CASE_NOTE_SUMMARY"
        }
      ]
    }'::jsonb, 'NOT_STARTED', '2023-10-18 12:21:38.709', 'Prison Officer'),
    (10, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'SUBMITTED', 'BCST2', '{
      "assessment": [
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "NO"
          },
          "questionId": "ISSUES_WITH_GAMBLING"
        },
        {
          "answer": {
            "@class": "StringAnswer",
            "answer": "YES"
          },
          "questionId": "HELP_TO_MANAGE_ANGER"
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
    }'::jsonb, 'NOT_STARTED', '2023-10-16 12:21:38.709', 'Prison Officer');
