package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus

data class PrisonerResettlementAssessment(
  val pathway: Pathway,
  val assessmentStatus: ResettlementAssessmentStatus,
)
