package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status

data class ResettlementAssessment(
  val pathway: Pathway,
  val nomsID: String,
  val type: ResettlementAssessmentType,
  val currentPage: String,
  val questions: List<ResettlementAssessmentQuestionAndAnswer>,
  val newStatus: Status,
)

data class ResettlementAssessmentRequest(
  val pathway: Pathway,
  val nomsID: String,
  val type: ResettlementAssessmentType,
  val currentPage: String,
  val questions: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>,
  val newStatus: Status,
)
