package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption

val yesNoOptions = listOf(
  ResettlementAssessmentOption(id = "YES", displayText = "Yes"),
  ResettlementAssessmentOption(id = "NO", displayText = "No"),
  ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided"),
)
