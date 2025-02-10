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

class FinanceAndIdV3ResettlementAssessmentAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.FINANCE_AND_ID, 3) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "FINANCE_AND_ID_REPORT",
    ),
    // Any answer to FINANCE_AND_ID_REPORT, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "FINANCE_AND_ID_REPORT",
      "CHECK_ANSWERS",
    ),
  )

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
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
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has a bank account"),
              options = yesNoOptions,
            ),
            originalPageId = "FINANCE_AND_ID_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HAS_BANK_ACCOUNT_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "FINANCE_AND_ID_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WHAT_ID_DOCUMENTS",
              title = "What ID documents does the person in prison have?",
              subTitle = "Select all that apply",
              type = TypeOfQuestion.CHECKBOX,
              validation = Validation(ValidationType.MANDATORY, message = "Select ID documents, or select 'No ID documents' or 'No answer provided'"),
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
              id = "WHAT_ID_DOCUMENTS_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "FINANCE_AND_ID_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SELECT_BENEFITS",
              title = "What benefits was the person in prison receiving before custody?",
              subTitle = "Select all that apply",
              type = TypeOfQuestion.CHECKBOX,
              validation = Validation(ValidationType.MANDATORY, message = "Select benefits, or select 'No benefits' or 'No answer provided'"),
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
              id = "SELECT_BENEFITS_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "FINANCE_AND_ID_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DEBTS_OR_ARREARS",
              title = "Does the person in prison have any debts or arrears?",
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has any debts or arrears"),
              options = yesNoOptions,
            ),
            originalPageId = "FINANCE_AND_ID_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DEBTS_OR_ARREARS_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "Include details of what type of debt it is, and the amount. Do not include the names of anyone the person owes money to.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "FINANCE_AND_ID_REPORT",
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
