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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
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
      AccommodationAssessmentPage.WHERE_DID_THEY_LIVE,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE && it.answer?.answer is String && (it.answer!!.answer as String in listOf("PRIVATE_RENTED_HOUSING", "SOCIAL_HOUSING", "HOMEOWNER")) }) {
          AccommodationAssessmentPage.WHERE_DID_THEY_LIVE_ADDRESS
        } else if (currentQuestionsAndAnswers.any { it.question == AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE && (it.answer?.answer as String in listOf("NO_PERMANENT_OR_FIXED", "NO_ANSWER")) }) {
          AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE_2
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      AccommodationAssessmentPage.WHERE_DID_THEY_LIVE_ADDRESS,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return AccommodationAssessmentPage.HELP_TO_KEEP_HOME
      },
    ),
    ResettlementAssessmentNode(
      AccommodationAssessmentPage.HELP_TO_KEEP_HOME,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE_1
      },
    ),
    ResettlementAssessmentNode(
      AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE_1,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, edit: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_1 && it.answer?.answer is String && (it.answer!!.answer as String == "MOVE_TO_NEW_ADDRESS") }) {
          AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE_ADDRESS
        } else if (currentQuestionsAndAnswers.any { it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_1 && (it.answer?.answer as String in listOf("RETURN_TO_PREVIOUS_ADDRESS", "DOES_NOT_HAVE_ANYWHERE", "NO_ANSWER")) }) {
          finalQuestionNextPage(currentQuestionsAndAnswers, edit)
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_1}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE_2,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, edit: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_2 && it.answer?.answer is String && (it.answer!!.answer as String == "MOVE_TO_NEW_ADDRESS") }) {
          AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE_ADDRESS
        } else if (currentQuestionsAndAnswers.any { it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_2 && (it.answer?.answer as String in listOf("DOES_NOT_HAVE_ANYWHERE", "NO_ANSWER")) }) {
          finalQuestionNextPage(currentQuestionsAndAnswers, edit)
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_2}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE_ADDRESS,
      nextPage = ::finalQuestionNextPage,
    ),
    assessmentSummaryNode,
  )
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class AccommodationAssessmentPage(override val id: String, override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, override val title: String? = null) : IAssessmentPage {
  WHERE_DID_THEY_LIVE(id = "WHERE_DID_THEY_LIVE", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE))),
  WHERE_DID_THEY_LIVE_ADDRESS(id = "WHERE_DID_THEY_LIVE_ADDRESS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE_ADDRESS)), title = "Where did the person in prison live before custody?"),
  HELP_TO_KEEP_HOME(id = "HELP_TO_KEEP_HOME", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.HELP_TO_KEEP_HOME))),
  WHERE_WILL_THEY_LIVE_1(id = "WHERE_WILL_THEY_LIVE_1", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_1))),
  WHERE_WILL_THEY_LIVE_2(id = "WHERE_WILL_THEY_LIVE_2", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_2))),
  WHERE_WILL_THEY_LIVE_ADDRESS(id = "WHERE_WILL_THEY_LIVE_ADDRESS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_ADDRESS)), title = "Where will the person in prison live when they are released?"),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class AccommodationResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
  override val validationType: ValidationType = ValidationType.MANDATORY,
) : IResettlementAssessmentQuestion {
  WHERE_DID_THEY_LIVE(
    id = "WHERE_DID_THEY_LIVE",
    title = "Where did the person in prison live before custody?",
    type = TypeOfQuestion.RADIO,
    options = listOf(
      Option(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
      Option(id = "SOCIAL_HOUSING", displayText = "Social housing"),
      Option(id = "HOMEOWNER", displayText = "Homeowner"),
      Option(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
      Option(id = "NO_ANSWER", displayText = "No answer provided"),
    ),
  ),
  WHERE_DID_THEY_LIVE_ADDRESS(
    id = "WHERE_DID_THEY_LIVE_ADDRESS",
    title = "Enter the address",
    type = TypeOfQuestion.ADDRESS,
  ),
  HELP_TO_KEEP_HOME(
    id = "HELP_TO_KEEP_HOME",
    title = "Does the person in prison or their family need help to keep their home while they are in prison?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  WHERE_WILL_THEY_LIVE_1(
    id = "WHERE_WILL_THEY_LIVE_1",
    title = "Where will the person in prison live when they are released?",
    type = TypeOfQuestion.RADIO,
    options = listOf(
      Option(id = "RETURN_TO_PREVIOUS_ADDRESS", displayText = "Return to their previous address"),
      Option(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
      Option(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
      Option(id = "NO_ANSWER", displayText = "No answer provided"),
    ),
  ),
  WHERE_WILL_THEY_LIVE_2(
    id = "WHERE_WILL_THEY_LIVE_2",
    title = "Where will the person in prison live when they are released?",
    type = TypeOfQuestion.RADIO,
    options = listOf(
      Option(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
      Option(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
      Option(id = "NO_ANSWER", displayText = "No answer provided"),
    ),
  ),
  WHERE_WILL_THEY_LIVE_ADDRESS(
    id = "WHERE_WILL_THEY_LIVE_ADDRESS",
    title = "Enter the address",
    type = TypeOfQuestion.ADDRESS,
  ),
}
