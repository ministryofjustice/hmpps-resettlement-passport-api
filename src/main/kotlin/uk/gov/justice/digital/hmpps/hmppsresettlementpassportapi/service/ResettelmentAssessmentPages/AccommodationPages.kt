package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettelmentAssessmentPages

import com.fasterxml.jackson.annotation.JsonFormat
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.councilOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions

val accommodationPages: List<ResettlementAssessmentNode> = listOf(
  ResettlementAssessmentNode(
    AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE,
    nextPage =
    fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
      if (currentQuestionsAndAnswers.any { it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE && it.answer?.answer is String && (it.answer?.answer as String) == "NO_PLACE_TO_LIVE" }) {
        return AccommodationAssessmentPage.CONSENT_FOR_CRS
      } else if (currentQuestionsAndAnswers.any {it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE && (it.answer?.answer as String == "PREVIOUS_ADDRESS" || it.answer?.answer as String == "NEW_ADDRESS") }) {
        return AccommodationAssessmentPage.WHO_WILL_THEY_LIVE_WITH
      }
      return AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE //TODO figoue out error case
    },
  ),
  ResettlementAssessmentNode(
    AccommodationAssessmentPage.WHO_WILL_THEY_LIVE_WITH,
    nextPage =
    fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
      return AccommodationAssessmentPage.CHECK_ANSWERS
    },
  ),
  ResettlementAssessmentNode(
    AccommodationAssessmentPage.CONSENT_FOR_CRS,
    nextPage =
    fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
      return AccommodationAssessmentPage.WHAT_COUNCIL_AREA
    },
  ),
  ResettlementAssessmentNode(
    AccommodationAssessmentPage.WHAT_COUNCIL_AREA,
    nextPage =
    fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
      return AccommodationAssessmentPage.CHECK_ANSWERS
    },
  ),
)

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class AccommodationAssessmentPage(override val id: String, override val title: String, override val questionsAndAnswers: MutableList<ResettlementAssessmentQuestionAndAnswer>) : IAssessmentPage {
  WHERE_WILL_THEY_LIVE(id = "WHERE_WILL_THEY_LIVE", title = "Where will they live when released from custody?", questionsAndAnswers = mutableListOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE), ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHAT_IS_THE_ADDRESS))),
  WHO_WILL_THEY_LIVE_WITH(id = "WHO_WILL_THEY_LIVE_WITH", title = "What are the names and ages of all residents at this property and the prisoner's relationship to them?", questionsAndAnswers = mutableListOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHO_WILL_THEY_LIVE_WITH))),
  CONSENT_FOR_CRS(id = "CONSENT_FOR_CRS", title = "Do they give consent for a Commissioned Rehabilitative Service (CRS)?", questionsAndAnswers = mutableListOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.ACCOM_CRS))),
  WHAT_COUNCIL_AREA(id = "WHAT_COUNCIL_AREA", title = "Which council area are they intending to move to on release?", questionsAndAnswers = mutableListOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.COUNCIL_AREA), ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.COUNCIL_AREA_REASON))),
  CHECK_ANSWERS(id = "CHECK_ANSWERS", title = "", questionsAndAnswers = mutableListOf()),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class AccommodationResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
) : IResettlementAssessmentQuestion {
  WHERE_WILL_THEY_LIVE(
    id = "WHERE_WILL_THEY_LIVE",
    title = "",
    type = TypeOfQuestion.RADIO_WITH_ADDRESS,
    options = listOf(
      Option(id = "PREVIOUS_ADDRESS", displayText = "Returning to a previous address"),
      Option(id = "NEW_ADDRESS", displayText = "Moving to new address"),
      Option(id = "NO_PLACE_TO_LIVE", displayText = "No place to live"),
    ),
  ),
  WHO_WILL_THEY_LIVE_WITH(id = "WHO_WILL_THEY_LIVE_WITH", title = "", type = TypeOfQuestion.LIST_OF_PEOPLE),
  WHAT_IS_THE_ADDRESS(id = "WHAT_IS_THE_ADDRESS", title = "", type = TypeOfQuestion.ADDRESS),
  ACCOM_CRS(id = "ACCOM_CRS", title = "", type = TypeOfQuestion.RADIO, options = yesNoOptions),
  CHECK_ANSWERS(id = "CHECK_ANSWERS", title = "", type = TypeOfQuestion.LONG_TEXT), // TODO how to do this?
  COUNCIL_AREA(id = "COUNCIL_AREA", title = "", type = TypeOfQuestion.DROPDOWN, options = councilOptions),
  COUNCIL_AREA_REASON(id = "COUNCIL_AREA_REASON", title = "", type = TypeOfQuestion.LONG_TEXT),
}