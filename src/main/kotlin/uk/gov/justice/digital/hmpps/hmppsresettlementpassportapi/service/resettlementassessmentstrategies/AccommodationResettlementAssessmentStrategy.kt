package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.councilOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository

@Service
class AccommodationResettlementAssessmentStrategy(
  resettlementAssessmentRepository: ResettlementAssessmentRepository,
  prisonerRepository: PrisonerRepository,
  statusRepository: StatusRepository,
  pathwayRepository: PathwayRepository,
  resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
) : AbstractResettlementAssessmentStrategy<AccommodationAssessmentPage, AccommodationResettlementAssessmentQuestion>(resettlementAssessmentRepository, prisonerRepository, statusRepository, pathwayRepository, resettlementAssessmentStatusRepository, AccommodationAssessmentPage::class, AccommodationResettlementAssessmentQuestion::class) {
  override fun appliesTo(pathway: Pathway) = pathway == Pathway.ACCOMMODATION

  override fun getPageList(): List<ResettlementAssessmentNode> = listOf(
    ResettlementAssessmentNode(
      AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE && it.answer?.answer is String && (it.answer!!.answer as String) == "NO_PLACE_TO_LIVE" }) {
          AccommodationAssessmentPage.CONSENT_FOR_CRS
        } else if (currentQuestionsAndAnswers.any { it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE && (it.answer?.answer as String == "PREVIOUS_ADDRESS" || it.answer!!.answer as String == "NEW_ADDRESS") }) {
          AccommodationAssessmentPage.WHO_WILL_THEY_LIVE_WITH
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      AccommodationAssessmentPage.WHO_WILL_THEY_LIVE_WITH,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return GenericAssessmentPage.ASSESSMENT_SUMMARY
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
        return GenericAssessmentPage.ASSESSMENT_SUMMARY
      },
    ),
  )
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class AccommodationAssessmentPage(override val id: String, override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>) : IAssessmentPage {
  WHERE_WILL_THEY_LIVE(id = "WHERE_WILL_THEY_LIVE", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE), ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHAT_IS_THE_ADDRESS))),
  WHO_WILL_THEY_LIVE_WITH(id = "WHO_WILL_THEY_LIVE_WITH", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHO_WILL_THEY_LIVE_WITH))),
  CONSENT_FOR_CRS(id = "CONSENT_FOR_CRS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.ACCOM_CRS))),
  WHAT_COUNCIL_AREA(id = "WHAT_COUNCIL_AREA", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.COUNCIL_AREA), ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.COUNCIL_AREA_REASON))),
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
    title = "Where will they live when released from custody?",
    type = TypeOfQuestion.RADIO_WITH_ADDRESS,
    options = listOf(
      Option(id = "PREVIOUS_ADDRESS", displayText = "Returning to a previous address"),
      Option(id = "NEW_ADDRESS", displayText = "Moving to new address"),
      Option(id = "NO_PLACE_TO_LIVE", displayText = "No place to live"),
    ),
  ),
  WHO_WILL_THEY_LIVE_WITH(id = "WHO_WILL_THEY_LIVE_WITH", title = "What are the names and ages of all residents at this property and the prisoner's relationship to them?", type = TypeOfQuestion.LIST_OF_PEOPLE),
  WHAT_IS_THE_ADDRESS(id = "WHAT_IS_THE_ADDRESS", title = "", type = TypeOfQuestion.ADDRESS),
  ACCOM_CRS(id = "ACCOM_CRS", title = "Do they give consent for a Commissioned Rehabilitative Service (CRS)?", type = TypeOfQuestion.RADIO, options = yesNoOptions),
  COUNCIL_AREA(id = "COUNCIL_AREA", title = "Which council area are they intending to move to on release?", type = TypeOfQuestion.DROPDOWN, options = councilOptions),
  COUNCIL_AREA_REASON(id = "COUNCIL_AREA_REASON", title = "Why do they intend to move to this council area on release?", type = TypeOfQuestion.LONG_TEXT),
}
