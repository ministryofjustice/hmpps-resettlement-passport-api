package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

interface IAssessmentPage {
  val id: String
  val title: String?
  val questionsAndAnswers: List<IResettlementAssessmentQuestionAndAnswer>
}
