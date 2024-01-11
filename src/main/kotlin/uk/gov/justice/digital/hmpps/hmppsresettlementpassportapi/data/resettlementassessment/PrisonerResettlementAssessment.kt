package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus

data class PrisonerResettlementAssessment(
  val pathway: Pathway,
  val assessmentStatus: ResettlementAssessmentStatus,
)
