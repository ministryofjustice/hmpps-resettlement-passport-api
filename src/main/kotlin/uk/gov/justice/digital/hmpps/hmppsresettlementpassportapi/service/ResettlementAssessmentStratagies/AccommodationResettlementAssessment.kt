package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettlementAssessmentStratagies

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.AnswerDeserializer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswerSerialize
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettelmentAssessmentPages.AccommodationAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettelmentAssessmentPages.AccommodationResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettelmentAssessmentPages.accommodationPages
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDateTime

@Service
class AccommodationResettlementAssessment(
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val statusRepository: StatusRepository,
  private val pathwayRepository: PathwayRepository,
) : IResettlementAssessmentStrategy {
  override fun appliesTo(pathway: Pathway): Boolean {
    return pathway == Pathway.ACCOMMODATION
  }

  override fun storeAssessment(assessment: ResettlementAssessmentRequest, auth: String) {
    val prisonerEntity = prisonerRepository.findByNomsId(assessment.nomsID) ?: throw ServerWebInputException("Cannot get name from auth token") // TODO: figure out exception
    val statusEntity = statusRepository.findById(assessment.newStatus.id).get()
    val pathwayEntity = pathwayRepository.findById(assessment.pathway.id).get()
    val now = LocalDateTime.now()
    val gson: Gson = GsonBuilder().registerTypeAdapter(ResettlementAssessmentRequestQuestionAndAnswer::class.java, ResettlementAssessmentRequestQuestionAndAnswerSerialize()).setPrettyPrinting().create()

    val jsonAssessment = gson.toJson(assessment.questions)
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
    )
    resettlementAssessmentRepository.save(entity)
  }

  override fun nextQuestions(assessment: ResettlementAssessmentRequest): IAssessmentPage {
    val currentPageEnum = AccommodationAssessmentPage.valueOf(assessment.currentPage)
    val questionLambda = accommodationPages.first { it.assessmentPage == currentPageEnum }
    val questions: List<ResettlementAssessmentQuestionAndAnswer> = assessment.questions.map {
      ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.valueOf(it.question), it.answer as Answer<*>)
    }
    var nextPage =  questionLambda.nextPage(questions)

    if (nextPage == AccommodationAssessmentPage.CHECK_ANSWERS) {
      val prisonerEntity = prisonerRepository.findByNomsId(assessment.nomsID) ?: throw ServerWebInputException("Cannot get name from auth token") // TODO: figure out exception
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