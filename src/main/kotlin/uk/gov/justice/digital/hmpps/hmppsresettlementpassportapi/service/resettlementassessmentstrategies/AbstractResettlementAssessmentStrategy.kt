package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.transaction.Transactional
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentSubmitRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.convertEnumStringToEnum
import kotlin.reflect.KClass

abstract class AbstractResettlementAssessmentStrategy<T, Q>(
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val statusRepository: StatusRepository,
  private val pathwayRepository: PathwayRepository,
  private val resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
  private val assessmentPageClass: KClass<T>,
  private val questionClass: KClass<Q>,
) :
  IResettlementAssessmentStrategy where T : Enum<*>, T : IAssessmentPage, Q : Enum<*>, Q : IResettlementAssessmentQuestion {

  override fun getNextPageId(
    assessment: ResettlementAssessmentRequest,
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    currentPage: String?,
  ): String {
    // Validate the request
    if (currentPage != null && assessment.questionsAndAnswers == null) {
      throw ServerWebInputException("If current page is defined, questions must also be defined.")
    }

    // Get the current page
    val currentPageEnum = if (currentPage != null) {
      convertEnumStringToEnum(
        enumClass = assessmentPageClass,
        secondaryEnumClass = GenericAssessmentPage::class,
        stringValue = currentPage,
      )
    } else {
      null
    }

    // Get the next page
    val nextPage: IAssessmentPage

    // Option 1 - If the current page is null then send back the first page unless there is already an assessment completed, in which case go straight to CHECK_ANSWERS
    if (currentPageEnum == null) {
      val existingAssessment = getExistingAssessment(nomsId, pathway, assessmentType)
      nextPage = if (existingAssessment == null) {
        assessmentPageClass.java.enumConstants[0] as IAssessmentPage
      } else {
        GenericAssessmentPage.CHECK_ANSWERS
      }
    }
    // Option 2 - If the current page is CHECK_ANSWERS then something has gone wrong as this is the end of the flow, and we should be called the submit endpoint instead
    else if (currentPageEnum.id == "CHECK_ANSWERS") {
      throw ServerWebInputException("Cannot get the next question from CHECK_ANSWERS as this is the end of the flow for this pathway.")
    }
    // Option 3 - If the current page is ASSESSMENT_SUMMARY then always go to CHECK_ANSWERS and fill out the questions and answers from the database
    else if (currentPageEnum.id == "ASSESSMENT_SUMMARY") {
      nextPage = GenericAssessmentPage.CHECK_ANSWERS
    }
    // Option 4 - If none of the above use the next page function in the question lambda to calculate the next page
    else {
      val questionLambda = getPageList().first { it.assessmentPage == currentPageEnum }
      val questions: List<ResettlementAssessmentQuestionAndAnswer> = assessment.questionsAndAnswers!!.map {
        ResettlementAssessmentQuestionAndAnswer(
          convertEnumStringToEnum(enumClass = questionClass, secondaryEnumClass = GenericResettlementAssessmentQuestion::class, stringValue = it.question),
          it.answer as Answer<*>,
        )
      }
      nextPage = questionLambda.nextPage(questions)
    }

    return nextPage.id
  }

  private fun getExistingAssessment(
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
  ): ResettlementAssessmentEntity? {
    // Obtain prisoner from database, if exists
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    // Obtain pathway entity from database
    val pathwayEntity = pathwayRepository.findById(pathway.id).get()

    // Obtain COMPLETE resettlement status entity from database
    val resettlementAssessmentStatusEntity =
      resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.COMPLETE.id).get()

    return resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusOrderByCreationDateDesc(
      prisonerEntity,
      pathwayEntity,
      assessmentType,
      resettlementAssessmentStatusEntity,
    )
  }

  override fun getPageFromId(
    nomsId: String,
    pathway: Pathway,
    pageId: String,
    assessmentType: ResettlementAssessmentType,
  ): ResettlementAssessmentResponsePage {
    // Get the latest complete assessment (if exists)
    var existingAssessment = getExistingAssessment(nomsId, pathway, assessmentType)

    // If this is a RESETTLEMENT_PLAN (BCST3) type and there is not existing assessment we should use an existing BCST2 if available.
    if (existingAssessment == null && assessmentType == ResettlementAssessmentType.RESETTLEMENT_PLAN) {
      existingAssessment = getExistingAssessment(nomsId, pathway, ResettlementAssessmentType.BCST2)
    }

    // Get the current page
    val page = convertEnumStringToEnum(enumClass = assessmentPageClass, secondaryEnumClass = GenericAssessmentPage::class, stringValue = pageId)

    // Convert to ResettlementAssessmentPage DTO
    val resettlementAssessmentResponsePage = ResettlementAssessmentResponsePage(
      id = page.id,
      title = page.title,
      questionsAndAnswers = page.questionsAndAnswers.map {
        ResettlementAssessmentResponseQuestionAndAnswer(
          ResettlementAssessmentResponseQuestion(
            it.question.id,
            it.question.title,
            it.question.subTitle,
            it.question.type,
            it.question.options?.toMutableList(),
          ),
          it.answer,
        )
      },
    )

    // If there is an existing assessment, add the answer into the question
    // Do not populate the case notes
    if (existingAssessment != null) {
      resettlementAssessmentResponsePage.questionsAndAnswers.forEach { q ->
        val existingAnswer = existingAssessment.assessment.assessment.find { it.questionId == q.question.id }
        if (existingAnswer != null && q.question != GenericResettlementAssessmentQuestion.CASE_NOTE_SUMMARY) {
          q.answer = existingAnswer.answer
        }
        // TODO - SUPPORT_NEEDS list of options may depend on the state of the assessment.
      }
    }

    return resettlementAssessmentResponsePage
  }

  abstract fun getPageList(): List<ResettlementAssessmentNode>

  @Transactional
  override fun submitAssessment(nomsId: String, assessment: ResettlementAssessmentSubmitRequest) {
    // TODO - re-write this to take in the complete set of questions and answers and store a new row in database.

    // Obtain prisoner from database, if exists
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    // Obtain pathway entity from database
    val pathwayEntity = pathwayRepository.findById(assessment.pathway.id).get()

    // Obtain resettlement assessment if exists
    val resettlementAssessmentEntity =
      resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(
        prisonerEntity,
        pathwayEntity,
        assessment.assessmentType,
      )
        ?: throw ResourceNotFoundException("Assessment of type ${assessment.assessmentType} for prisoner with nomsId $nomsId and pathway ${assessment.pathway} not found in database")

    // Obtain COMPLETE resettlement status entity from database
    val resettlementAssessmentStatusEntity =
      resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.COMPLETE.id).get()

    // Update assessment status, status changed to and case note text
    resettlementAssessmentEntity.assessmentStatus = resettlementAssessmentStatusEntity

    resettlementAssessmentRepository.save(resettlementAssessmentEntity)
  }
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class GenericAssessmentPage(
  override val id: String,
  override val title: String,
  override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>,
) : IAssessmentPage {
  CHECK_ANSWERS(
    id = "CHECK_ANSWERS",
    title = "",
    questionsAndAnswers = listOf(),
  ), // in this case questionsAndAnswers will be dynamically populated later by the UI cache
  ASSESSMENT_SUMMARY(
    id = "ASSESSMENT_SUMMARY",
    title = "",
    questionsAndAnswers = listOf(
      ResettlementAssessmentQuestionAndAnswer(GenericResettlementAssessmentQuestion.SUPPORT_NEEDS),
      ResettlementAssessmentQuestionAndAnswer(GenericResettlementAssessmentQuestion.CASE_NOTE_SUMMARY),
    ),
  ),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class GenericResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
) : IResettlementAssessmentQuestion {
  SUPPORT_NEEDS(
    id = "SUPPORT_NEEDS",
    title = "",
    type = TypeOfQuestion.RADIO,
    options = listOf(
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
  CASE_NOTE_SUMMARY(
    id = "CASE_NOTE_SUMMARY",
    title = "Add a case note summary",
    subTitle = "This will be displayed as a case note in both DPS and nDelius",
    type = TypeOfQuestion.LONG_TEXT,
  ),
}
