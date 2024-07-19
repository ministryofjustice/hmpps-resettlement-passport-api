package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway

data class PrisonerResettlementAssessment(
  val pathway: Pathway,
  val assessmentStatus: ResettlementAssessmentStatus,
)
