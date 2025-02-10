package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.params.provider.Arguments
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Validation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import java.util.stream.Stream

class AttitudesThinkingAndBehaviourV4ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, 4) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "ATTITUDES_THINKING_AND_BEHAVIOUR_REPORT",
    ),
    // Any answer to ATTITUDES_THINKING_AND_BEHAVIOUR_REPORT, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "ATTITUDES_THINKING_AND_BEHAVIOUR_REPORT",
      "CHECK_ANSWERS",
    ),
  )

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      "ATTITUDES_THINKING_AND_BEHAVIOUR_REPORT",
      ResettlementAssessmentResponsePage(
        id = "ATTITUDES_THINKING_AND_BEHAVIOUR_REPORT",
        title = "Attitudes, thinking and behaviour report",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HELP_TO_MANAGE_ANGER",
              title = "Does the person in prison have any issues managing their emotions?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has any issues managing their emotions"),
              options = yesNoOptions,
            ),
            originalPageId = "ATTITUDES_THINKING_AND_BEHAVIOUR_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HELP_TO_MANAGE_ANGER_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "ATTITUDES_THINKING_AND_BEHAVIOUR_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "ISSUES_WITH_GAMBLING",
              title = "Does the person in prison have any issues with gambling?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has any issues with gambling"),
              options = yesNoOptions,
            ),
            originalPageId = "ATTITUDES_THINKING_AND_BEHAVIOUR_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "ISSUES_WITH_GAMBLING_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "ATTITUDES_THINKING_AND_BEHAVIOUR_REPORT",
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
