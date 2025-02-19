package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.params.provider.Arguments
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import java.util.stream.Stream

class FinanceAndIdV1ResettlementAssessmentAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.FINANCE_AND_ID, 1) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
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

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      "HAS_BANK_ACCOUNT",
      ResettlementAssessmentResponsePage(
        id = "HAS_BANK_ACCOUNT",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HAS_BANK_ACCOUNT",
              title = "Does the person in prison have a bank account?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HELP_WITH_BANK_ACCOUNT",
              title = "Does the person in prison want help to apply for a bank account?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WHAT_ID_DOCUMENTS",
              title = "What ID documents does the person in prison have?",
              subTitle = "Select all that apply.",
              type = TypeOfQuestion.CHECKBOX,
              options = listOf(
                ResettlementAssessmentOption(id = "BIRTH_CERTIFICATE", displayText = "Birth or adoption certificate"),
                ResettlementAssessmentOption(id = "PASSPORT", displayText = "Passport"),
                ResettlementAssessmentOption(id = "DRIVING_LICENCE", displayText = "Driving licence"),
                ResettlementAssessmentOption(id = "MARRIAGE_CERTIFICATE", displayText = "Marriage or civil partnership certificate"),
                ResettlementAssessmentOption(id = "DIVORCE_CERTIFICATE", displayText = "Divorce decree absolute certificate"),
                ResettlementAssessmentOption(id = "BIOMETRIC_RESIDENCE_PERMIT", displayText = "Biometric residence permit"),
                ResettlementAssessmentOption(id = "DEED_POLL_CERTIFICATE", displayText = "Deed poll certificate"),
                ResettlementAssessmentOption(id = "NO_ID_DOCUMENTS", displayText = "No ID documents", exclusive = true),
                ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided", exclusive = true),
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HELP_APPLY_FOR_ID",
              title = "Does the person leaving prison want help to apply for ID?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "RECEIVING_BENEFITS",
              title = "Was the person in prison receiving benefits before custody?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SELECT_BENEFITS",
              title = "Select benefits the person in prison received before custody",
              subTitle = "Select all that apply.",
              type = TypeOfQuestion.CHECKBOX,
              options = listOf(
                ResettlementAssessmentOption(id = "ESA", displayText = "Employment and support allowance (ESA)"),
                ResettlementAssessmentOption(id = "HOUSING_BENEFIT", displayText = "Housing benefit"),
                ResettlementAssessmentOption(id = "UNIVERSAL_CREDIT_HOUSING_ELEMENT", displayText = "Universal credit housing element"),
                ResettlementAssessmentOption(id = "UNIVERSAL_CREDIT", displayText = "Universal credit"),
                ResettlementAssessmentOption(id = "PIP", displayText = "Personal independence payment (PIP)"),
                ResettlementAssessmentOption(id = "STATE_PENSION", displayText = "State pension"),
                ResettlementAssessmentOption(id = "NO_BENEFITS", displayText = "No benefits"),
                ResettlementAssessmentOption(id = "OTHER", displayText = "Other"),
                ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided", exclusive = true),
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DEBTS_OR_ARREARS",
              title = "Does the person in prison have any debts or arrears?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HELP_MANAGE_DEBTS",
              title = "Does the person in prison want support to manage their debts or arrears?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
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
        title = "Finance and ID report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Finance and ID support needs",
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
