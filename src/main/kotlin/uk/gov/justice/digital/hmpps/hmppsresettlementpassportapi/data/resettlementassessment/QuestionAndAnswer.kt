package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

interface IResettlementAssessmentQuestionAndAnswer {
  val question: IResettlementAssessmentQuestion
  val answer: Answer<*>?
}

data class ResettlementAssessmentRequestQuestionAndAnswer<T>(
  val question: String,
  val answer: Answer<T>,
)

data class ResettlementAssessmentQuestionAndAnswer(
  override val question: IResettlementAssessmentQuestion,
  override var answer: Answer<*>? = null,
) : IResettlementAssessmentQuestionAndAnswer
