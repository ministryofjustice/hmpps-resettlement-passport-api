package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DpsCaseNoteSubType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.AssessmentSkipRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.LatestResettlementAssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.LatestResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentSubmitResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentSkipEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseNoteRetryEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.AssessmentSkipRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseNoteRetryRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ProfileTagsRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.AssessmentConfigOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.ResettlementAssessmentStrategy
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.processProfileTags
import java.time.LocalDate
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
  private val caseNoteRetryRepository: CaseNoteRetryRepository,
  private val profileTagsRepository: ProfileTagsRepository,
  @Value("\${psfr.base.url}") private val psfrBaseUrl: String,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getResettlementAssessmentSummaryByNomsId(nomsId: String, assessmentType: ResettlementAssessmentType): List<PrisonerResettlementAssessment> {
    val prisonerEntity = getPrisonerEntityOrThrow(nomsId)
    val resettlementEntityList = resettlementAssessmentRepository.findLatestForEachPathway(prisonerEntity.id(), assessmentType)
    return getAssessmentSummary(resettlementEntityList)
  }

  @Transactional
  fun getResettlementAssessmentSummaryByNomsIdAndCreationDate(
    nomsId: String,
    assessmentType: ResettlementAssessmentType,
    fromDate: LocalDate,
    toDate: LocalDate,
  ): List<PrisonerResettlementAssessment> {
    val prisonerEntity = getPrisonerEntityOrThrow(nomsId)
    val resettlementEntityList = resettlementAssessmentRepository.findLatestForEachPathwayAndCreationDateBetween(
      prisonerEntity.id(),
      assessmentType,
      fromDate.atStartOfDay(),
      toDate.atStartOfDay(),
    )
    return getAssessmentSummary(resettlementEntityList)
  }

  private fun getAssessmentSummary(
    resettlementEntityList: List<ResettlementAssessmentEntity>,
  ): List<PrisonerResettlementAssessment> {
    val latestForEachPathway = resettlementEntityList.associateBy { it.pathway }
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
  fun submitResettlementAssessmentByNomsId(nomsId: String, assessmentType: ResettlementAssessmentType, useNewDeliusCaseNoteFormat: Boolean, auth: String, resettlementAssessmentStrategies: ResettlementAssessmentStrategy): ResettlementAssessmentSubmitResponse {
    // Check auth - must be NOMIS
    val authSource = getClaimFromJWTToken(auth, "auth_source")?.lowercase()
    if (authSource != "nomis") {
      throw ServerWebInputException("Endpoint must be called with a user token with authSource of NOMIS")
    }

    val name = getClaimFromJWTToken(auth, "name") ?: throw ServerWebInputException("Cannot get name from auth token")
    val userId = getClaimFromJWTToken(auth, "sub") ?: throw ServerWebInputException("Cannot get sub from auth token")

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    val assessmentList = mutableListOf<ResettlementAssessmentEntity>()
    val profileTagList = ProfileTagList(listOf())
    var tagList = profileTagList.tags
    // For each pathway, get the latest complete assessment
    Pathway.entries.forEach { pathway ->

      val resettlementAssessment = resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        prisonerId = prisonerEntity.id(),
        pathway = pathway,
        assessmentType = assessmentType,
        assessmentStatus = listOf(ResettlementAssessmentStatus.COMPLETE),
      )
      if (resettlementAssessment != null) {
        val pages = resettlementAssessmentStrategies.getConfigPages(assessmentType, pathway, resettlementAssessment.version)
        log.info("Pages size ${pages.size}")
        assessmentList.add(resettlementAssessment)
        tagList = tagList + processProfileTags(resettlementAssessment, pages)
      }
    }
    profileTagList.tags = tagList
    log.info("ProfileTagList size ${profileTagList.tags.size}")

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
    val profileTagsEntity: ProfileTagsEntity
    if (profileTagList.tags.isNotEmpty()) {
      if (prisonerEntity.id?.let { profileTagsRepository.existsProfileTagsEntityByPrisonerId(it) } == true) {
        profileTagsEntity = profileTagsRepository.findFirstByPrisonerId(prisonerEntity.id())
        profileTagsEntity.profileTags = profileTagList
        profileTagsEntity.updatedDate = LocalDateTime.now()
      } else {
        profileTagsEntity = prisonerEntity.id?.let {
          ProfileTagsEntity(
            id = null,
            prisonerId = it,
            profileTags = profileTagList,
            updatedDate = LocalDateTime.now(),
          )
        }!!
      }
      profileTagsRepository.save(profileTagsEntity)
    }
    val failedCaseNotes = mutableListOf<UserAndCaseNote>()
    val prisonCode = prisonerSearchApiService.findPrisonerPersonalDetails(prisonerEntity.nomsId).prisonId
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

    // If new format is required, just send a single case note to Delius, otherwise use the old method
    val groupedAssessmentsDelius = if (useNewDeliusCaseNoteFormat) {
      listOf(generateLinkOnlyDeliusCaseNote(prisonerEntity.nomsId, name, userId, assessmentType))
    } else {
      processAndGroupAssessmentCaseNotes(assessmentList, false, assessmentType)
    }

    if (crn != null) {
      groupedAssessmentsDelius.forEach {
        val success = caseNotesService.postBCSTCaseNoteToDelius(
          crn = crn,
          prisonCode = prisonCode,
          notes = it.caseNoteText,
          name = it.user.name,
          deliusCaseNoteType = it.deliusCaseNoteType,
          description = it.description,
        )
        if (!success) {
          log.warn("Cannot send report case note to Delius due to error on API for prisoner ${prisonerEntity.nomsId}. Adding to failed case notes for retry.")
          failedCaseNotes.add(it)
        }
      }
    } else {
      log.warn("Cannot send report case notes to Delius as no CRN is available for prisoner ${prisonerEntity.nomsId}. Adding to failed case notes for retry.")
      failedCaseNotes.addAll(groupedAssessmentsDelius)
    }

    caseNoteRetryRepository.saveAll(
      failedCaseNotes.map { failedCaseNote ->
        CaseNoteRetryEntity(
          id = null,
          prisoner = prisonerEntity,
          type = failedCaseNote.deliusCaseNoteType,
          notes = failedCaseNote.caseNoteText,
          author = failedCaseNote.user.name,
          prisonCode = prisonCode,
          originalSubmissionDate = LocalDateTime.now(),
          retryCount = 0,
          // Set this to now to retry on the next run of the retry cron job
          nextRuntime = LocalDateTime.now(),
        )
      },
    )
    return ResettlementAssessmentSubmitResponse(deliusCaseNoteFailed = failedCaseNotes.isNotEmpty())
  }

  fun processAndGroupAssessmentCaseNotes(assessmentList: List<ResettlementAssessmentEntity>, limitChars: Boolean, assessmentType: ResettlementAssessmentType): List<UserAndCaseNote> {
    val deliusCaseNoteType = convertToDeliusCaseNoteType(assessmentType)
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
        deliusCaseNoteType = deliusCaseNoteType,
        description = null,
      )
    }.groupBy { it.user }

    val caseNoteList = userToCaseNoteMap.flatMap { (user, notes) ->
      val combinedCaseNotes = splitToCharLimit(notes.map { it.caseNoteText }, maxCaseNoteLength)
      combinedCaseNotes.map {
        UserAndCaseNote(
          user = user,
          caseNoteText = it,
          deliusCaseNoteType = deliusCaseNoteType,
          description = null,
        )
      }
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
          deliusCaseNoteType = deliusCaseNoteType,
          description = "$descriptionPrefix - Part ${index + 1} of ${caseNoteList.size}",
        )
      }
    } else {
      caseNoteList.map { caseNote ->
        UserAndCaseNote(
          user = caseNote.user,
          caseNoteText = caseNote.caseNoteText,
          deliusCaseNoteType = deliusCaseNoteType,
          description = null,
        )
      }
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
    val deliusCaseNoteType: DeliusCaseNoteType,
    val description: String? = null,
  )

  data class User(
    val userId: String,
    val name: String,
  )

  fun getLatestResettlementAssessmentByNomsIdAndPathway(nomsId: String, pathway: Pathway, resettlementAssessmentStrategies: ResettlementAssessmentStrategy): LatestResettlementAssessmentResponse {
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    val latestResettlementAssessment = convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentStatusOrderByCreationDateDesc(prisonerEntity.id(), pathway, ResettlementAssessmentStatus.SUBMITTED)
        ?: throw ResourceNotFoundException("No submitted resettlement assessment found for prisoner $nomsId / pathway $pathway"),
      resettlementAssessmentStrategies,
    )

    val originalResettlementAssessment = convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentStatusOrderByCreationDateAsc(prisonerEntity.id(), pathway, ResettlementAssessmentStatus.SUBMITTED)
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

  fun getLatestResettlementAssessmentByNomsIdAndPathwayAndCreationDate(
    nomsId: String,
    pathway: Pathway,
    resettlementAssessmentStrategies: ResettlementAssessmentStrategy,
    fromDate: LocalDate,
    toDate: LocalDate,
  ): LatestResettlementAssessmentResponse {
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    val latestResettlementAssessment = convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentStatusAndCreationDateBetweenOrderByCreationDateDesc(
        prisonerEntity.id(),
        pathway,
        ResettlementAssessmentStatus.SUBMITTED,
        fromDate.atStartOfDay(),
        toDate.atStartOfDay(),
      )
        ?: throw ResourceNotFoundException("No submitted resettlement assessment found for prisoner $nomsId / pathway $pathway"),
      resettlementAssessmentStrategies,
    )

    val originalResettlementAssessment = convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentStatusAndCreationDateBetweenOrderByCreationDateAsc(
        prisonerEntity.id(),
        pathway,
        ResettlementAssessmentStatus.SUBMITTED,
        fromDate.atStartOfDay(),
        toDate.atStartOfDay(),
      )
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

  fun convertFromResettlementAssessmentEntityToResettlementAssessmentResponse(resettlementAssessmentEntity: ResettlementAssessmentEntity, resettlementAssessmentStrategies: ResettlementAssessmentStrategy): ResettlementAssessmentResponse {
    val config = resettlementAssessmentStrategies.getConfig(resettlementAssessmentEntity.pathway, resettlementAssessmentEntity.assessmentType, resettlementAssessmentEntity.version)
    val questionsAndAnswers = config.pages.filter { it.questions != null }.flatMap { it.questions!! }.mapNotNull { questionFromConfig ->
      val questionAndAnswerFromDatabase = resettlementAssessmentEntity.assessment.assessment.firstOrNull { it.questionId == questionFromConfig.id }
      if (questionAndAnswerFromDatabase != null && questionAndAnswerFromDatabase.questionId !in listOf("SUPPORT_NEEDS", "SUPPORT_NEEDS_PRERELEASE", "CASE_NOTE_SUMMARY")) {
        LatestResettlementAssessmentResponseQuestionAndAnswer(
          questionTitle = questionFromConfig.title,
          answer = convertAnswerToString(questionFromConfig.options, questionAndAnswerFromDatabase.answer),
          originalPageId = resettlementAssessmentStrategies.findPageIdFromQuestionId(questionFromConfig.id, resettlementAssessmentEntity.assessmentType, resettlementAssessmentEntity.pathway, resettlementAssessmentEntity.version),
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

  fun convertAnswerToString(options: List<AssessmentConfigOption>?, answer: Answer<*>): String? {
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

  fun convertFromListToStringWithLineBreaks(stringElements: List<String>, options: List<AssessmentConfigOption>?) =
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
    val resettlementEntityList = resettlementAssessmentRepository.findLatestForEachPathway(prisonerEntity.id(), assessmentType)
    val assessmentSummary = getAssessmentSummary(resettlementEntityList)
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

  fun generateLinkOnlyDeliusCaseNote(nomsId: String, name: String, userId: String, assessmentType: ResettlementAssessmentType): UserAndCaseNote {
    return UserAndCaseNote(
      user = User(userId, name),
      deliusCaseNoteType = convertToDeliusCaseNoteType(assessmentType),
      caseNoteText = generateLinkOnlyDeliusCaseNoteText(nomsId, assessmentType, psfrBaseUrl),
    )
  }

  fun deleteAllResettlementAssessments(nomsId: String) {
    val prisoner = getPrisonerEntityOrThrow(nomsId)

    resettlementAssessmentRepository.findAllByPrisonerId(prisoner.id!!).forEach {
        assessment ->
      assessment.deleted = true
      assessment.deletedDate = LocalDateTime.now()
      resettlementAssessmentRepository.save(assessment)
    }
  }
}
