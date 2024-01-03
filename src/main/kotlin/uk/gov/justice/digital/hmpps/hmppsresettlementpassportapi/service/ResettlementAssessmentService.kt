package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AccommodationAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AccommodationResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AnswerDeserializer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequestQuestionAndAnswerSerialize
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettelmentAssessmentQuestions.accommodationQuestions
import java.time.LocalDateTime


//@Service
//class ResettlementAssessmentService (private val resettlementAssessmentStrategies: List<IResettlementAssessmentStrategy>) {
//
//  fun getNextQuestions(resettlementAssessment: ResettlementAssessment) {
//    val assessmentStrategy = resettlementAssessmentStrategies.first { it.appliesTo(resettlementAssessment.pathway) }
//    return
//    // Get pathway decision tree
//    // identify current location in decision tree
//    // find next questions in tree
//  }
//}

interface IResettlementAssessmentStrategy {
  fun appliesTo(pathway: Pathway): Boolean
  fun storeAssessment(assessment: ResettlementAssessmentRequest, auth: String)
  fun nextQuestions(assessment: ResettlementAssessmentRequest): AssessmentPage
}

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

  override fun nextQuestions(assessment: ResettlementAssessmentRequest): AssessmentPage {
    val currentPageEnum = AccommodationAssessmentPage.valueOf(assessment.currentPage)
    val questionLambda = accommodationQuestions.first { it.assessmentPage == currentPageEnum }
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

//class AttitudesResettlementAssessment : ResettlementAssessmentInterface {
//  override fun appliesTo(pathway: Pathway): Boolean {
//    return pathway == Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
//  }
//
//  override fun nextQuestions(currentPage: AssessmentPage, questions: List<ResettlementAssessmentQuestionAndAnswer<*>>): AssessmentPage {
//    val questionLambda = AccommodationQuestions.accommodationQuestions.first { it.assessmentPage == currentPage }
//    val nextPage = questionLambda.nextPage(currentPage, questions)
//    return nextPage
//  }
//}
