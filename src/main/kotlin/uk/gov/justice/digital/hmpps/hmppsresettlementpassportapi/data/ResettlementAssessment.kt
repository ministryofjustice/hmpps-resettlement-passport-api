package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status

data class ResettlementAssessment(
  val pathway: Pathway,
  val nomsID: String,
  val type: ResettlementAssessmentType,
  val questions: List<ResettlementAssessmentQuestionAndAnswer<*>>,
  val newStatus: Status,
)
