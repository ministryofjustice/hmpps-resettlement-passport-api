package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.NextPageContext
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository

@Service
class FinanceAndIdResettlementAssessmentStrategy(
  resettlementAssessmentRepository: ResettlementAssessmentRepository,
  prisonerRepository: PrisonerRepository,
  statusRepository: StatusRepository,
  pathwayRepository: PathwayRepository,
  pathwayStatusRepository: PathwayStatusRepository,
  resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
) : AbstractResettlementAssessmentStrategy<FinanceAndIdAssessmentPage, FinanceAndIdResettlementAssessmentQuestion>(
  resettlementAssessmentRepository,
  prisonerRepository,
  statusRepository,
  pathwayRepository,
  pathwayStatusRepository,
  resettlementAssessmentStatusRepository,
  FinanceAndIdAssessmentPage::class,
  FinanceAndIdResettlementAssessmentQuestion::class,
) {
  override fun appliesTo(pathway: Pathway) = pathway == Pathway.FINANCE_AND_ID

  override fun getPageList(assessmentType: ResettlementAssessmentType): List<ResettlementAssessmentNode> = listOf(
    ResettlementAssessmentNode(
      FinanceAndIdAssessmentPage.HAS_BANK_ACCOUNT,
      nextPage =
      fun(context: NextPageContext): IAssessmentPage {
        val (currentQuestionsAndAnswers) = context
        return if (currentQuestionsAndAnswers.any { it.question == FinanceAndIdResettlementAssessmentQuestion.HAS_BANK_ACCOUNT && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          FinanceAndIdAssessmentPage.WHAT_ID_DOCUMENTS
        } else if (currentQuestionsAndAnswers.any { it.question == FinanceAndIdResettlementAssessmentQuestion.HAS_BANK_ACCOUNT && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          FinanceAndIdAssessmentPage.HELP_WITH_BANK_ACCOUNT
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${FinanceAndIdResettlementAssessmentQuestion.HAS_BANK_ACCOUNT}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      FinanceAndIdAssessmentPage.HELP_WITH_BANK_ACCOUNT,
      nextPage =
      fun(_: NextPageContext): IAssessmentPage {
        return FinanceAndIdAssessmentPage.WHAT_ID_DOCUMENTS
      },
    ),
    ResettlementAssessmentNode(
      FinanceAndIdAssessmentPage.WHAT_ID_DOCUMENTS,
      nextPage =
      fun(_: NextPageContext): IAssessmentPage {
        return FinanceAndIdAssessmentPage.HELP_APPLY_FOR_ID
      },
    ),
    ResettlementAssessmentNode(
      FinanceAndIdAssessmentPage.HELP_APPLY_FOR_ID,
      nextPage =
      fun(_: NextPageContext): IAssessmentPage {
        return FinanceAndIdAssessmentPage.RECEIVING_BENEFITS
      },
    ),
    ResettlementAssessmentNode(
      FinanceAndIdAssessmentPage.RECEIVING_BENEFITS,
      nextPage =
      fun(context: NextPageContext): IAssessmentPage {
        val (currentQuestionsAndAnswers) = context
        return if (currentQuestionsAndAnswers.any { it.question == FinanceAndIdResettlementAssessmentQuestion.RECEIVING_BENEFITS && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          FinanceAndIdAssessmentPage.SELECT_BENEFITS
        } else if (currentQuestionsAndAnswers.any { it.question == FinanceAndIdResettlementAssessmentQuestion.RECEIVING_BENEFITS && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          FinanceAndIdAssessmentPage.DEBTS_OR_ARREARS
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${FinanceAndIdResettlementAssessmentQuestion.RECEIVING_BENEFITS}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      FinanceAndIdAssessmentPage.SELECT_BENEFITS,
      nextPage =
      fun(_: NextPageContext): IAssessmentPage {
        return FinanceAndIdAssessmentPage.DEBTS_OR_ARREARS
      },
    ),
    ResettlementAssessmentNode(
      FinanceAndIdAssessmentPage.DEBTS_OR_ARREARS,
      nextPage =
      fun(context: NextPageContext): IAssessmentPage {
        val (currentQuestionsAndAnswers) = context
        return if (currentQuestionsAndAnswers.any { it.question == FinanceAndIdResettlementAssessmentQuestion.DEBTS_OR_ARREARS && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          FinanceAndIdAssessmentPage.HELP_MANAGE_DEBTS
        } else if (currentQuestionsAndAnswers.any { it.question == FinanceAndIdResettlementAssessmentQuestion.DEBTS_OR_ARREARS && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          finalQuestionNextPage(context)
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${FinanceAndIdResettlementAssessmentQuestion.DEBTS_OR_ARREARS}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      FinanceAndIdAssessmentPage.HELP_MANAGE_DEBTS,
      nextPage = ::finalQuestionNextPage,
    ),
    assessmentSummaryNode(assessmentType),
  )
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class FinanceAndIdAssessmentPage(override val id: String, override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, override val title: String? = null) : IAssessmentPage {
  HAS_BANK_ACCOUNT(id = "HAS_BANK_ACCOUNT", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(FinanceAndIdResettlementAssessmentQuestion.HAS_BANK_ACCOUNT))),
  HELP_WITH_BANK_ACCOUNT(id = "HELP_WITH_BANK_ACCOUNT", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(FinanceAndIdResettlementAssessmentQuestion.HELP_WITH_BANK_ACCOUNT))),
  WHAT_ID_DOCUMENTS(id = "WHAT_ID_DOCUMENTS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(FinanceAndIdResettlementAssessmentQuestion.WHAT_ID_DOCUMENTS))),
  HELP_APPLY_FOR_ID(id = "HELP_APPLY_FOR_ID", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(FinanceAndIdResettlementAssessmentQuestion.HELP_APPLY_FOR_ID))),
  RECEIVING_BENEFITS(id = "RECEIVING_BENEFITS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(FinanceAndIdResettlementAssessmentQuestion.RECEIVING_BENEFITS))),
  SELECT_BENEFITS(id = "SELECT_BENEFITS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(FinanceAndIdResettlementAssessmentQuestion.SELECT_BENEFITS))),
  DEBTS_OR_ARREARS(id = "DEBTS_OR_ARREARS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(FinanceAndIdResettlementAssessmentQuestion.DEBTS_OR_ARREARS))),
  HELP_MANAGE_DEBTS(id = "HELP_MANAGE_DEBTS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(FinanceAndIdResettlementAssessmentQuestion.HELP_MANAGE_DEBTS))),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class FinanceAndIdResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
  override val validationType: ValidationType = ValidationType.MANDATORY,
) : IResettlementAssessmentQuestion {
  HAS_BANK_ACCOUNT(
    id = "HAS_BANK_ACCOUNT",
    title = "Does the person in prison have a bank account?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  HELP_WITH_BANK_ACCOUNT(
    id = "HELP_WITH_BANK_ACCOUNT",
    title = "Does the person in prison want help to apply for a bank account?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  WHAT_ID_DOCUMENTS(
    id = "WHAT_ID_DOCUMENTS",
    title = "What ID documents does the person in prison have?",
    type = TypeOfQuestion.CHECKBOX,
    options = listOf(
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
  HELP_APPLY_FOR_ID(
    id = "HELP_APPLY_FOR_ID",
    title = "Does the person leaving prison want help to apply for ID?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  RECEIVING_BENEFITS(
    id = "RECEIVING_BENEFITS",
    title = "Was the person in prison receiving benefits before custody?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  SELECT_BENEFITS(
    id = "SELECT_BENEFITS",
    title = "Select benefits the person in prison received before custody",
    type = TypeOfQuestion.CHECKBOX,
    options = listOf(
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
  DEBTS_OR_ARREARS(
    id = "DEBTS_OR_ARREARS",
    title = "Does the person in prison have any debts or arrears?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  HELP_MANAGE_DEBTS(
    id = "HELP_MANAGE_DEBTS",
    title = "Does the person in prison want support to manage their debts or arrears?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
}
