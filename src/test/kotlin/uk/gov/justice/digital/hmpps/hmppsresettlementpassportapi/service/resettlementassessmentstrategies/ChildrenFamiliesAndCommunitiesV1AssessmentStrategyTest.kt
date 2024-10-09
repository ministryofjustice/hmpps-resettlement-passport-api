package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.util.stream.Stream

class ChildrenFamiliesAndCommunitiesV1AssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, 1) {

  @ParameterizedTest
  @MethodSource("test next page function flow - no existing assessment data")
  fun `test next page function flow - no existing assessment`(
    questionsAndAnswers: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>,
    currentPage: String?,
    expectedPage: String,
  ) {
    val nomsId = "123"
    setUpMocks(nomsId, false)

    val assessment = ResettlementAssessmentRequest(
      questionsAndAnswers = questionsAndAnswers,
    )
    val nextPage = resettlementAssessmentStrategy.getNextPageId(
      assessment = assessment,
      nomsId = nomsId,
      pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
      assessmentType = ResettlementAssessmentType.BCST2,
      currentPage = currentPage,
    )
    Assertions.assertEquals(expectedPage, nextPage)
  }
  private fun `test next page function flow - no existing assessment data`() = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "PARTNER_OR_SPOUSE",
    ),
    // Any answer to PARTNER_OR_SPOUSE, go to PRIMARY_CARER_FOR_CHILDREN
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
      ),
      "PARTNER_OR_SPOUSE",
      "PRIMARY_CARER_FOR_CHILDREN",
    ),
    // If the answer to PRIMARY_CARER_FOR_CHILDREN is YES, go to CHILDREN_SERVICES_INVOLVED
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
      ),
      "PRIMARY_CARER_FOR_CHILDREN",
      "CHILDREN_SERVICES_INVOLVED",
    ),
    // If the answer to PRIMARY_CARER_FOR_CHILDREN is NO, go to CARING_FOR_ADULT
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("NO")),
      ),
      "PRIMARY_CARER_FOR_CHILDREN",
      "CARING_FOR_ADULT",
    ),
    // If the answer to CHILDREN_SERVICES_INVOLVED is YES, go to SUPPORT_MEETING_CHILDREN_SERVICES
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
      ),
      "CHILDREN_SERVICES_INVOLVED",
      "SUPPORT_MEETING_CHILDREN_SERVICES",
    ),
    // If the answer to CHILDREN_SERVICES_INVOLVED is NO, go to CARING_FOR_ADULT
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("NO")),
      ),
      "CHILDREN_SERVICES_INVOLVED",
      "CARING_FOR_ADULT",
    ),
    // Any answer to SUPPORT_MEETING_CHILDREN_SERVICES, go to CARING_FOR_ADULT
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_MEETING_CHILDREN_SERVICES", answer = StringAnswer("NO")),
      ),
      "SUPPORT_MEETING_CHILDREN_SERVICES",
      "CARING_FOR_ADULT",
    ),
    // If the answer to CARING_FOR_ADULT is YES, go to SOCIAL_SERVICES_INVOLVED_FOR_ADULT
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_MEETING_CHILDREN_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CARING_FOR_ADULT", answer = StringAnswer("YES")),
      ),
      "CARING_FOR_ADULT",
      "SOCIAL_SERVICES_INVOLVED_FOR_ADULT",
    ),
    // If the answer to CARING_FOR_ADULT is NO, go to SUPPORT_FROM_SOCIAL_SERVICES
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_MEETING_CHILDREN_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CARING_FOR_ADULT", answer = StringAnswer("NO")),
      ),
      "CARING_FOR_ADULT",
      "SUPPORT_FROM_SOCIAL_SERVICES",
    ),
    // Any answer to SOCIAL_SERVICES_INVOLVED_FOR_ADULT, go to SUPPORT_FROM_SOCIAL_SERVICES
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_MEETING_CHILDREN_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CARING_FOR_ADULT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SOCIAL_SERVICES_INVOLVED_FOR_ADULT", answer = StringAnswer("YES")),
      ),
      "SOCIAL_SERVICES_INVOLVED_FOR_ADULT",
      "SUPPORT_FROM_SOCIAL_SERVICES",
    ),
    // Any answer to SUPPORT_FROM_SOCIAL_SERVICES, go to FRIEND_FAMILY_COMMUNITY_SUPPORT
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_MEETING_CHILDREN_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CARING_FOR_ADULT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SOCIAL_SERVICES_INVOLVED_FOR_ADULT", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_FROM_SOCIAL_SERVICES", answer = StringAnswer("YES")),
      ),
      "SUPPORT_FROM_SOCIAL_SERVICES",
      "FRIEND_FAMILY_COMMUNITY_SUPPORT",
    ),
    // Any answer to FRIEND_FAMILY_COMMUNITY_SUPPORT, go to INVOLVEMENT_IN_GANG_ACTIVITY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_MEETING_CHILDREN_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CARING_FOR_ADULT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SOCIAL_SERVICES_INVOLVED_FOR_ADULT", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_FROM_SOCIAL_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("FRIEND_FAMILY_COMMUNITY_SUPPORT", answer = StringAnswer("NO")),
      ),
      "FRIEND_FAMILY_COMMUNITY_SUPPORT",
      "INVOLVEMENT_IN_GANG_ACTIVITY",
    ),
    // Any answer to INVOLVEMENT_IN_GANG_ACTIVITY, go to UNDER_THREAT_OUTSIDE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_MEETING_CHILDREN_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CARING_FOR_ADULT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SOCIAL_SERVICES_INVOLVED_FOR_ADULT", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_FROM_SOCIAL_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("INVOLVEMENT_IN_GANG_ACTIVITY", answer = StringAnswer("NO")),
      ),
      "INVOLVEMENT_IN_GANG_ACTIVITY",
      "UNDER_THREAT_OUTSIDE",
    ),
    // Any answer to UNDER_THREAT_OUTSIDE, go to COMMUNITY_ORGANISATION_SUPPORT
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_MEETING_CHILDREN_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CARING_FOR_ADULT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SOCIAL_SERVICES_INVOLVED_FOR_ADULT", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_FROM_SOCIAL_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("INVOLVEMENT_IN_GANG_ACTIVITY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("UNDER_THREAT_OUTSIDE", answer = StringAnswer("YES")),
      ),
      "UNDER_THREAT_OUTSIDE",
      "COMMUNITY_ORGANISATION_SUPPORT",
    ),
    // Any answer to COMMUNITY_ORGANISATION_SUPPORT, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_MEETING_CHILDREN_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CARING_FOR_ADULT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SOCIAL_SERVICES_INVOLVED_FOR_ADULT", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_FROM_SOCIAL_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("INVOLVEMENT_IN_GANG_ACTIVITY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("UNDER_THREAT_OUTSIDE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("COMMUNITY_ORGANISATION_SUPPORT", answer = StringAnswer("NO")),
      ),
      "COMMUNITY_ORGANISATION_SUPPORT",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("PARTNER_OR_SPOUSE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("PRIMARY_CARER_FOR_CHILDREN", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CHILDREN_SERVICES_INVOLVED", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_MEETING_CHILDREN_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CARING_FOR_ADULT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SOCIAL_SERVICES_INVOLVED_FOR_ADULT", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_FROM_SOCIAL_SERVICES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("INVOLVEMENT_IN_GANG_ACTIVITY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("UNDER_THREAT_OUTSIDE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("COMMUNITY_ORGANISATION_SUPPORT", answer = StringAnswer("NO")),
      ),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  @ParameterizedTest
  @MethodSource("test get page from Id - no existing assessment data")
  fun `test get page from Id - no existing assessment`(pageIdInput: String, expectedPage: ResettlementAssessmentResponsePage) {
    val nomsId = "123"
    setUpMocks("123", false)

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "PARTNER_OR_SPOUSE",
      ResettlementAssessmentResponsePage(
        id = "PARTNER_OR_SPOUSE",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PARTNER_OR_SPOUSE",
              title = "Does the person in prison have a partner or spouse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "PARTNER_OR_SPOUSE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "PRIMARY_CARER_FOR_CHILDREN",
      ResettlementAssessmentResponsePage(
        id = "PRIMARY_CARER_FOR_CHILDREN",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PRIMARY_CARER_FOR_CHILDREN",
              title = "Is the person in prison the primary carer for any children?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "PRIMARY_CARER_FOR_CHILDREN",
          ),
        ),
      ),
    ),
    Arguments.of(
      "CHILDREN_SERVICES_INVOLVED",
      ResettlementAssessmentResponsePage(
        id = "CHILDREN_SERVICES_INVOLVED",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CHILDREN_SERVICES_INVOLVED",
              title = "Are children's services involved with the person in prison and the children they look after?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_SERVICES_INVOLVED",
          ),
        ),
      ),
    ),
    Arguments.of(
      "SUPPORT_MEETING_CHILDREN_SERVICES",
      ResettlementAssessmentResponsePage(
        id = "SUPPORT_MEETING_CHILDREN_SERVICES",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_MEETING_CHILDREN_SERVICES",
              title = "Does the person in prison want support when they meet with children's services?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "SUPPORT_MEETING_CHILDREN_SERVICES",
          ),
        ),
      ),
    ),
    Arguments.of(
      "CARING_FOR_ADULT",
      ResettlementAssessmentResponsePage(
        id = "CARING_FOR_ADULT",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CARING_FOR_ADULT",
              title = "Does the person in prison have caring responsibilities for any adults?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CARING_FOR_ADULT",
          ),
        ),
      ),
    ),
    Arguments.of(
      "SOCIAL_SERVICES_INVOLVED_FOR_ADULT",
      ResettlementAssessmentResponsePage(
        id = "SOCIAL_SERVICES_INVOLVED_FOR_ADULT",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SOCIAL_SERVICES_INVOLVED_FOR_ADULT",
              title = "Are social services involved with the person in prison and the adult they provide care for?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "SOCIAL_SERVICES_INVOLVED_FOR_ADULT",
          ),
        ),
      ),
    ),
    Arguments.of(
      "SUPPORT_FROM_SOCIAL_SERVICES",
      ResettlementAssessmentResponsePage(
        id = "SUPPORT_FROM_SOCIAL_SERVICES",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_FROM_SOCIAL_SERVICES",
              title = "Has the person in prison themselves ever received support from social services?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "SUPPORT_FROM_SOCIAL_SERVICES",
          ),
        ),
      ),
    ),
    Arguments.of(
      "FRIEND_FAMILY_COMMUNITY_SUPPORT",
      ResettlementAssessmentResponsePage(
        id = "FRIEND_FAMILY_COMMUNITY_SUPPORT",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "FRIEND_FAMILY_COMMUNITY_SUPPORT",
              title = "Will the person in prison have support from family, friends or their community outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "FRIEND_FAMILY_COMMUNITY_SUPPORT",
          ),
        ),
      ),
    ),
    Arguments.of(
      "INVOLVEMENT_IN_GANG_ACTIVITY",
      ResettlementAssessmentResponsePage(
        id = "INVOLVEMENT_IN_GANG_ACTIVITY",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "INVOLVEMENT_IN_GANG_ACTIVITY",
              title = "Has the person in prison had any involvement in gang activity?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "INVOLVEMENT_IN_GANG_ACTIVITY",
          ),
        ),
      ),
    ),
    Arguments.of(
      "UNDER_THREAT_OUTSIDE",
      ResettlementAssessmentResponsePage(
        id = "UNDER_THREAT_OUTSIDE",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "UNDER_THREAT_OUTSIDE",
              title = "Is the person in prison under threat outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "UNDER_THREAT_OUTSIDE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "COMMUNITY_ORGANISATION_SUPPORT",
      ResettlementAssessmentResponsePage(
        id = "COMMUNITY_ORGANISATION_SUPPORT",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "COMMUNITY_ORGANISATION_SUPPORT",
              title = "Does the person in prison need support from community organisations outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "COMMUNITY_ORGANISATION_SUPPORT",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ASSESSMENT_SUMMARY",
      ResettlementAssessmentResponsePage(
        id = "ASSESSMENT_SUMMARY",
        title = "Children, families and communities report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Children, families and communities support needs",
              subTitle = "Select one option.",
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "SUPPORT_REQUIRED",
                  displayText = "Support required",
                  description = "a need for support has been identified and is accepted",
                ),
                ResettlementAssessmentOption(id = "SUPPORT_NOT_REQUIRED", displayText = "Support not required", description = "no need was identified"),
                ResettlementAssessmentOption(
                  id = "SUPPORT_DECLINED",
                  displayText = "Support declined",
                  description = "a need has been identified but support is declined",
                ),
              ),
            ),
            originalPageId = "ASSESSMENT_SUMMARY",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CASE_NOTE_SUMMARY",
              title = "Case note summary",
              subTitle = "This will be displayed as a case note in both DPS and nDelius",
              type = TypeOfQuestion.LONG_TEXT,
            ),
            originalPageId = "ASSESSMENT_SUMMARY",
          ),
        ),
      ),
    ),
    Arguments.of(
      "CHECK_ANSWERS",
      ResettlementAssessmentResponsePage(
        id = "CHECK_ANSWERS",
        questionsAndAnswers = listOf(),
      ),
    ),
  )

  @Test
  fun `test next page function start - existing assessment`() {
    // If there is an existing assessment we should go to CHECK_ANSWERS at the start
    val nomsId = "123456"
    setUpMocks(nomsId, true)

    val assessment = ResettlementAssessmentRequest(
      questionsAndAnswers = null,
    )
    val nextPage = resettlementAssessmentStrategy.getNextPageId(
      assessment = assessment,
      nomsId = nomsId,
      pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
      assessmentType = ResettlementAssessmentType.BCST2,
      currentPage = null,
    )
    Assertions.assertEquals("CHECK_ANSWERS", nextPage)
  }

  @Test
  fun `test next page function - error case from CHECK_ANSWERS`() {
    // We should send back an error if CHECK_ANSWERS is the current page as there is no next page to get
    val nomsId = "123456"
    val assessment = ResettlementAssessmentRequest(
      questionsAndAnswers = listOf(ResettlementAssessmentRequestQuestionAndAnswer("ANY_QUESTION", StringAnswer("Any answer"))),
    )

    setUpMocks(nomsId, false)

    val exception = assertThrows<ServerWebInputException> {
      resettlementAssessmentStrategy.getNextPageId(
        assessment = assessment,
        nomsId = nomsId,
        pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
        assessmentType = ResettlementAssessmentType.BCST2,
        currentPage = "CHECK_ANSWERS",
      )
    }
    Assertions.assertEquals("400 BAD_REQUEST \"Cannot get the next question from CHECK_ANSWERS as this is the end of the flow for this pathway.\"", exception.message)
  }
}
