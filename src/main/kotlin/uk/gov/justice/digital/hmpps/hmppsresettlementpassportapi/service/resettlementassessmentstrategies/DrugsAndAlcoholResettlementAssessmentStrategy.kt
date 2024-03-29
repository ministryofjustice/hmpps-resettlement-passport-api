package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository

@Service
class DrugsAndAlcoholResettlementAssessmentStrategy(
  resettlementAssessmentRepository: ResettlementAssessmentRepository,
  prisonerRepository: PrisonerRepository,
  statusRepository: StatusRepository,
  pathwayRepository: PathwayRepository,
  resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
) : AbstractResettlementAssessmentStrategy<DrugsAndAlcoholAssessmentPage, DrugsAndAlcoholResettlementAssessmentQuestion>(resettlementAssessmentRepository, prisonerRepository, statusRepository, pathwayRepository, resettlementAssessmentStatusRepository, DrugsAndAlcoholAssessmentPage::class, DrugsAndAlcoholResettlementAssessmentQuestion::class) {
  override fun appliesTo(pathway: Pathway) = pathway == Pathway.DRUGS_AND_ALCOHOL

  override fun getPageList(): List<ResettlementAssessmentNode> = listOf(
    ResettlementAssessmentNode(
      DrugsAndAlcoholAssessmentPage.DRUG_ISSUES,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == DrugsAndAlcoholResettlementAssessmentQuestion.DRUG_ISSUES && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          DrugsAndAlcoholAssessmentPage.SUPPORT_WITH_DRUG_ISSUES
        } else if (currentQuestionsAndAnswers.any { it.question == DrugsAndAlcoholResettlementAssessmentQuestion.DRUG_ISSUES && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          DrugsAndAlcoholAssessmentPage.ALCOHOL_ISSUES
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${DrugsAndAlcoholResettlementAssessmentQuestion.DRUG_ISSUES}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      DrugsAndAlcoholAssessmentPage.SUPPORT_WITH_DRUG_ISSUES,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return DrugsAndAlcoholAssessmentPage.ALCOHOL_ISSUES
      },
    ),
    ResettlementAssessmentNode(
      DrugsAndAlcoholAssessmentPage.ALCOHOL_ISSUES,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, edit: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == DrugsAndAlcoholResettlementAssessmentQuestion.ALCOHOL_ISSUES && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          DrugsAndAlcoholAssessmentPage.SUPPORT_WITH_ALCOHOL_ISSUES
        } else if (currentQuestionsAndAnswers.any { it.question == DrugsAndAlcoholResettlementAssessmentQuestion.ALCOHOL_ISSUES && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          finalQuestionNextPage(currentQuestionsAndAnswers, edit)
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${DrugsAndAlcoholResettlementAssessmentQuestion.ALCOHOL_ISSUES}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      DrugsAndAlcoholAssessmentPage.SUPPORT_WITH_ALCOHOL_ISSUES,
      nextPage = ::finalQuestionNextPage,
    ),
    assessmentSummaryNode,
  )
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class DrugsAndAlcoholAssessmentPage(override val id: String, override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, override val title: String? = null) : IAssessmentPage {
  DRUG_ISSUES(id = "DRUG_ISSUES", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(DrugsAndAlcoholResettlementAssessmentQuestion.DRUG_ISSUES))),
  SUPPORT_WITH_DRUG_ISSUES(id = "SUPPORT_WITH_DRUG_ISSUES", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(DrugsAndAlcoholResettlementAssessmentQuestion.SUPPORT_WITH_DRUG_ISSUES))),
  ALCOHOL_ISSUES(id = "ALCOHOL_ISSUES", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(DrugsAndAlcoholResettlementAssessmentQuestion.ALCOHOL_ISSUES))),
  SUPPORT_WITH_ALCOHOL_ISSUES(id = "SUPPORT_WITH_ALCOHOL_ISSUES", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(DrugsAndAlcoholResettlementAssessmentQuestion.SUPPORT_WITH_ALCOHOL_ISSUES))),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class DrugsAndAlcoholResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
  override val validationType: ValidationType = ValidationType.MANDATORY,
) : IResettlementAssessmentQuestion {
  DRUG_ISSUES(
    id = "DRUG_ISSUES",
    title = "Does the person in prison have any previous or current drug misuse issues?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  SUPPORT_WITH_DRUG_ISSUES(
    id = "SUPPORT_WITH_DRUG_ISSUES",
    title = "Does the person in prison want support with drug issues from the drug and alcohol team to help them prepare for release?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  ALCOHOL_ISSUES(
    id = "ALCOHOL_ISSUES",
    title = "Does the person in prison have any previous or current alcohol misuse issues?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  SUPPORT_WITH_ALCOHOL_ISSUES(
    id = "SUPPORT_WITH_ALCOHOL_ISSUES",
    title = "Does the person in prison want support with alcohol issues from the drug and alcohol team to help them prepare for release?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
}
