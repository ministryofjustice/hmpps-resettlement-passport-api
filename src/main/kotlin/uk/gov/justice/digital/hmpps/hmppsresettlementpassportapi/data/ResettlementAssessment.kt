package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.time.LocalDateTime

data class ResettlementAssessmentRequest(
  val questionsAndAnswers: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>?,
)

data class ResettlementAssessmentCompleteRequest(
  val questionsAndAnswers: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>,
)

data class ResettlementAssessmentNextPage(
  val nextPageId: String,
)

data class ResettlementAssessmentResponsePage(
  override val id: String,
  override val title: String? = null,
  override val questionsAndAnswers: List<ResettlementAssessmentResponseQuestionAndAnswer>,
) : IAssessmentPage

data class ResettlementAssessmentResponseQuestionAndAnswer(
  override val question: IResettlementAssessmentQuestion,
  override var answer: Answer<*>? = null,
  val originalPageId: String,
) : IResettlementAssessmentQuestionAndAnswer

data class ResettlementAssessmentResponseQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: MutableList<Option>? = null,
  override val validationType: ValidationType = ValidationType.MANDATORY,
) : IResettlementAssessmentQuestion

data class LatestResettlementAssessmentResponse(
  val originalAssessment: ResettlementAssessmentResponse? = null,
  val latestAssessment: ResettlementAssessmentResponse,
)

data class ResettlementAssessmentResponse(
  val assessmentType: ResettlementAssessmentType,
  val lastUpdated: LocalDateTime,
  val updatedBy: String,
  val questionsAndAnswers: List<LatestResettlementAssessmentResponseQuestionAndAnswer>,
)

data class LatestResettlementAssessmentResponseQuestionAndAnswer(
  val questionTitle: String,
  val answer: String?,
  val originalPageId: String,
)
