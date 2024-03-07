package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

data class ResettlementAssessmentNode(
  val assessmentPage: IAssessmentPage,
  val nextPage: (currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, edit: Boolean) -> IAssessmentPage,
)
