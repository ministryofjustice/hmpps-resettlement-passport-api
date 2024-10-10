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

class FinanceAndIdV2ResettlementAssessmentAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.FINANCE_AND_ID, 2) {

  @ParameterizedTest(name = "current: {1}, expected: {2}")
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
      pathway = Pathway.FINANCE_AND_ID,
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
      "FINANCE_AND_ID_REPORT",
    ),
    // Any answer to FINANCE_AND_ID_REPORT, go to SUPPORT_REQUIREMENTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "FINANCE_AND_ID_REPORT",
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

  @ParameterizedTest(name = "start: {0}")
  @MethodSource("test get page from Id - no existing assessment data")
  fun `test get page from Id - no existing assessment`(pageIdInput: String, expectedPage: ResettlementAssessmentResponsePage) {
    val nomsId = "123"
    setUpMocks("123", false)

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.FINANCE_AND_ID,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
      version = 2,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "FINANCE_AND_ID_REPORT",
      ResettlementAssessmentResponsePage(
        id = "FINANCE_AND_ID_REPORT",
        title = "Finance and ID report",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HAS_BANK_ACCOUNT",
              title = "Does the person in prison have a bank account?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "FINANCE_AND_ID_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WHAT_ID_DOCUMENTS",
              title = "What ID documents does the person in prison have?",
              subTitle = "Select all that apply",
              type = TypeOfQuestion.CHECKBOX,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "BIRTH_CERTIFICATE",
                  displayText = "Birth or adoption certificate",
                ),
                ResettlementAssessmentOption(
                  id = "PASSPORT",
                  displayText = "Passport",
                ),
                ResettlementAssessmentOption(
                  id = "DRIVING_LICENCE",
                  displayText = "Driving licence",
                ),
                ResettlementAssessmentOption(
                  id = "MARRIAGE_CERTIFICATE",
                  displayText = "Marriage or civil partnership certificate",
                ),
                ResettlementAssessmentOption(
                  id = "DIVORCE_CERTIFICATE",
                  displayText = "Divorce decree absolute certificate",
                ),
                ResettlementAssessmentOption(
                  id = "BIOMETRIC_RESIDENCE_PERMIT",
                  displayText = "Biometric residence permit",
                ),
                ResettlementAssessmentOption(
                  id = "DEED_POLL_CERTIFICATE",
                  displayText = "Deed poll certificate",
                ),
                ResettlementAssessmentOption(
                  id = "CITIZEN_CARD",
                  displayText = "CitizenCard",
                ),
                ResettlementAssessmentOption(
                  id = "NO_ID_DOCUMENTS",
                  displayText = "No ID documents",
                  exclusive = true,
                ),
                ResettlementAssessmentOption(
                  id = "NO_ANSWER",
                  displayText = "No answer provided",
                  exclusive = true,
                ),
              ),
            ),
            originalPageId = "FINANCE_AND_ID_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SELECT_BENEFITS",
              title = "What benefits was the person in prison receiving before custody?",
              subTitle = "Select all that apply",
              type = TypeOfQuestion.CHECKBOX,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "ESA",
                  displayText = "Employment and support allowance (ESA)",
                ),
                ResettlementAssessmentOption(
                  id = "HOUSING_BENEFIT",
                  displayText = "Housing benefit",
                ),
                ResettlementAssessmentOption(
                  id = "UNIVERSAL_CREDIT_HOUSING_ELEMENT",
                  displayText = "Universal credit housing element",
                ),
                ResettlementAssessmentOption(
                  id = "UNIVERSAL_CREDIT",
                  displayText = "Universal credit",
                ),
                ResettlementAssessmentOption(
                  id = "PIP",
                  displayText = "Personal independence payment (PIP)",
                ),
                ResettlementAssessmentOption(
                  id = "STATE_PENSION",
                  displayText = "State pension",
                ),
                ResettlementAssessmentOption(
                  id = "NO_BENEFITS",
                  displayText = "No benefits",
                  exclusive = true,
                ),
                ResettlementAssessmentOption(
                  id = "NO_ANSWER",
                  displayText = "No answer provided",
                  exclusive = true,
                ),
              ),
            ),
            originalPageId = "FINANCE_AND_ID_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DEBTS_OR_ARREARS",
              title = "Does the person in prison have any debts or arrears?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "FINANCE_AND_ID_REPORT",
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
                  id = "APPLY_FOR_BANK_ACCOUNT",
                  displayText = "Apply for a bank account",
                  tag = "NO_BANK_ACCOUNT",
                ),
                ResettlementAssessmentOption(
                  id = "APPLY_FOR_ID",
                  displayText = "Apply for ID documents",
                  tag = "MANAGE_ID_DOCUMENTS",
                ),
                ResettlementAssessmentOption(
                  id = "SUPPORT_TO_MANAGE_DEBT",
                  displayText = "Support to manage debts or arrears",
                  tag = "MANAGE_DEBT_ARREARS",
                ),
                ResettlementAssessmentOption(
                  id = "HELP_TO_CONTACT_BANK",
                  displayText = "Help to contact their bank to manage their account",
                  tag = "HELP_TO_CONTACT_BANK",
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
        ),
      ),
    ),
    Arguments.of(
      "ASSESSMENT_SUMMARY",
      ResettlementAssessmentResponsePage(
        id = "ASSESSMENT_SUMMARY",
        title = "Finance and ID report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Finance and ID resettlement status",
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
              subTitle = "Include any relevant information about why you have chosen that resettlement status. Do not include any special category data. This information will be displayed in PSFR on the overview tab and the finance and ID tab.",
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
