package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
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

class HealthV1ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.HEALTH, 1) {

  @ParameterizedTest(name = "nextPage {1} -> {2}")
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
      pathway = Pathway.HEALTH,
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
      "REGISTERED_WITH_GP",
    ),
    // If the answer to REGISTERED_WITH_GP is NO, go to HELP_REGISTERING_GP
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
      ),
      "REGISTERED_WITH_GP",
      "HELP_REGISTERING_GP",
    ),
    // If the answer to REGISTERED_WITH_GP is YES, go to MEET_HEALTHCARE_TEAM
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("YES")),
      ),
      "REGISTERED_WITH_GP",
      "MEET_HEALTHCARE_TEAM",
    ),
    // If the answer to REGISTERED_WITH_GP is NO_ANSWER, go to HELP_REGISTERING_GP
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO_ANSWER")),
      ),
      "REGISTERED_WITH_GP",
      "HELP_REGISTERING_GP",
    ),
    // Any answer to HELP_REGISTERING_GP, go to MEET_HEALTHCARE_TEAM
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_REGISTERING_GP", answer = StringAnswer("NO")),
      ),
      "HELP_REGISTERING_GP",
      "MEET_HEALTHCARE_TEAM",
    ),
    // If the answer to MEET_HEALTHCARE_TEAM is YES, go to WHAT_HEALTH_NEED
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_REGISTERING_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("MEET_HEALTHCARE_TEAM", answer = StringAnswer("YES")),
      ),
      "MEET_HEALTHCARE_TEAM",
      "WHAT_HEALTH_NEED",
    ),
    // If the answer to MEET_HEALTHCARE_TEAM is NO, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_REGISTERING_GP", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("MEET_HEALTHCARE_TEAM", answer = StringAnswer("NO")),
      ),
      "MEET_HEALTHCARE_TEAM",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to WHAT_HEALTH_NEED, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_REGISTERING_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("MEET_HEALTHCARE_TEAM", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_HEALTH_NEED", answer = ListAnswer(listOf("Physical health", "Mental health"))),
      ),
      "WHAT_HEALTH_NEED",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_REGISTERING_GP", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("MEET_HEALTHCARE_TEAM", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_HEALTH_NEED", answer = ListAnswer(listOf("Physical health", "Mental health"))),
        ResettlementAssessmentRequestQuestionAndAnswer("CASE_NOTE_SUMMARY", answer = StringAnswer("My case note summary.")),
      ),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  @ParameterizedTest(name = "get page {1}")
  @MethodSource("test get page from Id - no existing assessment data")
  fun `test get page from Id - no existing assessment`(pageIdInput: String, expectedPage: ResettlementAssessmentResponsePage) {
    val nomsId = "123"
    setUpMocks("123", false)

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.HEALTH,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
    )
    Assertions.assertEquals(expectedPage.id, page.id)
    Assertions.assertEquals(expectedPage.questionsAndAnswers, page.questionsAndAnswers)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "REGISTERED_WITH_GP",
      ResettlementAssessmentResponsePage(
        id = "REGISTERED_WITH_GP",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "REGISTERED_WITH_GP",
              title = "Is the person in prison registered with a GP surgery outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "REGISTERED_WITH_GP",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_REGISTERING_GP",
      ResettlementAssessmentResponsePage(
        id = "HELP_REGISTERING_GP",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HELP_REGISTERING_GP",
              title = "Does the person in prison want help registering with a GP surgery?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "HELP_REGISTERING_GP",
          ),
        ),
      ),
    ),
    Arguments.of(
      "MEET_HEALTHCARE_TEAM",
      ResettlementAssessmentResponsePage(
        id = "MEET_HEALTHCARE_TEAM",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "MEET_HEALTHCARE_TEAM",
              title = "Does the person in prison want to meet with a prison healthcare team?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "MEET_HEALTHCARE_TEAM",
          ),
        ),
      ),
    ),
    Arguments.of(
      "WHAT_HEALTH_NEED",
      ResettlementAssessmentResponsePage(
        id = "WHAT_HEALTH_NEED",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WHAT_HEALTH_NEED",
              title = "What health need is this related to?",
              subTitle = null,
              type = TypeOfQuestion.CHECKBOX,
              options = listOf(
                ResettlementAssessmentOption(id = "PHYSICAL_HEALTH", displayText = "Physical health"),
                ResettlementAssessmentOption(id = "MENTAL_HEALTH", displayText = "Mental health"),
                ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided", exclusive = true),
              ),
            ),
            originalPageId = "WHAT_HEALTH_NEED",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ASSESSMENT_SUMMARY",
      ResettlementAssessmentResponsePage(
        id = "ASSESSMENT_SUMMARY",
        title = "Health report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Health support needs",
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
}
