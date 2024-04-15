package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

enum class ResettlementAssessmentStatus {
  /**
   * Assessment has not been completed
   */
  NOT_STARTED,

  /**
   * Not used currently
   */
  IN_PROGRESS,

  /**
   * Assessment has been completed but the case note has not been submitted
   */
  COMPLETE,

  /**
   * Assessment has been completed and case note has been submitted
   */
  SUBMITTED,
}
