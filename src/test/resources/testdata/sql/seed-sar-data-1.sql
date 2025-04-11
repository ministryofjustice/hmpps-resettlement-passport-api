INSERT INTO prisoner
(id, noms_id, creation_date)
VALUES
(1, 'G1458GV', '2024-01-01 12:21:38.709');

INSERT INTO assessment (id, prisoner_id, assessment_date, is_bank_account_required, is_id_required, when_created, is_deleted)
VALUES(1, 1, '2023-08-16 12:21:38.709',true, true, '2023-08-16 12:21:38.709', false);

INSERT INTO delius_contact (id, prisoner_id, category, contact_type, created_date, notes, created_by)
VALUES(1, 1, 'ACCOMMODATION','CASE_NOTE', '2023-08-16 12:21:38.709', 'Some notes here', 'username1'),
(2, 1, 'HEALTH','CASE_NOTE', '2024-06-01 12:21:38.709', 'health notes', 'username1');

INSERT INTO bank_application (id, prisoner_id, application_submitted_date, status, added_to_personal_items, when_created, is_deleted, bank_name)
VALUES(1, 1, '2023-08-16 12:21:38.709', 'Application submitted' ,false, '2023-08-16 12:21:38.709', false, 'Nationwide');

INSERT INTO id_application (id, prisoner_id, application_submitted_date, id_type_id, cost_of_application, have_gro, uk_national_born_overseas,
                            priority_application, status, status_update_date, added_to_personal_items, when_created, is_deleted)
VALUES(1, 1, '2023-08-16 12:21:38.709', 2 ,'10', false, false, false,
       'pending', '2023-08-16 12:21:38.709', false, '2023-08-16 12:21:38.709', false);

INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(1, 1, 'ACCOMMODATION', 'NOT_STARTED', '2023-08-16 12:21:44.234');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'IN_PROGRESS', '2023-08-17 12:28:10.466');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'SUPPORT_NOT_REQUIRED', '2023-08-17 12:28:10.475');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(4, 1, 'DRUGS_AND_ALCOHOL', 'SUPPORT_DECLINED', '2023-08-17 12:28:10.479');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(5, 1, 'EDUCATION_SKILLS_AND_WORK', 'DONE', '2023-08-17 12:28:10.483');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(6, 1, 'FINANCE_AND_ID', 'IN_PROGRESS', '2023-08-17 12:28:10.490');
INSERT INTO pathway_status
(id, prisoner_id, pathway, status, updated_date)
VALUES(7, 1, 'HEALTH', 'SUPPORT_NOT_REQUIRED', '2023-08-17 12:28:10.493');

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
}', '2024-05-22 12:40:13.933994', 'Matthew Kerry', 'sadas', 'MKERRY_GEN', 'ACCOMMODATION', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 15:47:30.577230');
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
}', '2024-05-22 15:45:51.383575', 'Matthew Kerry', 'asdsada', 'MKERRY_GEN', 'ATTITUDES_THINKING_AND_BEHAVIOUR',
        'SUBMITTED', 'SUPPORT_NOT_REQUIRED', '2024-05-22 15:47:30.733600');
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
}', '2024-05-22 15:46:17.002205', 'Matthew Kerry', ' feffd', 'MKERRY_GEN', 'CHILDREN_FAMILIES_AND_COMMUNITY',
        'SUBMITTED', 'SUPPORT_DECLINED', '2024-05-22 15:47:30.874739');
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
}', '2024-05-22 15:46:31.837468', 'Matthew Kerry', 'sdsd', 'MKERRY_GEN', 'DRUGS_AND_ALCOHOL', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 15:47:31.027064');
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
}', '2024-05-22 15:46:50.480459', 'Matthew Kerry', 'sdsds', 'MKERRY_GEN', 'EDUCATION_SKILLS_AND_WORK',
        'SUBMITTED', 'SUPPORT_DECLINED', '2024-05-22 15:47:31.186027');
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
}', '2024-05-22 15:47:14.182755', 'Matthew Kerry', 'sdsd', 'MKERRY_GEN', 'FINANCE_AND_ID', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 15:47:31.330052');
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
}', '2024-05-22 15:47:28.145149', 'Matthew Kerry', 'dfdfd', 'MKERRY_GEN', 'HEALTH', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 15:47:31.469644');
INSERT INTO resettlement_assessment (id, prisoner_id, assessment_type, assessment, created_date, created_by,
                                     case_note_text, created_by_user_id, pathway, assessment_status,
                                     status_changed_to, submission_date)
