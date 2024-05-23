INSERT INTO prisoner (id, noms_id, creation_date, crn, prison_id, release_date) VALUES
    (1, 'ABC1234', '2023-08-16 12:21:38.709', '123', 'MDI', '2030-09-12');

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
        "answer": "Carefully crafted case note answer"
      },
      "questionId": "CASE_NOTE_SUMMARY"
    }
  ]
}', '2024-05-22 15:46:31.837468 +00:00', 'Matthew Kerry', 'sdsd', 'MKERRY_GEN', 'DRUGS_AND_ALCOHOL', 'SUBMITTED',
        'SUPPORT_DECLINED', '2024-05-22 15:47:31.027064 +00:00');

INSERT INTO pathway_status (id, prisoner_id, pathway, status, updated_date) VALUES (1, 1, 'DRUGS_AND_ALCOHOL', 'DONE', '2024-04-02 09:25:01.126170 +00:00');
