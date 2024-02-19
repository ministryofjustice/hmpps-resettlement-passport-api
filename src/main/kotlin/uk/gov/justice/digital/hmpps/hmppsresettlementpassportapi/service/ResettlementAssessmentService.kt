package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LatestResettlementAssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LatestResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatusAndCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.GenericResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.IResettlementAssessmentStrategy
import java.time.LocalDateTime

@Service
class ResettlementAssessmentService(
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
  private val prisonerRepository: PrisonerRepository,
  private val pathwayRepository: PathwayRepository,
  private val deliusContactService: DeliusContactService,
  private val caseNotesService: CaseNotesService,
  private val pathwayAndStatusService: PathwayAndStatusService,
) {
  @Transactional
  fun getResettlementAssessmentSummaryByNomsId(nomsId: String): List<PrisonerResettlementAssessment> {
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val pathways = Pathway.entries.toTypedArray()
    return pathways.map {
      val pathwayEntity = pathwayRepository.findById(it.id).get()
      val resettlementAssessmentForPathway =
        resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(
          prisonerEntity,
          pathwayEntity,
          ResettlementAssessmentType.BCST2,
        )
      if (resettlementAssessmentForPathway == null) {
        PrisonerResettlementAssessment(it, ResettlementAssessmentStatus.NOT_STARTED)
      } else {
        PrisonerResettlementAssessment(
          it,
          ResettlementAssessmentStatus.getById(resettlementAssessmentForPathway.assessmentStatus.id),
        )
      }
    }
  }

  @Transactional
  fun submitResettlementAssessmentByNomsId(nomsId: String, assessmentType: ResettlementAssessmentType, auth: String) {
    // Check auth - must be NOMIS
    val authSource = getClaimFromJWTToken(auth, "auth_source")?.lowercase()
    if (authSource != "nomis") {
      throw ServerWebInputException("Endpoint must be called with a user token with authSource of NOMIS")
    }

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val resettlementAssessmentStatusCompleteEntity = resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.COMPLETE.id).get()
    val resettlementAssessmentStatusSubmittedEntity = resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.SUBMITTED.id).get()

    val assessmentList = mutableListOf<ResettlementAssessmentEntity>()

    // For each pathway, get the latest complete assessment
    Pathway.entries.forEach { pathway ->
      val pathwayEntity = pathwayRepository.findById(pathway.id).get()
      val resettlementAssessment = resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        prisoner = prisonerEntity,
        pathway = pathwayEntity,
        assessmentType = assessmentType,
        assessmentStatus = listOf(resettlementAssessmentStatusCompleteEntity),
      )
      if (resettlementAssessment != null) {
        assessmentList.add(resettlementAssessment)
      }
    }

    if (assessmentList.size != Pathway.entries.size) {
      throw RuntimeException("Found [${assessmentList.size}] assessments for prisoner [$nomsId]. This should be all ${Pathway.entries.size} pathways!")
    }

    assessmentList.forEach { assessment ->

      // Add case note to delius_contact table
      deliusContactService.addDeliusCaseNoteToDatabase(
        nomsId = nomsId,
        pathwayStatusAndCaseNote = PathwayStatusAndCaseNote(
          pathway = Pathway.getById(assessment.pathway.id),
          status = Status.getById(assessment.statusChangedTo.id),
          caseNoteText = assessment.caseNoteText,
        ),
        username = assessment.createdBy,
      )

      // Publish case note to Delius SNS topic
      // TODO - placeholder for when this is available. For now it will just be in our database.

      // Add case note to DPS (database and SQS)
      caseNotesService.addCaseNoteToDps(
        prisonerEntity = prisonerEntity,
        pathwayEntity = assessment.pathway,
        createdDate = LocalDateTime.now(),
        notes = assessment.caseNoteText,
        name = assessment.createdBy,
        userId = assessment.createdByUserId,
      )

      // Update pathway status
      pathwayAndStatusService.updatePathwayStatus(
        nomsId = nomsId,
        pathwayAndStatus = PathwayAndStatus(Pathway.getById(assessment.pathway.id), Status.getById(assessment.statusChangedTo.id)),
      )

      // Update assessment status to SUBMITTED
      assessment.assessmentStatus = resettlementAssessmentStatusSubmittedEntity
      resettlementAssessmentRepository.save(assessment)
    }
  }

  fun getLatestResettlementAssessmentByNomsIdAndPathway(nomsId: String, pathway: Pathway, resettlementAssessmentStrategies: List<IResettlementAssessmentStrategy<*>>): LatestResettlementAssessmentResponse {
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    val pathwayEntity = pathwayRepository.findById(pathway.id).get()
    val resettlementStatusEntity = resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.SUBMITTED.id).get()

    val resettlementAssessment = resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentStatusOrderByCreationDateDesc(prisonerEntity, pathwayEntity, resettlementStatusEntity)
      ?: throw ResourceNotFoundException("No submitted resettlement assessment found for prisoner $nomsId / pathway $pathway")

    val questionClass = resettlementAssessmentStrategies.first { it.appliesTo(Pathway.getById(resettlementAssessment.pathway.id)) }.getQuestionClass()
    val questionsAndAnswers = resettlementAssessment.assessment.assessment.mapNotNull {
      val question = convertEnumStringToEnum(questionClass, GenericResettlementAssessmentQuestion::class, it.questionId) as IResettlementAssessmentQuestion
      if (question !in listOf(GenericResettlementAssessmentQuestion.SUPPORT_NEEDS, GenericResettlementAssessmentQuestion.CASE_NOTE_SUMMARY)) {
        LatestResettlementAssessmentResponseQuestionAndAnswer(question.title, convertAnswerToString(question.type, question.options, it.answer))
      } else {
        null
      }
    }

    return LatestResettlementAssessmentResponse(
      lastUpdated = resettlementAssessment.creationDate,
      questionsAndAnswers = questionsAndAnswers,
    )
  }

  fun convertAnswerToString(type: TypeOfQuestion, options: List<Option>?, answer: Answer<*>): String? {
    val answerAsString = if (answer is StringAnswer) {
      answer.answer as String
    } else if (answer is ListAnswer) {
      val listAnswer = answer.answer
      if (listAnswer != null) {
        convertFromListToStringWithLineBreaks(listAnswer)
      } else {
        null
      }
    } else if (answer is MapAnswer) {
      val listOfMapsAnswer = answer.answer
      if (listOfMapsAnswer != null) {
        convertFromListOfMapsToStringWithLineBreaks(listOfMapsAnswer)
      } else {
        null
      }
    } else {
      throw RuntimeException("Unknown answer type ${answer::class.qualifiedName}")
    }

    return if (type in listOf(TypeOfQuestion.RADIO_WITH_ADDRESS, TypeOfQuestion.RADIO)) {
      options?.find { it.id == answerAsString }?.displayText ?: answerAsString
    } else {
      answerAsString
    }
  }

  fun convertFromListToStringWithLineBreaks(stringElements: List<String>): String {
    var string = ""
    stringElements.forEach {
      if (it.isNotBlank()) {
        string += "${it.trim()}\n"
      }
    }
    return string.removeSuffix("\n")
  }

  private fun convertFromListOfMapsToStringWithLineBreaks(listOfMaps: List<Map<String, String>>): String {
    var string = ""
    listOfMaps.flatMap { it.values }.forEach {
      if (it.isNotBlank()) {
        string += "${it.trim()}\n"
      }
    }
    return string.removeSuffix("\n")
  }
}
