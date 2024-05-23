INSERT INTO prisoner (id, noms_id, creation_date, crn, prison_id, release_date) VALUES
    (1, 'ABC1234', '2023-08-16 12:21:38.709', '123', 'MDI', '2030-09-12');

INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (25, 1, 'BCST2', '{
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
            "addressLine1": "A Road"
          },
          {
            "addressLine2": ""
          },
          {
            "addressTown": "Some Town"
          },
          {
            "addressCounty": "Yorkshire"
          },
          {
            "addressPostcode": "AB13 C45"
          }
        ]
      },
      "questionId": "WHERE_DID_THEY_LIVE_ADDRESS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "HELP_TO_KEEP_HOME"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "WHERE_WILL_THEY_LIVE_1"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "sadas"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 12:40:13.933994 +00:00', 'Matthew Kerry', 'sadas', 'MKERRY_GEN', 'ACCOMMODATION', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 15:47:30.577230 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (26, 1, 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "HELP_TO_MANAGE_ANGER"
    },
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
        "answer": "SUPPORT_NOT_REQUIRED"
      },
      "questionId": "SUPPORT_NEEDS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "asdsada"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 15:45:51.383575 +00:00', 'Matthew Kerry', 'asdsada', 'MKERRY_GEN', 'ATTITUDES_THINKING_AND_BEHAVIOUR',
        'SUBMITTED', 'SUPPORT_NOT_REQUIRED', '2024-05-22 15:47:30.733600 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (27, 1, 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "PARTNER_OR_SPOUSE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "PRIMARY_CARER_FOR_CHILDREN"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "CARING_FOR_ADULT"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "SUPPORT_FROM_SOCIAL_SERVICES"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "FRIEND_FAMILY_COMMUNITY_SUPPORT"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "INVOLVEMENT_IN_GANG_ACTIVITY"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "UNDER_THREAT_OUTSIDE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "COMMUNITY_ORGANISATION_SUPPORT"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": " feffd"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 15:46:17.002205 +00:00', 'Matthew Kerry', ' feffd', 'MKERRY_GEN', 'CHILDREN_FAMILIES_AND_COMMUNITY',
        'SUBMITTED', 'SUPPORT_DECLINED', '2024-05-22 15:47:30.874739 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (28, 1, 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "DRUG_ISSUES"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "ALCOHOL_ISSUES"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "sdsd"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 15:46:31.837468 +00:00', 'Matthew Kerry', 'sdsd', 'MKERRY_GEN', 'DRUGS_AND_ALCOHOL', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 15:47:31.027064 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (29, 1, 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "JOB_BEFORE_CUSTODY"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "HAVE_A_JOB_AFTER_RELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "SUPPORT_TO_FIND_JOB"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "sdsds"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 15:46:50.480459 +00:00', 'Matthew Kerry', 'sdsds', 'MKERRY_GEN', 'EDUCATION_SKILLS_AND_WORK',
        'SUBMITTED', 'SUPPORT_DECLINED', '2024-05-22 15:47:31.186027 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (30, 1, 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "HAS_BANK_ACCOUNT"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "HELP_WITH_BANK_ACCOUNT"
    },
    {
      "answer": {
        "@class": "ListAnswer",
        "answer": [
          "BIRTH_CERTIFICATE",
          "PASSPORT"
        ]
      },
      "questionId": "WHAT_ID_DOCUMENTS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "HELP_APPLY_FOR_ID"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "RECEIVING_BENEFITS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "DEBTS_OR_ARREARS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "sdsd"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 15:47:14.182755 +00:00', 'Matthew Kerry', 'sdsd', 'MKERRY_GEN', 'FINANCE_AND_ID', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 15:47:31.330052 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (31, 1, 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "YES"
      },
      "questionId": "REGISTERED_WITH_GP"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "MEET_HEALTHCARE_TEAM"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "dfdfd"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 15:47:28.145149 +00:00', 'Matthew Kerry', 'dfdfd', 'MKERRY_GEN', 'HEALTH', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 15:47:31.469644 +00:00');

INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (40, 1, 'RESETTLEMENT_PLAN', '{
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
            "addressLine1": "A Road"
          },
          {
            "addressLine2": ""
          },
          {
            "addressTown": "Some Town"
          },
          {
            "addressCounty": "Yorkshire"
          },
          {
            "addressPostcode": "AB13 C45"
          }
        ]
      },
      "questionId": "WHERE_DID_THEY_LIVE_ADDRESS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "HELP_TO_KEEP_HOME"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "WHERE_WILL_THEY_LIVE_1"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS_PRERELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "hello"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 16:02:41.310279 +00:00', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'ACCOMMODATION', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 16:06:53.443806 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (41, 1, 'RESETTLEMENT_PLAN', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "HELP_TO_MANAGE_ANGER"
    },
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
        "answer": "SUPPORT_NOT_REQUIRED"
      },
      "questionId": "SUPPORT_NEEDS_PRERELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "hello"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 16:05:17.294788 +00:00', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'ATTITUDES_THINKING_AND_BEHAVIOUR',
        'SUBMITTED', 'SUPPORT_NOT_REQUIRED', '2024-05-22 16:06:53.590886 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (42, 1, 'RESETTLEMENT_PLAN', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "PARTNER_OR_SPOUSE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "PRIMARY_CARER_FOR_CHILDREN"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "CARING_FOR_ADULT"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "SUPPORT_FROM_SOCIAL_SERVICES"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "FRIEND_FAMILY_COMMUNITY_SUPPORT"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "INVOLVEMENT_IN_GANG_ACTIVITY"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "UNDER_THREAT_OUTSIDE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "COMMUNITY_ORGANISATION_SUPPORT"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS_PRERELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "hello"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 16:05:40.499062 +00:00', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'CHILDREN_FAMILIES_AND_COMMUNITY',
        'SUBMITTED', 'SUPPORT_DECLINED', '2024-05-22 16:06:53.730328 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (43, 1, 'RESETTLEMENT_PLAN', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "YES"
      },
      "questionId": "DRUG_ISSUES"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "SUPPORT_WITH_DRUG_ISSUES"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "ALCOHOL_ISSUES"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS_PRERELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "hello"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 16:05:57.329810 +00:00', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'DRUGS_AND_ALCOHOL', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 16:06:53.873318 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (44, 1, 'RESETTLEMENT_PLAN', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "JOB_BEFORE_CUSTODY"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "HAVE_A_JOB_AFTER_RELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "SUPPORT_TO_FIND_JOB"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS_PRERELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "hello"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 16:06:14.287830 +00:00', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'EDUCATION_SKILLS_AND_WORK',
        'SUBMITTED', 'SUPPORT_DECLINED', '2024-05-22 16:06:54.025609 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (45, 1, 'RESETTLEMENT_PLAN', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "HAS_BANK_ACCOUNT"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "HELP_WITH_BANK_ACCOUNT"
    },
    {
      "answer": {
        "@class": "ListAnswer",
        "answer": [
          "BIRTH_CERTIFICATE",
          "PASSPORT"
        ]
      },
      "questionId": "WHAT_ID_DOCUMENTS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "HELP_APPLY_FOR_ID"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "RECEIVING_BENEFITS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "DEBTS_OR_ARREARS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS_PRERELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "hello"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 16:06:32.454374 +00:00', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'FINANCE_AND_ID', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 16:06:54.175592 +00:00');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                            case_note_text, created_by_user_id, pathway, assessment_status,
                                            status_changed_to, submission_date)
VALUES (46, 1, 'RESETTLEMENT_PLAN', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "YES"
      },
      "questionId": "REGISTERED_WITH_GP"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "MEET_HEALTHCARE_TEAM"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "SUPPORT_DECLINED"
      },
      "questionId": "SUPPORT_NEEDS_PRERELEASE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "hello"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 16:06:48.238178 +00:00', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'HEALTH', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 16:06:54.339643 +00:00');
