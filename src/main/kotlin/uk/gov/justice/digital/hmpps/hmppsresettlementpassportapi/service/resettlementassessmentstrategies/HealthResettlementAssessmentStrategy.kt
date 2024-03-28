package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.NextPageContext
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository

@Service
class HealthResettlementAssessmentStrategy(
  resettlementAssessmentRepository: ResettlementAssessmentRepository,
  prisonerRepository: PrisonerRepository,
  statusRepository: StatusRepository,
  pathwayRepository: PathwayRepository,
  pathwayStatusRepository: PathwayStatusRepository,
  resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
) : AbstractResettlementAssessmentStrategy<HealthAssessmentPage, HealthResettlementAssessmentQuestion>(
  resettlementAssessmentRepository,
  prisonerRepository,
  statusRepository,
  pathwayRepository,
  pathwayStatusRepository,
  resettlementAssessmentStatusRepository,
  HealthAssessmentPage::class,
  HealthResettlementAssessmentQuestion::class,
) {
  override fun appliesTo(pathway: Pathway) = pathway == Pathway.HEALTH

  override fun getPageList(assessmentType: ResettlementAssessmentType): List<ResettlementAssessmentNode> = listOf(
    ResettlementAssessmentNode(
      HealthAssessmentPage.REGISTERED_WITH_GP,
      nextPage =
      fun(context: NextPageContext): IAssessmentPage {
        val (currentQuestionsAndAnswers) = context
        return if (currentQuestionsAndAnswers.any { it.question == HealthResettlementAssessmentQuestion.REGISTERED_WITH_GP && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          HealthAssessmentPage.MEET_HEALTHCARE_TEAM
        } else if (currentQuestionsAndAnswers.any { it.question == HealthResettlementAssessmentQuestion.REGISTERED_WITH_GP && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          HealthAssessmentPage.HELP_REGISTERING_GP
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${HealthResettlementAssessmentQuestion.HELP_REGISTERING_GP}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      HealthAssessmentPage.HELP_REGISTERING_GP,
      nextPage =
      fun(_: NextPageContext): IAssessmentPage {
        return HealthAssessmentPage.MEET_HEALTHCARE_TEAM
      },
    ),
    ResettlementAssessmentNode(
      HealthAssessmentPage.MEET_HEALTHCARE_TEAM,
      nextPage =
      fun(context: NextPageContext): IAssessmentPage {
        val (currentQuestionsAndAnswers) = context
        return if (currentQuestionsAndAnswers.any { it.question == HealthResettlementAssessmentQuestion.MEET_HEALTHCARE_TEAM && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          HealthAssessmentPage.WHAT_HEALTH_NEED
        } else if (currentQuestionsAndAnswers.any { it.question == HealthResettlementAssessmentQuestion.MEET_HEALTHCARE_TEAM && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          finalQuestionNextPage(context)
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${HealthResettlementAssessmentQuestion.MEET_HEALTHCARE_TEAM}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      HealthAssessmentPage.WHAT_HEALTH_NEED,
      nextPage = ::finalQuestionNextPage,
    ),
    assessmentSummaryNode(assessmentType),
  )
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class HealthAssessmentPage(override val id: String, override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, override val title: String? = null) : IAssessmentPage {
  REGISTERED_WITH_GP(id = "REGISTERED_WITH_GP", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(HealthResettlementAssessmentQuestion.REGISTERED_WITH_GP))),
  HELP_REGISTERING_GP(id = "HELP_REGISTERING_GP", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(HealthResettlementAssessmentQuestion.HELP_REGISTERING_GP))),
  MEET_HEALTHCARE_TEAM(id = "MEET_HEALTHCARE_TEAM", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(HealthResettlementAssessmentQuestion.MEET_HEALTHCARE_TEAM))),
  WHAT_HEALTH_NEED(id = "WHAT_HEALTH_NEED", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(HealthResettlementAssessmentQuestion.WHAT_HEALTH_NEED))),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class HealthResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
  override val validationType: ValidationType = ValidationType.MANDATORY,
) : IResettlementAssessmentQuestion {
  REGISTERED_WITH_GP(
    id = "REGISTERED_WITH_GP",
    title = "Is the person in prison registered with a GP surgery outside of prison?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  HELP_REGISTERING_GP(
    id = "HELP_REGISTERING_GP",
    title = "Does the person in prison want help registering with a  GP surgery?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  MEET_HEALTHCARE_TEAM(
    id = "MEET_HEALTHCARE_TEAM",
    title = "Does the person in prison want to meet with a prison healthcare team?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  WHAT_HEALTH_NEED(
    id = "WHAT_HEALTH_NEED",
    title = "What health need is this related to?",
    type = TypeOfQuestion.CHECKBOX,
    options = listOf(
      Option(id = "PHYSICAL_HEALTH", displayText = "Physical health"),
      Option(id = "MENTAL_HEALTH", displayText = "Mental health"),
      Option(id = "NO_ANSWER", displayText = "No answer provided", exclusive = true),
    ),
  ),
}
