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

class DrugsAndAlcoholV3ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.DRUGS_AND_ALCOHOL, 3) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "DRUGS_AND_ALCOHOL_REPORT",
    ),
    // Any answer to DRUGS_AND_ALCOHOL_REPORT, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "DRUGS_AND_ALCOHOL_REPORT",
      "CHECK_ANSWERS",
    ),
  )

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      "DRUGS_AND_ALCOHOL_REPORT",
      ResettlementAssessmentResponsePage(
        id = "DRUGS_AND_ALCOHOL_REPORT",
        title = "Drugs and alcohol report",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DRUG_MISUSE_ISSUES",
              title = "Does the person in prison have any previous or current drug misuse issues?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has any previous or current drug misuse issues"),
              options = yesNoOptions,
            ),
            originalPageId = "DRUGS_AND_ALCOHOL_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DRUG_MISUSE_ISSUES_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "Specify if they have spoken to the healthcare team about their issues. Do not include any details about medical treatments, conditions or ongoing monitoring.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "DRUGS_AND_ALCOHOL_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "ALCOHOL_MISUSE_ISSUES",
              title = "Does the person in prison have any previous or current alcohol misuse issues?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has any previous or current alcohol misuse issues"),
              options = yesNoOptions,
            ),
            originalPageId = "DRUGS_AND_ALCOHOL_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "ALCOHOL_MISUSE_ISSUES_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "Specify if they have spoken to the healthcare team about their issues. Do not include any details about medical treatments, conditions or ongoing monitoring.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "DRUGS_AND_ALCOHOL_REPORT",
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
