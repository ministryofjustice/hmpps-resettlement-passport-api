package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AssessmentSkipRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DpsCaseNoteSubType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LatestResettlementAssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LatestResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentSkipEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.AssessmentSkipRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.YamlResettlementAssessmentStrategy
import java.time.LocalDateTime

@Service
class ResettlementAssessmentService(
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val caseNotesService: CaseNotesService,
  private val pathwayAndStatusService: PathwayAndStatusService,
  private val assessmentSkipRepository: AssessmentSkipRepository,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getResettlementAssessmentSummaryByNomsId(nomsId: String, assessmentType: ResettlementAssessmentType): List<PrisonerResettlementAssessment> {
    val prisonerEntity = getPrisonerEntityOrThrow(nomsId)
    return getAssessmentSummary(prisonerEntity, assessmentType)
  }

  private fun getAssessmentSummary(
    prisonerEntity: PrisonerEntity,
    assessmentType: ResettlementAssessmentType,
  ): List<PrisonerResettlementAssessment> {
    val latestForEachPathway =
      resettlementAssessmentRepository.findLatestForEachPathway(prisonerEntity, assessmentType)
        .associateBy { it.pathway }
    return Pathway.entries.map {
      val resettlementAssessmentForPathway = latestForEachPathway[it]
      if (resettlementAssessmentForPathway == null) {
        PrisonerResettlementAssessment(it, ResettlementAssessmentStatus.NOT_STARTED)
      } else {
        PrisonerResettlementAssessment(
          it,
          resettlementAssessmentForPathway.assessmentStatus,
        )
      }
    }
  }

  private fun getPrisonerEntityOrThrow(nomsId: String) = (
    prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    )

  @Transactional
  fun submitResettlementAssessmentByNomsId(nomsId: String, assessmentType: ResettlementAssessmentType, auth: String, sendCombinedCaseNotes: Boolean) {
    // Check auth - must be NOMIS
    val authSource = getClaimFromJWTToken(auth, "auth_source")?.lowercase()
    if (authSource != "nomis") {
      throw ServerWebInputException("Endpoint must be called with a user token with authSource of NOMIS")
    }

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    val assessmentList = mutableListOf<ResettlementAssessmentEntity>()

    // For each pathway, get the latest complete assessment
    Pathway.entries.forEach { pathway ->
      val resettlementAssessment = resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        prisoner = prisonerEntity,
        pathway = pathway,
        assessmentType = assessmentType,
        assessmentStatus = listOf(ResettlementAssessmentStatus.COMPLETE),
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
      val caseNotesText = "${getFirstLineOfBcstCaseNote(assessment.pathway, assessment.assessmentType)}\n\n${assessment.caseNoteText}"

      // Old way - post case note to DPS
      if (!sendCombinedCaseNotes) {
        caseNotesService.postBCSTCaseNoteToDps(
          nomsId = prisonerEntity.nomsId,
          notes = caseNotesText,
          userId = assessment.createdByUserId,
          subType = DpsCaseNoteSubType.BCST,
        )
      }

      // Update pathway status
      pathwayAndStatusService.updatePathwayStatus(
        nomsId = nomsId,
        pathwayAndStatus = PathwayAndStatus(assessment.pathway, assessment.statusChangedTo!!),
      )

      // Update assessment status to SUBMITTED
      assessment.assessmentStatus = ResettlementAssessmentStatus.SUBMITTED
      assessment.submissionDate = LocalDateTime.now()
      resettlementAssessmentRepository.save(assessment)
    }

    // New way - combine and send case note to DPS and Delius
    if (sendCombinedCaseNotes) {
      val dpsSubType = when (assessmentType) {
        ResettlementAssessmentType.BCST2 -> DpsCaseNoteSubType.INR
        ResettlementAssessmentType.RESETTLEMENT_PLAN -> DpsCaseNoteSubType.PRR
      }

      val groupedAssessmentsDps = processAndGroupAssessmentCaseNotes(assessmentList, true, assessmentType)
      groupedAssessmentsDps.forEach {
        caseNotesService.postBCSTCaseNoteToDps(
          nomsId = prisonerEntity.nomsId,
          notes = it.caseNoteText,
          userId = it.user.userId,
          subType = dpsSubType,
        )
      }

      val crn = resettlementPassportDeliusApiService.getCrn(nomsId)

      if (crn != null) {
        val groupedAssessmentsDelius = processAndGroupAssessmentCaseNotes(assessmentList, false, assessmentType)
        groupedAssessmentsDelius.forEach {
          caseNotesService.postBCSTCaseNoteToDelius(
            crn = crn,
            prisonCode = prisonerSearchApiService.findPrisonerPersonalDetails(prisonerEntity.nomsId).prisonId,
            notes = it.caseNoteText,
            name = it.user.name,
            assessmentType = assessmentType,
          )
        }
      } else {
        // TODO PSFR-1386 Add retry mechanism if we fail to send the case note
        log.warn("Cannot send report case note to Delius as no CRN is available for prisoner ${prisonerEntity.nomsId}.")
      }
    }
  }

  fun processAndGroupAssessmentCaseNotes(assessmentList: List<ResettlementAssessmentEntity>, limitChars: Boolean, assessmentType: ResettlementAssessmentType): List<UserAndCaseNote> {
    val maxCaseNoteLength = if (limitChars) {
      // Limit for DPS is 4000 but set this lower to account for line breaks between each pathway and the Part x of y text at the start (should be about 25 chars)
      3950
    } else {
      Int.MAX_VALUE
    }

    val userToCaseNoteMap = assessmentList.map {
      UserAndCaseNote(
        user = User(userId = it.createdByUserId, name = it.createdBy),
        caseNoteText = "${it.pathway.displayName}\n\n${it.caseNoteText}",
      )
    }.groupBy { it.user }

    val caseNoteList = userToCaseNoteMap.flatMap { (user, notes) ->
      val combinedCaseNotes = splitToCharLimit(notes.map { it.caseNoteText }, maxCaseNoteLength)
      combinedCaseNotes.map { UserAndCaseNote(user = user, caseNoteText = it) }
    }

    val descriptionPrefix = when (assessmentType) {
      ResettlementAssessmentType.BCST2 -> "NOMIS - Immediate needs report"
      ResettlementAssessmentType.RESETTLEMENT_PLAN -> "NOMIS - Pre-release report"
    }

    return if (caseNoteList.size > 1) {
      caseNoteList.mapIndexed { index, caseNote ->
        UserAndCaseNote(
          user = caseNote.user,
          caseNoteText = "Part ${index + 1} of ${caseNoteList.size}\n\n${caseNote.caseNoteText}",
          description = "$descriptionPrefix - Part ${index + 1} of ${caseNoteList.size}",
        )
      }
    } else {
      caseNoteList
    }
  }

  fun splitToCharLimit(caseNotes: List<String>, maxCaseNoteLength: Int): List<String> {
    val splitCaseNotes = mutableListOf<List<String>>()
    var currentCaseNote = mutableListOf<String>()

    caseNotes.forEach {
      if ((currentCaseNote.joinToString("").length + it.length) <= maxCaseNoteLength) {
        currentCaseNote.add(it)
      } else {
        splitCaseNotes.add(currentCaseNote)
        currentCaseNote = mutableListOf()
        currentCaseNote.add(it)
      }
    }

    if (currentCaseNote.isNotEmpty()) {
      splitCaseNotes.add(currentCaseNote)
    }

    return splitCaseNotes.map { it.joinToString("\n\n\n") }
  }

  data class UserAndCaseNote(
    val user: User,
    val caseNoteText: String,
    val description: String? = null,
  )

  data class User(
    val userId: String,
    val name: String,
  )

  fun getLatestResettlementAssessmentByNomsIdAndPathway(nomsId: String, pathway: Pathway, resettlementAssessmentStrategies: YamlResettlementAssessmentStrategy): LatestResettlementAssessmentResponse {
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    val latestResettlementAssessment = convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(
      resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentStatusOrderByCreationDateDesc(prisonerEntity, pathway, ResettlementAssessmentStatus.SUBMITTED)
        ?: throw ResourceNotFoundException("No submitted resettlement assessment found for prisoner $nomsId / pathway $pathway"),
      resettlementAssessmentStrategies,
    )

    val originalResettlementAssessment = convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(
      resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentStatusOrderByCreationDateAsc(prisonerEntity, pathway, ResettlementAssessmentStatus.SUBMITTED)
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

  fun convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(resettlementAssessmentEntity: ResettlementAssessmentEntity, resettlementAssessmentStrategies: YamlResettlementAssessmentStrategy): ResettlementAssessmentResponse {
    val questionsAndAnswers = resettlementAssessmentEntity.assessment.assessment.mapNotNull {
      val question = resettlementAssessmentStrategies.getQuestionById(it.questionId, resettlementAssessmentEntity.pathway, resettlementAssessmentEntity.assessmentType)
      if (question.id !in listOf("SUPPORT_NEEDS", "SUPPORT_NEEDS_PRERELEASE", "CASE_NOTE_SUMMARY")) {
        LatestResettlementAssessmentResponseQuestionAndAnswer(
          questionTitle = question.title,
          answer = convertAnswerToString(question.options, it.answer),
          originalPageId = resettlementAssessmentStrategies.findPageIdFromQuestionId(it.questionId, resettlementAssessmentEntity.assessmentType, resettlementAssessmentEntity.pathway),
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

  @Transactional
  fun skipAssessment(nomsId: String, assessmentType: ResettlementAssessmentType, request: AssessmentSkipRequest) {
    if (assessmentType != ResettlementAssessmentType.BCST2) {
      throw ServerWebInputException("Only BCST2 assessment can currently be skipped")
    }

    val prisonerEntity = getPrisonerEntityOrThrow(nomsId)
    val assessmentSummary = getAssessmentSummary(prisonerEntity, assessmentType)
    if (assessmentSummary.any { it.assessmentStatus != ResettlementAssessmentStatus.NOT_STARTED }) {
      throw ServerWebInputException("Cannot skip assessment that has already been started")
    }

    assessmentSkipRepository.save(
      AssessmentSkipEntity(
        prisonerId = prisonerEntity.id!!,
        assessmentType = assessmentType,
        reason = request.reason,
        moreInfo = request.moreInfo,
      ),
    )
  }
}
