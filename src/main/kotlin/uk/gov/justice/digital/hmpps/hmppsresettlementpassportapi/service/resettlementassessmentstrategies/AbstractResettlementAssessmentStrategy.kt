package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import jakarta.transaction.Transactional
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentSubmitRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.AnswerDeserializer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswerSerialize
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.convertEnumStringToEnum
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDateTime
import kotlin.reflect.KClass

abstract class AbstractResettlementAssessmentStrategy<T, Q>(
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val statusRepository: StatusRepository,
  private val pathwayRepository: PathwayRepository,
  private val resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
  private val assessmentPageClass: KClass<T>,
  private val questionClass: KClass<Q>,
) : IResettlementAssessmentStrategy where T : Enum<*>, T : IAssessmentPage, Q : Enum<*>, Q : IResettlementAssessmentQuestion {

  override fun nextQuestions(assessment: ResettlementAssessmentRequest): IAssessmentPage {
    val currentPageEnum = convertEnumStringToEnum(assessmentPageClass, assessment.currentPage)
    val questionLambda = getPageList().first { it.assessmentPage == currentPageEnum }
    val questions: List<ResettlementAssessmentQuestionAndAnswer> = assessment.questions.map {
      ResettlementAssessmentQuestionAndAnswer(convertEnumStringToEnum(questionClass, it.question), it.answer as Answer<*>)
    }
    val nextPage = questionLambda.nextPage(questions)

    if (nextPage == convertEnumStringToEnum(assessmentPageClass, "CHECK_ANSWERS")) {
      val prisonerEntity = prisonerRepository.findByNomsId(assessment.nomsID)
        ?: throw ResourceNotFoundException("Prisoner with id ${assessment.nomsID} not found in database")
      val pathwayEntity = pathwayRepository.findById(assessment.pathway.id).get()
      val latestAssessment = resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, pathwayEntity, assessment.type)
        ?: throw RuntimeException("Unexpectedly cannot find assessment of type ${assessment.type} for prisoner ${assessment.nomsID} for pathway ${assessment.pathway}")
      val gson = GsonBuilder().registerTypeAdapter(Answer::class.java, AnswerDeserializer()).create()
      val typeToken = object : TypeToken<List<ResettlementAssessmentRequestQuestionAndAnswer<*>>>() {}.type
      val answers = gson.fromJson<List<ResettlementAssessmentRequestQuestionAndAnswer<*>>>(latestAssessment.assessment, typeToken)
      val questionsAndAnswers = answers.map {
        ResettlementAssessmentQuestionAndAnswer(
          convertEnumStringToEnum(questionClass, it.question),
          it.answer,
        )
      }
      questionsAndAnswers.forEach { nextPage.questionsAndAnswers.add(it) }
    }

    return nextPage
  }

  abstract fun getPageList(): List<ResettlementAssessmentNode>

  @Transactional
  override fun storeAssessment(assessment: ResettlementAssessmentRequest, auth: String) {
    val prisonerEntity = prisonerRepository.findByNomsId(assessment.nomsID)
      ?: throw ResourceNotFoundException("Prisoner with id ${assessment.nomsID} not found in database")
    val statusEntity = statusRepository.findById(assessment.newStatus.id).get()
    val pathwayEntity = pathwayRepository.findById(assessment.pathway.id).get()
    val now = LocalDateTime.now()
    val gson: Gson = GsonBuilder()
      .registerTypeAdapter(ResettlementAssessmentRequestQuestionAndAnswer::class.java, ResettlementAssessmentRequestQuestionAndAnswerSerialize())
      .setPrettyPrinting()
      .create()

    val jsonAssessment = gson.toJson(assessment.questions)
    val assessmentStatus = resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.NOT_STARTED.id).get()
    val entity = ResettlementAssessmentEntity(
      id = null,
      pathway = pathwayEntity,
      prisoner = prisonerEntity,
      assessmentType = assessment.type,
      statusChangedTo = statusEntity,
      createdBy = getClaimFromJWTToken(auth, "name")
        ?: throw ServerWebInputException("Cannot get name from auth token"),
      creationDate = now,
      assessment = jsonAssessment,
      assessmentStatus = assessmentStatus,
    )
    resettlementAssessmentRepository.save(entity)
  }

  @Transactional
  override fun submitAssessment(nomsId: String, assessment: ResettlementAssessmentSubmitRequest) {
    // Obtain prisoner from database, if exists
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    // Obtain pathway entity from database
    val pathwayEntity = pathwayRepository.findById(assessment.pathway.id).get()

    // Obtain resettlement assessment if exists
    val resettlementAssessmentEntity = resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, pathwayEntity, assessment.assessmentType)
      ?: throw ResourceNotFoundException("Assessment of type ${assessment.assessmentType} for prisoner with nomsId $nomsId and pathway ${assessment.pathway} not found in database")

    // Obtain COMPLETE resettlement status entity from database
    val resettlementAssessmentStatusEntity = resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.COMPLETE.id).get()

    // Update assessment status, status changed to and case note text
    resettlementAssessmentEntity.assessmentStatus = resettlementAssessmentStatusEntity

    resettlementAssessmentRepository.save(resettlementAssessmentEntity)
  }
}
