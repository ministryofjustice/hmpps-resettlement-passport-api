INSERT INTO prisoner (id, noms_id, creation_date, prison_id) VALUES
    (1, 'ABC1234', '2023-08-16 12:21:38.709', 'MDI');
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
        "answer": "Expertly written case note answer"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 15:47:14.182755 +00:00', 'Matthew Kerry', 'sdsd', 'MKERRY_GEN', 'FINANCE_AND_ID', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 15:47:31.330052 +00:00');

INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES (1, 1, 'FINANCE_AND_ID', 'DONE', '2024-04-02 09:25:01.126170 +00:00');
