package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

data class ResettlementAssessmentRequestQuestionAndAnswer<T>(
  val question: String,
  val answer: Answer<T>,
)

data class ResettlementAssessmentQuestionAndAnswer(
  val question: ResettlementAssessmentQuestion,
  var answer: Answer<*>? = null,
)
