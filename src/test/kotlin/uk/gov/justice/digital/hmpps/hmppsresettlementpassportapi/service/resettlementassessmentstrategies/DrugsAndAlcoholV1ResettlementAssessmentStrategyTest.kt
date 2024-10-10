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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.util.stream.Stream

class DrugsAndAlcoholV1ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.DRUGS_AND_ALCOHOL, 1) {

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
      pathway = Pathway.DRUGS_AND_ALCOHOL,
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
      "DRUG_ISSUES",
    ),
    // If the answer to DRUG_ISSUES is YES, go to SUPPORT_WITH_DRUG_ISSUES
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("DRUG_ISSUES", answer = StringAnswer("YES")),
      ),
      "DRUG_ISSUES",
      "SUPPORT_WITH_DRUG_ISSUES",
    ),
    // If the answer to DRUG_ISSUES is NO, go to ALCOHOL_ISSUES
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("DRUG_ISSUES", answer = StringAnswer("NO")),
      ),
      "DRUG_ISSUES",
      "ALCOHOL_ISSUES",
    ),
    // Any answer to SUPPORT_WITH_DRUG_ISSUES, go to ALCOHOL_ISSUES
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("DRUG_ISSUES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_WITH_DRUG_ISSUES", answer = StringAnswer("NO")),
      ),
      "SUPPORT_WITH_DRUG_ISSUES",
      "ALCOHOL_ISSUES",
    ),
    // If the answer to ALCOHOL_ISSUES is YES, go to SUPPORT_WITH_ALCOHOL_ISSUES
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("DRUG_ISSUES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_WITH_DRUG_ISSUES", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("ALCOHOL_ISSUES", answer = StringAnswer("YES")),
      ),
      "ALCOHOL_ISSUES",
      "SUPPORT_WITH_ALCOHOL_ISSUES",
    ),
    // If the answer to ALCOHOL_ISSUES is NO, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("DRUG_ISSUES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_WITH_DRUG_ISSUES", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("ALCOHOL_ISSUES", answer = StringAnswer("NO")),
      ),
      "ALCOHOL_ISSUES",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to SUPPORT_WITH_ALCOHOL_ISSUES, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("DRUG_ISSUES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_WITH_DRUG_ISSUES", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("ALCOHOL_ISSUES", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_WITH_ALCOHOL_ISSUES", answer = StringAnswer("YES")),
      ),
      "SUPPORT_WITH_ALCOHOL_ISSUES",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("DRUG_ISSUES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_WITH_DRUG_ISSUES", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("ALCOHOL_ISSUES", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_WITH_ALCOHOL_ISSUES", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("CASE_NOTE_SUMMARY", answer = StringAnswer("My case note summary.")),
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
      pathway = Pathway.DRUGS_AND_ALCOHOL,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "DRUG_ISSUES",
      ResettlementAssessmentResponsePage(
        id = "DRUG_ISSUES",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DRUG_ISSUES",
              title = "Does the person in prison have any previous or current drug misuse issues?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "DRUG_ISSUES",
          ),
        ),
      ),
    ),
    Arguments.of(
      "SUPPORT_WITH_DRUG_ISSUES",
      ResettlementAssessmentResponsePage(
        id = "SUPPORT_WITH_DRUG_ISSUES",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_WITH_DRUG_ISSUES",
              title = "Does the person in prison want support with drug issues from the drug and alcohol team to help them prepare for release?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "SUPPORT_WITH_DRUG_ISSUES",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ALCOHOL_ISSUES",
      ResettlementAssessmentResponsePage(
        id = "ALCOHOL_ISSUES",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "ALCOHOL_ISSUES",
              title = "Does the person in prison have any previous or current alcohol misuse issues?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "ALCOHOL_ISSUES",
          ),
        ),
      ),
    ),
    Arguments.of(
      "SUPPORT_WITH_ALCOHOL_ISSUES",
      ResettlementAssessmentResponsePage(
        id = "SUPPORT_WITH_ALCOHOL_ISSUES",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_WITH_ALCOHOL_ISSUES",
              title = "Does the person in prison want support with alcohol issues from the drug and alcohol team to help them prepare for release?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "SUPPORT_WITH_ALCOHOL_ISSUES",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ASSESSMENT_SUMMARY",
      ResettlementAssessmentResponsePage(
        id = "ASSESSMENT_SUMMARY",
        title = "Drugs and alcohol report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Drugs and alcohol support needs",
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
