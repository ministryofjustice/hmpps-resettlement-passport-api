package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

enum class ResettlementAssessmentStatus(val displayText: String) {
  /**
   * Assessment has not been completed
   */
  NOT_STARTED("Not started"),

  /**
   * Not used currently
   */
  IN_PROGRESS("In progress"),

  /**
   * Assessment has been completed but the case note has not been submitted
   */
  COMPLETE("Complete"),

  /**
   * Assessment has been completed and case note has been submitted
   */
  SUBMITTED("Submitted"),
}