VALUES (39, 1, 'BCST2', '{
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
    }
  ]
}', '2024-05-22 15:54:35.535989', 'Matthew Kerry', null, 'MKERRY_GEN', 'DRUGS_AND_ALCOHOL', 'SUBMITTED', null,
        '2024-05-22 15:54:35.535994');
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
}', '2024-05-22 16:02:41.310279', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'ACCOMMODATION', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 16:06:53.443806');
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
}', '2024-05-22 16:05:17.294788', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'ATTITUDES_THINKING_AND_BEHAVIOUR',
        'SUBMITTED', 'SUPPORT_NOT_REQUIRED', '2024-05-22 16:06:53.590886');
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
}', '2024-05-22 16:05:40.499062', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'CHILDREN_FAMILIES_AND_COMMUNITY',
        'SUBMITTED', 'SUPPORT_DECLINED', '2024-05-22 16:06:53.730328');
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
}', '2024-05-22 16:05:57.329810', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'DRUGS_AND_ALCOHOL', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 16:06:53.873318');
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
}', '2024-05-22 16:06:14.287830', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'EDUCATION_SKILLS_AND_WORK',
        'SUBMITTED', 'SUPPORT_DECLINED', '2024-05-22 16:06:54.025609');
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
}', '2024-05-22 16:06:32.454374', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'FINANCE_AND_ID', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 16:06:54.175592');
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
}', '2024-05-22 16:06:48.238178', 'Matthew Kerry', 'hello', 'MKERRY_GEN', 'HEALTH', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 16:06:54.339643');

INSERT INTO assessment_skip(id, prisoner_id, assessment_type, reason, more_info, created_by, creation_date)
VALUES(1, 1, '0', 'COMPLETED_IN_OASYS', 'Skipped as completed in OASys', 'MKERRY_GEN', '2020-09-01 12:00:00.000');
INSERT INTO assessment_skip(id, prisoner_id, assessment_type, reason, more_info, created_by, creation_date)
VALUES(2, 1, '0', 'EARLY_RELEASE', 'Skipped as on early release', 'MKERRY_GEN', '2022-09-01 12:00:00.000');
INSERT INTO assessment_skip(id, prisoner_id, assessment_type, reason, more_info, created_by, creation_date)
VALUES(3, 1, '0', 'OTHER', 'Skipped for other reason', 'MKERRY_GEN', '2023-09-01 12:00:00.000');

INSERT INTO support_need
(id, pathway, "section", title, hidden, exclude_from_count, allow_other_detail, created_date, is_deleted, deleted_date)
VALUES(999999, 'ACCOMMODATION', 'Test support need', 'Used for exact creation date', false, false, false, '2020-01-01 00:00:00.000', false, NULL)
ON CONFLICT DO NOTHING;
INSERT INTO prisoner_support_need
(id, prisoner_id, support_need_id, other_detail, created_by, created_date, is_deleted, deleted_date, latest_update_id)
VALUES(1, 1, 999999, NULL, 'Matthew Kerry', '2020-09-01 12:00:00.000', true, '2023-09-01 12:00:00.000', NULL);
INSERT INTO prisoner_support_need
(id, prisoner_id, support_need_id, other_detail, created_by, created_date, is_deleted, deleted_date, latest_update_id)
VALUES(2, 1, 999999, NULL, 'Matthew Kerry', '2022-09-01 12:00:00.000', false, NULL, NULL);
INSERT INTO prisoner_support_need
(id, prisoner_id, support_need_id, other_detail, created_by, created_date, is_deleted, deleted_date, latest_update_id)
VALUES(3, 1, 999999, NULL, 'Matthew Kerry', '2023-09-01 12:00:00.000', false, NULL, NULL);

INSERT INTO prisoner_support_need_update
(id, prisoner_support_need_id, created_by, created_date, update_text, status, is_prison, is_probation, is_deleted, deleted_date)
VALUES(1, 3, 'Matthew Kerry', '2020-09-01 12:00:00.000', 'Support need in progress 1', 'IN_PROGRESS', false, true, true, '2020-09-01 13:00:00.000');
INSERT INTO prisoner_support_need_update
(id, prisoner_support_need_id, created_by, created_date, update_text, status, is_prison, is_probation, is_deleted, deleted_date)
VALUES(2, 3, 'Matthew Kerry', '2020-09-01 14:00:00.000', 'Support need in progress 2', 'IN_PROGRESS', false, true, false, NULL);
INSERT INTO prisoner_support_need_update
(id, prisoner_support_need_id, created_by, created_date, update_text, status, is_prison, is_probation, is_deleted, deleted_date)
VALUES(3, 3, 'Matthew Kerry', '2022-09-01 14:00:00.000', 'Support need in progress 3', 'IN_PROGRESS', false, true, false, NULL);
INSERT INTO prisoner_support_need_update
(id, prisoner_support_need_id, created_by, created_date, update_text, status, is_prison, is_probation, is_deleted, deleted_date)
VALUES(4, 3, 'Matthew Kerry', '2023-09-01 14:00:00.000', 'Support need met', 'MET', false, true, false, NULL);

