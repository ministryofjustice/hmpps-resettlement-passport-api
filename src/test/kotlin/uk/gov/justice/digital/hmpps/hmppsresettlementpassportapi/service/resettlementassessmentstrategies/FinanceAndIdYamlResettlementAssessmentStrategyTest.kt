package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.time.LocalDate
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class FinanceAndIdYamlResettlementAssessmentStrategyTest : YamlResettlementStrategyTest() {

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
    val nextPage = resettlementAssessmentService.getNextPageId(
      assessment = assessment,
      nomsId = nomsId,
      pathway = Pathway.FINANCE_AND_ID,
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
      "HAS_BANK_ACCOUNT",
    ),
    // If the answer to HAS_BANK_ACCOUNT is NO, go to HELP_WITH_BANK_ACCOUNT
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
      ),
      "HAS_BANK_ACCOUNT",
      "HELP_WITH_BANK_ACCOUNT",
    ),
    // If the answer to HAS_BANK_ACCOUNT is YES, go to WHAT_ID_DOCUMENTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("YES")),
      ),
      "HAS_BANK_ACCOUNT",
      "WHAT_ID_DOCUMENTS",
    ),
    // Any answer to HELP_WITH_BANK_ACCOUNT, go to WHAT_ID_DOCUMENTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_WITH_BANK_ACCOUNT", answer = StringAnswer("NO")),
      ),
      "HELP_WITH_BANK_ACCOUNT",
      "WHAT_ID_DOCUMENTS",
    ),
    // Any answer to WHAT_ID_DOCUMENTS, go to HELP_APPLY_FOR_ID
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_WITH_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_ID_DOCUMENTS", answer = ListAnswer(listOf("Passport", "Biometric residence permit"))),
      ),
      "WHAT_ID_DOCUMENTS",
      "HELP_APPLY_FOR_ID",
    ),
    // Any answer to HELP_APPLY_FOR_ID, go to RECEIVING_BENEFITS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_WITH_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_ID_DOCUMENTS", answer = ListAnswer(listOf("Passport", "Biometric residence permit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_APPLY_FOR_ID", answer = StringAnswer("YES")),
      ),
      "HELP_APPLY_FOR_ID",
      "RECEIVING_BENEFITS",
    ),
    // If the answer to RECEIVING_BENEFITS is NO, go to DEBTS_OR_ARREARS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_WITH_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_ID_DOCUMENTS", answer = ListAnswer(listOf("Passport", "Biometric residence permit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_APPLY_FOR_ID", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RECEIVING_BENEFITS", answer = StringAnswer("NO")),
      ),
      "RECEIVING_BENEFITS",
      "DEBTS_OR_ARREARS",
    ),
    // If the answer to RECEIVING_BENEFITS is YES, go to SELECT_BENEFITS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_WITH_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_ID_DOCUMENTS", answer = ListAnswer(listOf("Passport", "Biometric residence permit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_APPLY_FOR_ID", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RECEIVING_BENEFITS", answer = StringAnswer("YES")),
      ),
      "RECEIVING_BENEFITS",
      "SELECT_BENEFITS",
    ),
    // Any answer to SELECT_BENEFITS, go to DEBTS_OR_ARREARS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_WITH_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_ID_DOCUMENTS", answer = ListAnswer(listOf("Passport", "Biometric residence permit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_APPLY_FOR_ID", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RECEIVING_BENEFITS", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SELECT_BENEFITS", answer = ListAnswer(listOf("Personal independence payment (PIP)", "State pension"))),
      ),
      "SELECT_BENEFITS",
      "DEBTS_OR_ARREARS",
    ),
    // If the answer to DEBTS_OR_ARREARS is YES, go to HELP_MANAGE_DEBTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_WITH_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_ID_DOCUMENTS", answer = ListAnswer(listOf("Passport", "Biometric residence permit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_APPLY_FOR_ID", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RECEIVING_BENEFITS", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SELECT_BENEFITS", answer = ListAnswer(listOf("Universal credit", "Housing benefit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("DEBTS_OR_ARREARS", answer = StringAnswer("YES")),
      ),
      "DEBTS_OR_ARREARS",
      "HELP_MANAGE_DEBTS",
    ),
    // Any answer to HELP_MANAGE_DEBTS, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_WITH_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_ID_DOCUMENTS", answer = ListAnswer(listOf("Passport", "Biometric residence permit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_APPLY_FOR_ID", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RECEIVING_BENEFITS", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SELECT_BENEFITS", answer = ListAnswer(listOf("Universal credit", "Housing benefit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("DEBTS_OR_ARREARS", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_MANAGE_DEBTS", answer = StringAnswer("NO")),
      ),
      "HELP_MANAGE_DEBTS",
      "ASSESSMENT_SUMMARY",
    ),
    // If the answer to DEBTS_OR_ARREARS is NO, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_WITH_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_ID_DOCUMENTS", answer = ListAnswer(listOf("Passport", "Biometric residence permit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_APPLY_FOR_ID", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RECEIVING_BENEFITS", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SELECT_BENEFITS", answer = ListAnswer(listOf("Universal credit", "Housing benefit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("DEBTS_OR_ARREARS", answer = StringAnswer("NO")),
      ),
      "DEBTS_OR_ARREARS",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("HAS_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_WITH_BANK_ACCOUNT", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_ID_DOCUMENTS", answer = ListAnswer(listOf("Passport", "Biometric residence permit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_APPLY_FOR_ID", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RECEIVING_BENEFITS", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SELECT_BENEFITS", answer = ListAnswer(listOf("Universal credit", "Housing benefit"))),
        ResettlementAssessmentRequestQuestionAndAnswer("DEBTS_OR_ARREARS", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("CASE_NOTE_SUMMARY", answer = StringAnswer("My case note summary.")),
      ),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  @ParameterizedTest(name = "start: {0}")
  @MethodSource("test get page from Id - no existing assessment data")
  fun `test get page from Id - no existing assessment`(pageIdInput: String, expectedPage: ResettlementAssessmentResponsePage) {
    val nomsId = "123"
    setUpMocks("123", false)

    val page = resettlementAssessmentService.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.FINANCE_AND_ID,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "HAS_BANK_ACCOUNT",
      ResettlementAssessmentResponsePage(
        id = "HAS_BANK_ACCOUNT",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "HAS_BANK_ACCOUNT",
              title = "Does the person in prison have a bank account?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "HAS_BANK_ACCOUNT",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_WITH_BANK_ACCOUNT",
      ResettlementAssessmentResponsePage(
        id = "HELP_WITH_BANK_ACCOUNT",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "HELP_WITH_BANK_ACCOUNT",
              title = "Does the person in prison want help to apply for a bank account?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "HELP_WITH_BANK_ACCOUNT",
          ),
        ),
      ),
    ),
    Arguments.of(
      "WHAT_ID_DOCUMENTS",
      ResettlementAssessmentResponsePage(
        id = "WHAT_ID_DOCUMENTS",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "WHAT_ID_DOCUMENTS",
              title = "What ID documents does the person in prison have?",
              subTitle = null,
              type = TypeOfQuestion.CHECKBOX,
              options = mutableListOf(
                Option(id = "BIRTH_CERTIFICATE", displayText = "Birth or adoption certificate"),
                Option(id = "PASSPORT", displayText = "Passport"),
                Option(id = "DRIVING_LICENCE", displayText = "Driving licence"),
                Option(id = "MARRIAGE_CERTIFICATE", displayText = "Marriage or civil partnership certificate"),
                Option(id = "DIVORCE_CERTIFICATE", displayText = "Divorce decree absolute certificate"),
                Option(id = "BIOMETRIC_RESIDENCE_PERMIT", displayText = "Biometric residence permit"),
                Option(id = "DEED_POLL_CERTIFICATE", displayText = "Deed poll certificate"),
                Option(id = "NO_ID_DOCUMENTS", displayText = "No ID documents", exclusive = true),
                Option(id = "NO_ANSWER", displayText = "No answer provided", exclusive = true),
              ),
            ),
            originalPageId = "WHAT_ID_DOCUMENTS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_APPLY_FOR_ID",
      ResettlementAssessmentResponsePage(
        id = "HELP_APPLY_FOR_ID",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "HELP_APPLY_FOR_ID",
              title = "Does the person leaving prison want help to apply for ID?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "HELP_APPLY_FOR_ID",
          ),
        ),
      ),
    ),
    Arguments.of(
      "RECEIVING_BENEFITS",
      ResettlementAssessmentResponsePage(
        id = "RECEIVING_BENEFITS",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "RECEIVING_BENEFITS",
              title = "Was the person in prison receiving benefits before custody?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "RECEIVING_BENEFITS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "SELECT_BENEFITS",
      ResettlementAssessmentResponsePage(
        id = "SELECT_BENEFITS",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "SELECT_BENEFITS",
              title = "Select benefits the person in prison received before custody",
              subTitle = null,
              type = TypeOfQuestion.CHECKBOX,
              options = mutableListOf(
                Option(id = "ESA", displayText = "Employment and support allowance (ESA)"),
                Option(id = "HOUSING_BENEFIT", displayText = "Housing benefit"),
                Option(id = "UNIVERSAL_CREDIT_HOUSING_ELEMENT", displayText = "Universal credit housing element"),
                Option(id = "UNIVERSAL_CREDIT", displayText = "Universal credit"),
                Option(id = "PIP", displayText = "Personal independence payment (PIP)"),
                Option(id = "STATE_PENSION", displayText = "State pension"),
                Option(id = "NO_BENEFITS", displayText = "No benefits"),
                Option(id = "OTHER", displayText = "Other"),
                Option(id = "NO_ANSWER", displayText = "No answer provided", exclusive = true),
              ),
            ),
            originalPageId = "SELECT_BENEFITS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "DEBTS_OR_ARREARS",
      ResettlementAssessmentResponsePage(
        id = "DEBTS_OR_ARREARS",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "DEBTS_OR_ARREARS",
              title = "Does the person in prison have any debts or arrears?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "DEBTS_OR_ARREARS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_MANAGE_DEBTS",
      ResettlementAssessmentResponsePage(
        id = "HELP_MANAGE_DEBTS",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "HELP_MANAGE_DEBTS",
              title = "Does the person in prison want support to manage their debts or arrears?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "HELP_MANAGE_DEBTS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ASSESSMENT_SUMMARY",
      ResettlementAssessmentResponsePage(
        id = "ASSESSMENT_SUMMARY",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "SUPPORT_NEEDS",
              title = "",
              type = TypeOfQuestion.RADIO,
              options = mutableListOf(
                Option(
                  id = "SUPPORT_REQUIRED",
                  displayText = "Support required",
                  description = "a need for support has been identified and is accepted",
                ),
                Option(id = "SUPPORT_NOT_REQUIRED", displayText = "Support not required", description = "no need was identified"),
                Option(
                  id = "SUPPORT_DECLINED",
                  displayText = "Support declined",
                  description = "a need has been identified but support is declined",
                ),
              ),
            ),
            originalPageId = "ASSESSMENT_SUMMARY",
          ),
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "CASE_NOTE_SUMMARY",
              title = "Add a case note summary",
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
        questionsAndAnswers = mutableListOf(),
      ),
    ),
  )

  private fun setUpMocks(nomsId: String, returnResettlementAssessmentEntity: Boolean, assessment: ResettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentQuestionAndAnswerList(mutableListOf())) {
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val resettlementAssessmentEntity = if (returnResettlementAssessmentEntity) ResettlementAssessmentEntity(1, prisonerEntity, Pathway.FINANCE_AND_ID, Status.NOT_STARTED, ResettlementAssessmentType.BCST2, assessment, testDate, "", ResettlementAssessmentStatus.COMPLETE, "some text", "USER_1", submissionDate = null) else null
    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(
      resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        prisonerEntity,
        Pathway.FINANCE_AND_ID,
        ResettlementAssessmentType.BCST2,
        listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
      ),
    ).thenReturn(resettlementAssessmentEntity)
  }
}
