package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LatestResettlementAssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LatestResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
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
  fun getResettlementAssessmentSummaryByNomsId(nomsId: String, assessmentType: ResettlementAssessmentType): List<PrisonerResettlementAssessment> {
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val pathways = Pathway.entries.toTypedArray()
    return pathways.map {
      val pathwayEntity = pathwayRepository.findById(it.id).get()
      val resettlementAssessmentForPathway =
        resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(
          prisonerEntity,
          pathwayEntity,
          assessmentType,
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
      if (assessment.statusChangedTo == null) {
        throw RuntimeException("Can't submit assessment with id ${assessment.id} as statusChangedTo is null")
      }

      if (assessment.caseNoteText == null) {
        throw RuntimeException("Can't submit assessment with id ${assessment.id} as caseNoteText is null")
      }

      // Add templated first line to case note before posting
      val caseNotesText = "${getFirstLineOfBcstCaseNote(Pathway.getById(assessment.pathway.id), assessment.assessmentType)}\n\n${assessment.caseNoteText}"

      // Post case note to DPS
      caseNotesService.postBCSTCaseNoteToDps(
        nomsId = prisonerEntity.nomsId,
        notes = caseNotesText,
        userId = assessment.createdByUserId,
      )

      // Update pathway status
      pathwayAndStatusService.updatePathwayStatus(
        nomsId = nomsId,
        pathwayAndStatus = PathwayAndStatus(Pathway.getById(assessment.pathway.id), Status.getById(assessment.statusChangedTo!!.id)),
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

    val latestResettlementAssessment = convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(
      resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentStatusOrderByCreationDateDesc(prisonerEntity, pathwayEntity, resettlementStatusEntity)
        ?: throw ResourceNotFoundException("No submitted resettlement assessment found for prisoner $nomsId / pathway $pathway"),
      resettlementAssessmentStrategies,
    )

    val originalResettlementAssessment = convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(
      resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentStatusOrderByCreationDateAsc(prisonerEntity, pathwayEntity, resettlementStatusEntity)
        ?: throw ResourceNotFoundException("No submitted resettlement assessment found for prisoner $nomsId / pathway $pathway"),
      resettlementAssessmentStrategies,
    )

    // If the latest and original assessments from the same, then only return the latest otherwise return both
    return if (latestResettlementAssessment == originalResettlementAssessment) {
      LatestResettlementAssessmentResponse(
        latestAssessment = latestResettlementAssessment,
      )
    } else {
      LatestResettlementAssessmentResponse(
        originalAssessment = originalResettlementAssessment,
        latestAssessment = latestResettlementAssessment,
      )
    }
  }

  fun convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(resettlementAssessmentEntity: ResettlementAssessmentEntity, resettlementAssessmentStrategies: List<IResettlementAssessmentStrategy<*>>): ResettlementAssessmentResponse {
    val resettlementStrategy = resettlementAssessmentStrategies.first { it.appliesTo(Pathway.getById(resettlementAssessmentEntity.pathway.id)) }

    val questionClass = resettlementStrategy.getQuestionClass()
    val questionsAndAnswers = resettlementAssessmentEntity.assessment.assessment.mapNotNull {
      val question = convertEnumStringToEnum(questionClass, GenericResettlementAssessmentQuestion::class, it.questionId) as IResettlementAssessmentQuestion
      if (question !in listOf(GenericResettlementAssessmentQuestion.SUPPORT_NEEDS, GenericResettlementAssessmentQuestion.CASE_NOTE_SUMMARY)) {
        LatestResettlementAssessmentResponseQuestionAndAnswer(
          questionTitle = question.title,
          answer = convertAnswerToString(question.options, it.answer),
          originalPageId = resettlementStrategy.findPageIdFromQuestionId(it.questionId),
        )
      } else {
        null
      }
    }

    return ResettlementAssessmentResponse(
      assessmentType = resettlementAssessmentEntity.assessmentType,
      lastUpdated = resettlementAssessmentEntity.creationDate,
      updatedBy = resettlementAssessmentEntity.createdBy,
      questionsAndAnswers = questionsAndAnswers,
    )
  }

  fun convertAnswerToString(options: List<Option>?, answer: Answer<*>): String? {
    val answerComponents: List<String>? = when (answer) {
      is StringAnswer -> listOf(answer.answer as String)
      is ListAnswer -> {
        answer.answer?.filter { it.isNotBlank() }?.map { it.trim() }
      }
      is MapAnswer -> {
        if (answer.answer != null) {
          answer.answer!!.flatMap { it.values }.filter { it.isNotBlank() }.map { it.trim() }
        } else {
          null
        }
      }
      else -> {
        throw RuntimeException("Unknown answer type ${answer::class.qualifiedName}")
      }
    }

    return if (answerComponents != null) convertFromListToStringWithLineBreaks(answerComponents, options) else null
  }

  fun convertFromListToStringWithLineBreaks(stringElements: List<String>, options: List<Option>?) =
    stringElements
      .filter { it.isNotBlank() }
      .map { it.trim() }
      .map { element -> options?.find { it.id == element }?.displayText ?: element }
      .reduceOrNull { acc, value -> "$acc\n$value" } ?: ""
}