INSERT INTO case_allocation
(id, prisoner_id, staff_id, staff_firstname, staff_lastname, is_deleted, when_created, deleted_at)
VALUES(1, 1, 999999, 'Michael', 'Scott', true, '2020-09-01 12:00:00.000', '2020-09-02 12:00:00.000');
INSERT INTO case_allocation
(id, prisoner_id, staff_id, staff_firstname, staff_lastname, is_deleted, when_created, deleted_at)
VALUES(2, 1, 999999, 'Michael', 'Scott', true, '2022-09-01 12:00:00.000', '2022-09-02 12:00:00.000');
INSERT INTO case_allocation
(id, prisoner_id, staff_id, staff_firstname, staff_lastname, is_deleted, when_created, deleted_at)
VALUES(3, 1, 999999, 'Michael', 'Scott', false, '2023-09-01 12:00:00.000', null);

/* Note: There should only be one profile_tag record per prisoner but it is not enforced the db */
INSERT INTO profile_tag
(id, prisoner_id, profile_tags, updated_date)
VALUES(1, 1, '{"tags": ["NO_FIXED_ABODE"]}'::jsonb, '2020-01-01 00:00:00.000');
INSERT INTO profile_tag
(id, prisoner_id, profile_tags, updated_date)
VALUES(2, 1, '{"tags": ["HELP_TO_CONTACT_BANK"]}'::jsonb, '2020-01-01 00:00:00.000');

INSERT INTO todo_item
(id, prisoner_id, title, notes, due_date, completed, created_by_urn, updated_by_urn, creation_date, updated_at)
VALUES('f2b40916-7399-4491-bf96-03b15d114a4d'::uuid, 1, 'Title', 'This is a to do item', '2030-01-01', false, '999999', '999999', '2020-09-01 12:00:00.000', '2020-09-01 12:00:00.000');
INSERT INTO todo_item
(id, prisoner_id, title, notes, due_date, completed, created_by_urn, updated_by_urn, creation_date, updated_at)
VALUES('48b6d8c4-be42-492e-bebc-71d4e396676d'::uuid, 1, 'Title', 'This is a to do item', '2030-01-01', false, '999999', '999999', '2022-09-01 12:00:00.000', '2022-09-01 12:00:00.000');
INSERT INTO todo_item
(id, prisoner_id, title, notes, due_date, completed, created_by_urn, updated_by_urn, creation_date, updated_at)
VALUES('796e405b-2b56-4ba8-a237-15f2151bb1b0'::uuid, 1, 'Title', 'This is a to do item', '2030-01-01', false, '999999', '999999', '2023-09-01 12:00:00.000', '2023-09-01 12:00:00.000');

INSERT INTO document_location
(id, prisoner_id, original_document_key, creation_date, pdf_document_key, category, original_document_file_name, is_deleted, deleted_date)
VALUES(1, 1, '8ad7b2e2-7160-4731-8b85-bacf49756a23'::uuid, '2020-09-01 12:01:00.000', 'c33e3a56-545f-41ee-bb44-632746480f22'::uuid, 'LICENCE_CONDITIONS', 'license1.pdf', true, '2020-09-02 12:01:00.000');
INSERT INTO document_location
(id, prisoner_id, original_document_key, creation_date, pdf_document_key, category, original_document_file_name, is_deleted, deleted_date)
VALUES(2, 1, '3e5a4c5b-86d8-4617-bfbe-15aa3a91dfd3'::uuid, '2022-09-01 12:01:00.000', '32deeb98-30f4-4baa-b187-1b0276736797'::uuid, 'LICENCE_CONDITIONS', 'license2.pdf', true, '2022-09-02 12:01:00.000');
INSERT INTO document_location
(id, prisoner_id, original_document_key, creation_date, pdf_document_key, category, original_document_file_name, is_deleted, deleted_date)
VALUES(3, 1, '10ad790a-6981-4b9c-bace-604c0468ec5b'::uuid, '2023-09-01 12:01:00.000', 'ed25e653-607f-428b-9c0d-06cc67465fb7'::uuid, 'LICENCE_CONDITIONS', 'license3.pdf', false, null);