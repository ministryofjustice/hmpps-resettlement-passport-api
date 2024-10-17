INSERT INTO prisoner (id, noms_id, creation_date, crn, prison_id)
VALUES (1, 'A8731DY', '2024-04-02 09:11:34.721525 +00:00', 'U328968', 'MDI');


INSERT INTO resettlement_assessment (id, prisoner_id, pathway, assessment_status, assessment_type,
                                            assessment, status_changed_to, created_date, created_by,
                                            case_note_text, created_by_user_id) VALUES
(1, 1, 'ACCOMMODATION', 'SUBMITTED', 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_PERMANENT_OR_FIXED"
      },
      "questionId": "WHERE_DID_THEY_LIVE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO_ANSWER"
      },
      "questionId": "WHERE_WILL_THEY_LIVE_2"
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
        "answer": "D"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', 'SUPPORT_DECLINED', '2024-04-02 09:22:25.080465 +00:00', 'Matthew Kerry', 'D', 'MKERRY_GEN'),
(2, 1, 'ATTITUDES_THINKING_AND_BEHAVIOUR', 'SUBMITTED', 'BCST2', '{
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
        "answer": "a"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', 'SUPPORT_NOT_REQUIRED', '2024-04-02 09:22:42.735799 +00:00', 'Matthew Kerry', 'a', 'MKERRY_GEN'),
(3, 1, 'CHILDREN_FAMILIES_AND_COMMUNITY', 'SUBMITTED', 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
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
        "answer": "NO"
      },
      "questionId": "CARING_FOR_ADULT"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "SUPPORT_FROM_SOCIAL_SERVICES"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "FRIEND_FAMILY_COMMUNITY_SUPPORT"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "INVOLVEMENT_IN_GANG_ACTIVITY"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "UNDER_THREAT_OUTSIDE"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "COMMUNITY_ORGANISATION_SUPPORT"
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
        "answer": "a"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', 'SUPPORT_NOT_REQUIRED', '2024-04-02 09:23:24.498140 +00:00', 'Matthew Kerry', 'a', 'MKERRY_GEN'),
(4, 1, 'DRUGS_AND_ALCOHOL', 'SUBMITTED', 'BCST2', '{
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
        "answer": "SUPPORT_NOT_REQUIRED"
      },
      "questionId": "SUPPORT_NEEDS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "a"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', 'SUPPORT_NOT_REQUIRED', '2024-04-02 09:24:00.471236 +00:00', 'Matthew Kerry', 'a', 'MKERRY_GEN'),
(5, 1, 'EDUCATION_SKILLS_AND_WORK', 'SUBMITTED', 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
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
        "answer": "NO"
      },
      "questionId": "SUPPORT_TO_FIND_JOB"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
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
        "answer": "SUPPORT_NOT_REQUIRED"
      },
      "questionId": "SUPPORT_NEEDS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "b"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', 'SUPPORT_NOT_REQUIRED', '2024-04-02 09:24:21.616536 +00:00', 'Matthew Kerry', 'b', 'MKERRY_GEN'),
(6, 1, 'FINANCE_AND_ID', 'SUBMITTED', 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
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
          "PASSPORT",
          "DRIVING_LICENCE"
        ]
      },
      "questionId": "WHAT_ID_DOCUMENTS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "HELP_APPLY_FOR_ID"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "RECEIVING_BENEFITS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "DEBTS_OR_ARREARS"
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
        "answer": "a"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', 'SUPPORT_NOT_REQUIRED', '2024-04-02 09:24:43.388222 +00:00', 'Matthew Kerry', 'a', 'MKERRY_GEN'),
(7, 1, 'HEALTH', 'SUBMITTED', 'BCST2', '{
  "assessment": [
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "REGISTERED_WITH_GP"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "NO"
      },
      "questionId": "HELP_REGISTERING_GP"
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
        "answer": "SUPPORT_NOT_REQUIRED"
      },
      "questionId": "SUPPORT_NEEDS"
    },
    {
      "answer": {
        "@class": "StringAnswer",
        "answer": "c"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', 'SUPPORT_NOT_REQUIRED', '2024-04-02 09:24:58.336982 +00:00', 'Matthew Kerry', 'c', 'MKERRY_GEN');

INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES (1, 1, 'ACCOMMODATION', 'DONE', '2024-04-02 09:25:01.126170 +00:00');
