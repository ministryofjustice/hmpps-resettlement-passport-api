package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.NextPageContext
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository

@Service
class AttitudesThinkingAndBehaviourResettlementAssessmentStrategy(
  resettlementAssessmentRepository: ResettlementAssessmentRepository,
  prisonerRepository: PrisonerRepository,
  pathwayStatusRepository: PathwayStatusRepository,
) :
  AbstractResettlementAssessmentStrategy<AttitudesThinkingAndBehaviourAssessmentPage, AttitudesThinkingAndBehaviourResettlementAssessmentQuestion>(
    resettlementAssessmentRepository,
    prisonerRepository,
    pathwayStatusRepository,
    AttitudesThinkingAndBehaviourAssessmentPage::class,
    AttitudesThinkingAndBehaviourResettlementAssessmentQuestion::class,
  ) {
  override fun appliesTo(pathway: Pathway) = pathway == Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR

  override fun getPageList(assessmentType: ResettlementAssessmentType): List<ResettlementAssessmentNode> = listOf(
    ResettlementAssessmentNode(
      AttitudesThinkingAndBehaviourAssessmentPage.HELP_TO_MANAGE_ANGER,
      nextPage =
      fun(_: NextPageContext): IAssessmentPage {
        return AttitudesThinkingAndBehaviourAssessmentPage.ISSUES_WITH_GAMBLING
      },
    ),
    ResettlementAssessmentNode(
      AttitudesThinkingAndBehaviourAssessmentPage.ISSUES_WITH_GAMBLING,
      nextPage = ::finalQuestionNextPage,
    ),
    assessmentSummaryNode(assessmentType),
  )
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class AttitudesThinkingAndBehaviourAssessmentPage(
  override val id: String,
  override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>,
  override val title: String? = null,
) : IAssessmentPage {
  HELP_TO_MANAGE_ANGER(
    id = "HELP_TO_MANAGE_ANGER",
    questionsAndAnswers = listOf(
      ResettlementAssessmentQuestionAndAnswer(AttitudesThinkingAndBehaviourResettlementAssessmentQuestion.HELP_TO_MANAGE_ANGER),
    ),
  ),
  ISSUES_WITH_GAMBLING(
    id = "ISSUES_WITH_GAMBLING",
    questionsAndAnswers = listOf(
      ResettlementAssessmentQuestionAndAnswer(AttitudesThinkingAndBehaviourResettlementAssessmentQuestion.ISSUES_WITH_GAMBLING),
    ),
  ),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class AttitudesThinkingAndBehaviourResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
  override val validationType: ValidationType = ValidationType.MANDATORY,
) : IResettlementAssessmentQuestion {
  HELP_TO_MANAGE_ANGER(
    id = "HELP_TO_MANAGE_ANGER",
    title = "Does the person in prison want support managing their emotions?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  ISSUES_WITH_GAMBLING(
    id = "ISSUES_WITH_GAMBLING",
    title = "Does the person in prison want support with gambling issues?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
}
