package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.time.LocalDateTime

data class ResettlementAssessmentRequest(
  val questionsAndAnswers: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>?,
)

data class ResettlementAssessmentCompleteRequest(
  val questionsAndAnswers: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>,
  val version: Int = 1,
)

data class ResettlementAssessmentRequestQuestionAndAnswer<T>(
  val question: String,
  val answer: Answer<T>,
)

data class ResettlementAssessmentNextPage(
  val nextPageId: String,
)

data class ResettlementAssessmentResponsePage(
  val id: String,
  val title: String? = null,
  val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>,
)

data class ResettlementAssessmentQuestionAndAnswer(
  val question: ResettlementAssessmentQuestion,
  var answer: Answer<*>? = null,
  val originalPageId: String,
)

data class ResettlementAssessmentQuestion(
  val id: String,
  val title: String,
  val subTitle: String? = null,
  val type: TypeOfQuestion,
  val options: List<ResettlementAssessmentOption>? = null,
  val validationType: ValidationType = ValidationType.MANDATORY,
  val customValidation: CustomValidation? = null,
  val detailsTitle: String? = null,
  val detailsContent: String? = null,
)

data class ResettlementAssessmentOption(
  val id: String,
  val displayText: String,
  val description: String? = null,
  val exclusive: Boolean = false,
  val nestedQuestions: List<ResettlementAssessmentQuestionAndAnswer>? = null,
)

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

data class AssessmentSkipRequest(
  val reason: AssessmentSkipReason,
  val moreInfo: String? = null,
)

enum class AssessmentSkipReason {
  COMPLETED_IN_OASYS,
  COMPLETED_IN_ANOTHER_PRISON,
  EARLY_RELEASE,
  TRANSFER,
  OTHER,
}

data class ResettlementAssessmentSubmitResponse(
  val deliusCaseNoteFailed: Boolean,
)

data class ResettlementAssessmentVersion(
  val version: Int?,
)

data class CustomValidation(
  val regex: String,
  val message: String,
)
