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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository

@Service
class ChildrenFamilyAndCommunitiesResettlementAssessmentStrategy(
  resettlementAssessmentRepository: ResettlementAssessmentRepository,
  prisonerRepository: PrisonerRepository,
  statusRepository: StatusRepository,
  pathwayRepository: PathwayRepository,
  resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
) : AbstractResettlementAssessmentStrategy<ChildrenFamilyAndCommunitiesAssessmentPage, ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion>(resettlementAssessmentRepository, prisonerRepository, statusRepository, pathwayRepository, resettlementAssessmentStatusRepository, ChildrenFamilyAndCommunitiesAssessmentPage::class, ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion::class) {
  override fun appliesTo(pathway: Pathway) = pathway == Pathway.CHILDREN_FAMILIES_AND_COMMUNITY

  override fun getPageList(): List<ResettlementAssessmentNode> = listOf(
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.PARTNER_OR_SPOUSE,
      nextPage = fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return ChildrenFamilyAndCommunitiesAssessmentPage.PRIMARY_CARER_FOR_CHILDREN
      },
    ),
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.PRIMARY_CARER_FOR_CHILDREN,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.PRIMARY_CARER_FOR_CHILDREN && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          ChildrenFamilyAndCommunitiesAssessmentPage.CHILDREN_SERVICES_INVOLVED
        } else if (currentQuestionsAndAnswers.any { it.question == ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.PRIMARY_CARER_FOR_CHILDREN && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          ChildrenFamilyAndCommunitiesAssessmentPage.CARING_FOR_ADULT
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.CARING_FOR_ADULT}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.CHILDREN_SERVICES_INVOLVED,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.CHILDREN_SERVICES_INVOLVED && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          ChildrenFamilyAndCommunitiesAssessmentPage.SUPPORT_MEETING_CHILDREN_SERVICES
        } else if (currentQuestionsAndAnswers.any { it.question == ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.CHILDREN_SERVICES_INVOLVED && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          ChildrenFamilyAndCommunitiesAssessmentPage.CARING_FOR_ADULT
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.CARING_FOR_ADULT}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.SUPPORT_MEETING_CHILDREN_SERVICES,
      nextPage = fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return ChildrenFamilyAndCommunitiesAssessmentPage.CARING_FOR_ADULT
      },
    ),
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.CARING_FOR_ADULT,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.CARING_FOR_ADULT && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          ChildrenFamilyAndCommunitiesAssessmentPage.SOCIAL_SERVICES_INVOLVED_FOR_ADULT
        } else if (currentQuestionsAndAnswers.any { it.question == ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.CARING_FOR_ADULT && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          ChildrenFamilyAndCommunitiesAssessmentPage.SUPPORT_FROM_SOCIAL_SERVICES
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.SUPPORT_FROM_SOCIAL_SERVICES}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.SOCIAL_SERVICES_INVOLVED_FOR_ADULT,
      nextPage = fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return ChildrenFamilyAndCommunitiesAssessmentPage.SUPPORT_FROM_SOCIAL_SERVICES
      },
    ),
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.SUPPORT_FROM_SOCIAL_SERVICES,
      nextPage = fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return ChildrenFamilyAndCommunitiesAssessmentPage.FRIEND_FAMILY_COMMUNITY_SUPPORT
      },
    ),
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.FRIEND_FAMILY_COMMUNITY_SUPPORT,
      nextPage = fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return ChildrenFamilyAndCommunitiesAssessmentPage.INVOLVEMENT_IN_GANG_ACTIVITY
      },
    ),
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.INVOLVEMENT_IN_GANG_ACTIVITY,
      nextPage = fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return ChildrenFamilyAndCommunitiesAssessmentPage.UNDER_THREAT_OUTSIDE
      },
    ),
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.UNDER_THREAT_OUTSIDE,
      nextPage = fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return ChildrenFamilyAndCommunitiesAssessmentPage.COMMUNITY_ORGANISATION_SUPPORT
      },
    ),
    ResettlementAssessmentNode(
      ChildrenFamilyAndCommunitiesAssessmentPage.COMMUNITY_ORGANISATION_SUPPORT,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>): IAssessmentPage {
        return GenericAssessmentPage.ASSESSMENT_SUMMARY
      },
    ),
  )
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ChildrenFamilyAndCommunitiesAssessmentPage(override val id: String, override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, override val title: String? = null) :
  IAssessmentPage {
  PARTNER_OR_SPOUSE(id = "PARTNER_OR_SPOUSE", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.PARTNER_OR_SPOUSE))),
  PRIMARY_CARER_FOR_CHILDREN(id = "PRIMARY_CARER_FOR_CHILDREN", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.PRIMARY_CARER_FOR_CHILDREN))),
  CHILDREN_SERVICES_INVOLVED(id = "CHILDREN_SERVICES_INVOLVED", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.CHILDREN_SERVICES_INVOLVED))),
  SUPPORT_MEETING_CHILDREN_SERVICES(id = "SUPPORT_MEETING_CHILDREN_SERVICES", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.SUPPORT_MEETING_CHILDREN_SERVICES))),
  CARING_FOR_ADULT(id = "CARING_FOR_ADULT", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.CARING_FOR_ADULT))),
  SOCIAL_SERVICES_INVOLVED_FOR_ADULT(id = "SOCIAL_SERVICES_INVOLVED_FOR_ADULT", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.SOCIAL_SERVICES_INVOLVED_FOR_ADULT))),
  SUPPORT_FROM_SOCIAL_SERVICES(id = "SUPPORT_FROM_SOCIAL_SERVICES", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.SUPPORT_FROM_SOCIAL_SERVICES))),
  FRIEND_FAMILY_COMMUNITY_SUPPORT(id = "FRIEND_FAMILY_COMMUNITY_SUPPORT", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.FRIEND_FAMILY_COMMUNITY_SUPPORT))),
  INVOLVEMENT_IN_GANG_ACTIVITY(id = "INVOLVEMENT_IN_GANG_ACTIVITY", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.INVOLVEMENT_IN_GANG_ACTIVITY))),
  UNDER_THREAT_OUTSIDE(id = "UNDER_THREAT_OUTSIDE", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.UNDER_THREAT_OUTSIDE))),
  COMMUNITY_ORGANISATION_SUPPORT(id = "COMMUNITY_ORGANISATION_SUPPORT", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion.COMMUNITY_ORGANISATION_SUPPORT))),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ChildrenFamilyAndCommunitiesResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
) : IResettlementAssessmentQuestion {
  PARTNER_OR_SPOUSE(
    id = "PARTNER_OR_SPOUSE",
    title = "Does the person in prison have a partner or spouse?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  PRIMARY_CARER_FOR_CHILDREN(
    id = "PRIMARY_CARER_FOR_CHILDREN",
    title = "Is the person in prison the primary carer for any children?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  CHILDREN_SERVICES_INVOLVED(
    id = "CHILDREN_SERVICES_INVOLVED",
    title = "Are children's services involved with the person in prison and the children they look after?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  SUPPORT_MEETING_CHILDREN_SERVICES(
    id = "SUPPORT_MEETING_CHILDREN_SERVICES",
    title = "Does the person in prison want support when they meet with children's services?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  CARING_FOR_ADULT(
    id = "CARING_FOR_ADULT",
    title = "Does the person in prison have caring responsibilities for any adults?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  SOCIAL_SERVICES_INVOLVED_FOR_ADULT(
    id = "SOCIAL_SERVICES_INVOLVED_FOR_ADULT",
    title = "Are social services involved with the person in prison and the adult they provide care for?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  SUPPORT_FROM_SOCIAL_SERVICES(
    id = "SUPPORT_FROM_SOCIAL_SERVICES",
    title = "Has the person in prison themselves ever received support from social services?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  FRIEND_FAMILY_COMMUNITY_SUPPORT(
    id = "FRIEND_FAMILY_COMMUNITY_SUPPORT",
    title = "Will the person in prison have support from family, friends or their community outside of prison?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  INVOLVEMENT_IN_GANG_ACTIVITY(
    id = "INVOLVEMENT_IN_GANG_ACTIVITY",
    title = "Has the person in prison had any involvement in gang activity?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  UNDER_THREAT_OUTSIDE(
    id = "UNDER_THREAT_OUTSIDE",
    title = "Is the person in prison under threat outside of prison?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  COMMUNITY_ORGANISATION_SUPPORT(
    id = "COMMUNITY_ORGANISATION_SUPPORT",
    title = "Does the person in prison need support from community organisations outside of prison?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
}
