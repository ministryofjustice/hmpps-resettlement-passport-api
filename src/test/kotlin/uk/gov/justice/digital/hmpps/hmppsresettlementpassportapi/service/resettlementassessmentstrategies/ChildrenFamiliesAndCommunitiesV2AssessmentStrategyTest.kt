package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.util.stream.Stream

class ChildrenFamiliesAndCommunitiesV2AssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY) {

  @ParameterizedTest(name = "{1} -> {2}")
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
      version = 2,
    )
    Assertions.assertEquals(expectedPage, nextPage)
  }

  private fun `test next page function flow - no existing assessment data`() = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
    ),
    // Any answer to CHILDREN_FAMILIES_AND_COMMUNITY_REPORT, go to SUPPORT_REQUIREMENTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
      "SUPPORT_REQUIREMENTS",
    ),
    // Any answer to SUPPORT_REQUIREMENTS, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "SUPPORT_REQUIREMENTS",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  @ParameterizedTest(name = "{0} page")
  @MethodSource("test get page from Id - no existing assessment data")
  fun `test get page from Id - no existing assessment`(pageIdInput: String, expectedPage: ResettlementAssessmentResponsePage) {
    val nomsId = "123"
    setUpMocks("123", false)

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
      version = 2,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
      ResettlementAssessmentResponsePage(
        id = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
        title = "Children, families and communities report",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PARTNER_OR_SPOUSE",
              title = "Does the person in prison have a partner or spouse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PARENTAL_RESPONSIBILITY",
              title = "Does the person in prison have parental responsibility for any children under 16?",
              subTitle = "Parental responsibility means they have legal rights and duties relating to the children's upbringing. It does not mean they are allowed contact.",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PRIMARY_CARER_FOR_CHILDREN",
              title = "Is the person in prison the primary carer for any children under 16?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "YES",
                  displayText = "Yes",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "NUMBER_OF_CHILDREN",
                        title = "Number of children",
                        type = TypeOfQuestion.SHORT_TEXT,
                        validationRegex = "^\\d+$"
                      ),
                      originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "NO",
                  displayText = "No",
                ),
                ResettlementAssessmentOption(
                  id = "NO_ANSWER",
                  displayText = "No answer provided",
                ),
              ),
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CHILDREN_SERVICE_INVOLVED",
              title = "Are children's services involved with the person in prison and the children they look after?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CARING_RESPONSIBILITIES_FOR_ADULTS",
              title = "Does the person in prison have caring responsibilities for any adults?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SOCIAL_SERVICE_INVOLVED",
              title = "Are social services involved with the person in prison and any adults they provide care for?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "RECEIVED_SUPPORT_FROM_SOCIAL_SERVICES",
              title = "Has the person in prison ever received support from social services or been in care?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DO_THEY_HAVE_SUPPORT_FROM_FAMILY_FRIENDS_COMMUNITY",
              title = "Does the person in prison have support from family, friends or their community outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "INVOLVEMENT_IN_GANG_ACTIVITY",
              title = "Has the person in prison had any involvement in gang activity?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "UNDER_THREAT_OUTSIDE_PRISON",
              title = "Is the person in prison under threat outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "VICTIM_OF_DOMESTIC_ABUSE",
              title = "Has the person in prison ever been the victim of domestic abuse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PERPETRATOR_OF_DOMESTIC_ABUSE",
              title = "Has the person in prison ever been the perpetrator of domestic abuse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "VICTIM_OF_SEXUAL_ABUSE",
              title = "Has the person in prison ever been the victim of sexual abuse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PERPETRATOR_OF_SEXUAL_ABUSE",
              title = "Has the person in prison ever been the perpetrator of sexual abuse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WORKED_IN_SEX_INDUSTRY",
              title = "Has the person in prison ever worked in the sex industry?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
        ),
      ),
    ),
    Arguments.of(
      "SUPPORT_REQUIREMENTS",
      ResettlementAssessmentResponsePage(
        id = "SUPPORT_REQUIREMENTS",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_REQUIREMENTS",
              title = "Support needs",
              subTitle = "Select any needs you have identified that could be met by prison or probation staff.",
              type = TypeOfQuestion.CHECKBOX,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "SUPPORT_WHEN_MEETING_CHILDREN_SERVICES",
                  displayText = "Support when they meet with children's services",
                ),
                ResettlementAssessmentOption(
                  id = "SUPPORT_FROM_COMMUNITY_ORGANISATION_OUTSIDE_PRISON",
                  displayText = "Support from community organisations outside of prison",
                ),
                ResettlementAssessmentOption(
                  id = "INTERNAL_SUPPORT_SERVICES",
                  displayText = "Internal support services",
                ),
                ResettlementAssessmentOption(
                  id = "MENTORING_SUPPORT_ON_RELEASE",
                  displayText = "Mentoring support on release",
                ),
                ResettlementAssessmentOption(
                  id = "NO_SUPPORT_NEEDS",
                  displayText = "No support needs identified",
                  exclusive = true,
                ),
              ),
            ),
            originalPageId = "SUPPORT_REQUIREMENTS",
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
              title = "Children, families and communities resettlement status",
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
              title = "Case note",
              subTitle = "Include any relevant information about why you have chosen that resettlement status. This information will be only displayed in PSfR. Do not include any information that could identify anyone other than the person in prison, or any  special category data.",
              type = TypeOfQuestion.LONG_TEXT,
              detailsTitle = "Help with special category data",
              detailsContent = "Special category data includes any personal data concerning someone's health, sex life or sexual orientation. Or any personal data revealing someone's racial or ethnic origin, religious or philosophical beliefs or trade union membership.",
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
}
