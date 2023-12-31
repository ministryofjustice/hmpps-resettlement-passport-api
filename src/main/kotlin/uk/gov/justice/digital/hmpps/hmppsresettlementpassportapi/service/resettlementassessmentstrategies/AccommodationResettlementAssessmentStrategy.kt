package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.AnswerDeserializer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswerSerialize
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentpages.AccommodationAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentpages.AccommodationResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentpages.accommodationPages
import java.time.LocalDateTime

@Service
class AccommodationResettlementAssessmentStrategy(
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val statusRepository: StatusRepository,
  private val pathwayRepository: PathwayRepository,
  private val resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
) : IResettlementAssessmentStrategy {
  override fun appliesTo(pathway: Pathway): Boolean {
    return pathway == Pathway.ACCOMMODATION
  }

  override fun storeAssessment(assessment: ResettlementAssessmentRequest, auth: String) {
    val prisonerEntity = prisonerRepository.findByNomsId(assessment.nomsID)
      ?: throw ResourceNotFoundException("Prisoner with id ${assessment.nomsID} not found in database") // TODO: figure out exception
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

  override fun nextQuestions(assessment: ResettlementAssessmentRequest): IAssessmentPage {
    val currentPageEnum = AccommodationAssessmentPage.valueOf(assessment.currentPage)
    val questionLambda = accommodationPages.first { it.assessmentPage == currentPageEnum }
    val questions: List<ResettlementAssessmentQuestionAndAnswer> = assessment.questions.map {
      ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.valueOf(it.question), it.answer as Answer<*>)
    }
    var nextPage = questionLambda.nextPage(questions)

    if (nextPage == AccommodationAssessmentPage.CHECK_ANSWERS) {
      val prisonerEntity = prisonerRepository.findByNomsId(assessment.nomsID)
        ?: throw ResourceNotFoundException("Prisoner with id ${assessment.nomsID} not found in database")
      val pathwayEntity = pathwayRepository.findById(assessment.pathway.id).get()
      val latestAssessment = resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, pathwayEntity, assessment.type)
      val gson = GsonBuilder().registerTypeAdapter(Answer::class.java, AnswerDeserializer()).create()
      val typeToken = object : TypeToken<List<ResettlementAssessmentRequestQuestionAndAnswer<*>>>() {}.type
      val answers = gson.fromJson<List<ResettlementAssessmentRequestQuestionAndAnswer<*>>>(latestAssessment.assessment, typeToken)
      val questionsAndAnswers = answers.map { ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.valueOf(it.question), it.answer) }
      questionsAndAnswers.forEach { nextPage.questionsAndAnswers.add(it) }
    }

    return nextPage
  }
}
