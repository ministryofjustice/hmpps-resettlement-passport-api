package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.params.provider.Arguments
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Validation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import java.util.stream.Stream

class HealthV2ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.HEALTH, 2) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "HEALTH_REPORT",
    ),
    // Any answer to HEALTH_REPORT, go to SUPPORT_REQUIREMENTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "HEALTH_REPORT",
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

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      "HEALTH_REPORT",
      ResettlementAssessmentResponsePage(
        id = "HEALTH_REPORT",
        title = "Health report",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "REGISTERED_WITH_GP",
              title = "Is the person in prison registered with a GP surgery outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison is registered with a GP surgery outside of prison"),
              options = yesNoOptions,
            ),
            originalPageId = "HEALTH_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "REGISTERED_WITH_GP_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "You do not need to include the name and address of the GP surgery.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "HEALTH_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "NEEDS_HELP_WITH_DAY_TO_DAY_LIVING",
              title = "Does the person in prison need help with day-to-day living when they are released because of an illness or disability?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison needs help with day-to-day living when they are released because of an illness or disability"),
              options = yesNoOptions,
            ),
            originalPageId = "HEALTH_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "NEEDS_HELP_WITH_DAY_TO_DAY_LIVING_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "Include details of any support, adaptations or equipment required. Do not include any details about medical treatments or conditions.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "HEALTH_REPORT",
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
              validation = Validation(ValidationType.MANDATORY, message = "Select support needs or 'No support needs identified'"),
              options = listOf(
                ResettlementAssessmentOption(
                  id = "HELP_TO_REGISTER_WITH_GP",
                  displayText = "Help to register with a GP surgery",
                  tag = "REGISTERING_GP_SURGERY",
                ),
                ResettlementAssessmentOption(
                  id = "MEET_WITH_HEALTHCARE_FOR_PHYSICAL_HEALTH_NEED",
                  displayText = "Meet with prison healthcare team for physical health need",
                ),
                ResettlementAssessmentOption(
                  id = "MEET_WITH_HEALTHCARE_FOR_MENTAL_HEALTH_NEED",
                  displayText = "Meet with prison healthcare team for mental health need",
                ),
                ResettlementAssessmentOption(
                  id = "SUPPORT_FROM_SOCIAL_CARE",
                  displayText = "Support from social care when they are released from prison",
                  tag = "CARE_HEALTH_SUPPORT",
                ),
                ResettlementAssessmentOption(
                  id = "OTHER_SUPPORT_NEEDS",
                  displayText = "Other",
                  freeText = true,
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
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "This information will only be displayed in PSfR.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
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
        title = "Health report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Health resettlement status",
              subTitle = "Select one option.",
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select a health resettlement status"),
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
