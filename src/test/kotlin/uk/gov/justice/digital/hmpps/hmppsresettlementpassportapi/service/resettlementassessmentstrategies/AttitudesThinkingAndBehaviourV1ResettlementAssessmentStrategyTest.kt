package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.params.provider.Arguments
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import java.util.stream.Stream

class AttitudesThinkingAndBehaviourV1ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, 1) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "HELP_TO_MANAGE_ANGER",
    ),
    // Any answer to HELP_TO_MANAGE_ANGER, go to ISSUES_WITH_GAMBLING
    Arguments.of(
      listOf(
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_MANAGE_ANGER", answer = StringAnswer("YES")),
      ),
      "HELP_TO_MANAGE_ANGER",
      "ISSUES_WITH_GAMBLING",
    ),
    // Any answer to ISSUES_WITH_GAMBLING, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf(
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_MANAGE_ANGER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("ISSUES_WITH_GAMBLING", answer = StringAnswer("NO_ANSWER")),
      ),
      "ISSUES_WITH_GAMBLING",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf(
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_MANAGE_ANGER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("ISSUES_WITH_GAMBLING", answer = StringAnswer("NO_ANSWER")),
      ),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      "HELP_TO_MANAGE_ANGER",
      ResettlementAssessmentResponsePage(
        id = "HELP_TO_MANAGE_ANGER",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HELP_TO_MANAGE_ANGER",
              title = "Does the person in prison want support managing their emotions?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "HELP_TO_MANAGE_ANGER",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ISSUES_WITH_GAMBLING",
      ResettlementAssessmentResponsePage(
        id = "ISSUES_WITH_GAMBLING",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "ISSUES_WITH_GAMBLING",
              title = "Does the person in prison want support with gambling issues?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "ISSUES_WITH_GAMBLING",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ASSESSMENT_SUMMARY",
      ResettlementAssessmentResponsePage(
        id = "ASSESSMENT_SUMMARY",
        title = "Attitudes, thinking and behaviour report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Attitudes, thinking and behaviour support needs",
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
