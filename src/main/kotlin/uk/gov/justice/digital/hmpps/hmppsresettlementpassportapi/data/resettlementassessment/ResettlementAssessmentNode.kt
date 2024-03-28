package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType

data class ResettlementAssessmentNode(
  val assessmentPage: IAssessmentPage,
  val nextPage: (nextPageContext: NextPageContext) -> IAssessmentPage,
)

data class NextPageContext(
  val currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>,
  val edit: Boolean,
  val assessmentType: ResettlementAssessmentType,
)
