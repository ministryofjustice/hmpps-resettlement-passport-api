package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository

@Service
class AttitudesThinkingAndBehaviourResettlementAssessmentStrategy(
  resettlementAssessmentRepository: ResettlementAssessmentRepository,
  prisonerRepository: PrisonerRepository,
  statusRepository: StatusRepository,
  pathwayRepository: PathwayRepository,
  resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
) : AbstractResettlementAssessmentStrategy<AttitudesThinkingAndBehaviourAssessmentPage, AttitudesThinkingAndBehaviourResettlementAssessmentQuestion>(resettlementAssessmentRepository, prisonerRepository, statusRepository, pathwayRepository, resettlementAssessmentStatusRepository, AttitudesThinkingAndBehaviourAssessmentPage::class, AttitudesThinkingAndBehaviourResettlementAssessmentQuestion::class) {
  override fun appliesTo(pathway: Pathway) = pathway == Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR

  override fun getPageList(): List<ResettlementAssessmentNode> = listOf(
    ResettlementAssessmentNode(
      AttitudesThinkingAndBehaviourAssessmentPage.HELP_TO_MANAGE_ANGER,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return AttitudesThinkingAndBehaviourAssessmentPage.INFLUENCED_BY_OTHERS
      },
    ),
    ResettlementAssessmentNode(
      AttitudesThinkingAndBehaviourAssessmentPage.INFLUENCED_BY_OTHERS,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return AttitudesThinkingAndBehaviourAssessmentPage.ISSUES_WITH_GAMBLING
      },
    ),
    ResettlementAssessmentNode(
      AttitudesThinkingAndBehaviourAssessmentPage.ISSUES_WITH_GAMBLING,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return AttitudesThinkingAndBehaviourAssessmentPage.INVOLVED_IN_GANG_ACTIVITY
      },
    ),
    ResettlementAssessmentNode(
      AttitudesThinkingAndBehaviourAssessmentPage.INVOLVED_IN_GANG_ACTIVITY,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return AttitudesThinkingAndBehaviourAssessmentPage.UNDER_THREAT_OUTSIDE_PRISON
      },
    ),
    ResettlementAssessmentNode(
      AttitudesThinkingAndBehaviourAssessmentPage.UNDER_THREAT_OUTSIDE_PRISON,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return GenericAssessmentPage.ASSESSMENT_SUMMARY
      },
    ),
  )
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class AttitudesThinkingAndBehaviourAssessmentPage(override val id: String, override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>) : IAssessmentPage {
  HELP_TO_MANAGE_ANGER(id = "HELP_TO_MANAGE_ANGER", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AttitudesThinkingAndBehaviourResettlementAssessmentQuestion.HELP_TO_MANAGE_ANGER))),
  INFLUENCED_BY_OTHERS(id = "INFLUENCED_BY_OTHERS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AttitudesThinkingAndBehaviourResettlementAssessmentQuestion.INFLUENCED_BY_OTHERS))),
  ISSUES_WITH_GAMBLING(id = "ISSUES_WITH_GAMBLING", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AttitudesThinkingAndBehaviourResettlementAssessmentQuestion.ISSUES_WITH_GAMBLING))),
  INVOLVED_IN_GANG_ACTIVITY(id = "INVOLVED_IN_GANG_ACTIVITY", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AttitudesThinkingAndBehaviourResettlementAssessmentQuestion.INVOLVED_IN_GANG_ACTIVITY))),
  UNDER_THREAT_OUTSIDE_PRISON(id = "UNDER_THREAT_OUTSIDE_PRISON", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(AttitudesThinkingAndBehaviourResettlementAssessmentQuestion.UNDER_THREAT_OUTSIDE_PRISON))),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class AttitudesThinkingAndBehaviourResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
) : IResettlementAssessmentQuestion {
  HELP_TO_MANAGE_ANGER(
    id = "HELP_TO_MANAGE_ANGER",
    title = "Does the person in prison need support managing their anger?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  INFLUENCED_BY_OTHERS(
    id = "INFLUENCED_BY_OTHERS",
    title = "Is the person in prison influenced by others to do things they don’t want to do?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  ISSUES_WITH_GAMBLING(
    id = "ISSUES_WITH_GAMBLING",
    title = "Does the person in prison have any issues with gambling?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  INVOLVED_IN_GANG_ACTIVITY(
    id = "INVOLVED_IN_GANG_ACTIVITY",
    title = "Has the person in prison had any involvement in gang activity?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  UNDER_THREAT_OUTSIDE_PRISON(
    id = "UNDER_THREAT_OUTSIDE_PRISON",
    title = "Is the person in prison under threat outside of prison?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
}
