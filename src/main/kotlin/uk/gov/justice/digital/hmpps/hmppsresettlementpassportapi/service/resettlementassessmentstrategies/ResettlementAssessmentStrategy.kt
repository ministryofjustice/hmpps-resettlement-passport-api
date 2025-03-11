package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentVersion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Validation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ProfileTagsRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.validateAnswer
import java.time.LocalDateTime

@Service
class ResettlementAssessmentStrategy(
  private val config: AssessmentQuestionSets,
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val pathwayStatusRepository: PathwayStatusRepository,
  private val profileTagsRepository: ProfileTagsRepository,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val telemetryClient: TelemetryClient,
) {

  fun getConfigPages(
    assessmentType: ResettlementAssessmentType,
    pathway: Pathway,
    version: Int,
  ): List<AssessmentConfigPage> {
    val pages = getConfig(
      pathway,
      assessmentType,
      version,
    ).pages

    return pages
  }
  fun getConfig(pathway: Pathway, assessmentType: ResettlementAssessmentType, version: Int): AssessmentQuestionSet {
    val pathwayConfig = config.questionSets.first { it.pathway == pathway && it.version == version }

    return AssessmentQuestionSet(
      version = pathwayConfig.version,
      requireStatusQuestion = pathwayConfig.requireStatusQuestion,
      pathway = pathwayConfig.pathway,
      // May need to remove either ASSESSMENT_SUMMARY or PRERELEASE_ASSESSMENT_SUMMARY page based on whether it's a BCST2 or RESETTLEMENT_PLAN
      pages = pathwayConfig.pages.filter {
        if (assessmentType == ResettlementAssessmentType.BCST2) {
          it.id != "PRERELEASE_ASSESSMENT_SUMMARY"
        } else {
          it.id != "ASSESSMENT_SUMMARY"
        }
      },
    )
  }

  fun getNextPageId(
    assessment: ResettlementAssessmentRequest,
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    currentPage: String?,
    version: Int = 1,
  ): String {
    // Validate the request
    if (currentPage != null && assessment.questionsAndAnswers == null) {
      throw ServerWebInputException("If current page is defined, questions must also be defined.")
    }

    val existingAssessment = getExistingAssessment(nomsId, pathway, assessmentType)
    val edit = existingAssessment?.assessmentStatus == ResettlementAssessmentStatus.SUBMITTED

    val config = getConfig(pathway, assessmentType, version)
    return if (currentPage == null) {
      // Option 1 - If the current page is null then send back the first page unless there is already an assessment completed, in which case go straight to CHECK_ANSWERS
      if (existingAssessment == null) {
        config.pages.getOrNull(0)?.id ?: throw IllegalStateException("Config for pathway $pathway has no pages")
      } else {
        "CHECK_ANSWERS"
      }
    } else if (currentPage == "CHECK_ANSWERS") {
      // Option 2 - If the current page is CHECK_ANSWERS then something has gone wrong as this is the end of the flow, and we should be called the submit endpoint instead
      throw ServerWebInputException("Cannot get the next question from CHECK_ANSWERS as this is the end of the flow for this pathway.")
    } else {
      // Option 3 - If none of the above calculate next page from the config
      val pageConfig = config.pages.find { it.id == currentPage }
        ?: throw ServerWebInputException("Cannot find page $currentPage on pathway $pathway")
      val nextPage = chooseNextPage(pageConfig, assessment.questionsAndAnswers ?: emptyList(), edit, assessmentType)

      return nextPage
    }
  }

  private fun chooseNextPage(
    pageConfig: AssessmentConfigPage,
    questionsAndAnswers: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>,
    edit: Boolean,
    assessmentType: ResettlementAssessmentType,
  ): String {
    // If there is just a single nextPageId then just send that back
    val nextPageId =
      if (pageConfig.nextPageLogic != null && pageConfig.nextPageLogic.size == 1 && pageConfig.nextPageLogic[0].questionId == null) {
        pageConfig.nextPageLogic[0].nextPageId
      } else {
        pageConfig.nextPageLogic?.firstOrNull { npl -> npl.answers?.contains(questionsAndAnswers.first { it.question == npl.questionId }.answer) == true }?.nextPageId
      }

    if (nextPageId == null) {
      throw RuntimeException("Cannot find next page id. This could be the answer to a mandatory question is missing or invalid configuration.")
    }

    return if (nextPageId != "FINAL_QUESTION_NEXT_PAGE") {
      nextPageId
    } else {
      finalQuestionNextPage(edit, assessmentType)
    }
  }

  private fun finalQuestionNextPage(edit: Boolean, assessmentType: ResettlementAssessmentType): String = if (!edit && assessmentType == ResettlementAssessmentType.BCST2) {
    "ASSESSMENT_SUMMARY"
  } else if (!edit && assessmentType == ResettlementAssessmentType.RESETTLEMENT_PLAN) {
    "PRERELEASE_ASSESSMENT_SUMMARY"
  } else {
    "CHECK_ANSWERS"
  }

  fun completeAssessment(
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    assessment: ResettlementAssessmentCompleteRequest,
    auth: String,
    declaration: Boolean,
  ) {
    // Check auth - must be NOMIS
    val authSource = getClaimFromJWTToken(auth, "auth_source")?.lowercase()
    if (authSource != "nomis") {
      throw ServerWebInputException("Endpoint must be called with a user token with authSource of NOMIS")
    }

    // Get name and sub (userId) from auth
    val name = getClaimFromJWTToken(auth, "name") ?: throw ServerWebInputException("Cannot get name from auth token")
    val userId = getClaimFromJWTToken(auth, "sub") ?: throw ServerWebInputException("Cannot get sub from auth token")

    // Check if the latest assessment is submitted - if so this is an edit
    val edit = getExistingAssessment(
      nomsId,
      pathway,
      assessmentType,
    )?.assessmentStatus == ResettlementAssessmentStatus.SUBMITTED

    // Check that question and answer set is valid
    validateQuestionAndAnswerSet(pathway, assessment, edit, assessmentType)

    // Obtain prisoner from database, if exists
    val prisonerEntity = loadPrisoner(nomsId)
    // If it's not an edit then use COMPLETE status, else use SUBMITTED
    val resettlementAssessmentStatus = if (!edit) {
      ResettlementAssessmentStatus.COMPLETE
    } else {
      ResettlementAssessmentStatus.SUBMITTED
    }

    // If it's not an edit and the report version supports statuses, get the status and the case note out of the questions and answers
    var status: Status? = null
    var caseNoteText: String? = null

    if (!edit && getConfig(pathway, assessmentType, assessment.version).requireStatusQuestion) {
      // Get statusChangedTo out of SUPPORT_NEEDS question and convert to a Status
      val supportNeedsQuestionAndAnswer = if (assessmentType == ResettlementAssessmentType.RESETTLEMENT_PLAN) {
        assessment.questionsAndAnswers.first { it.question == "SUPPORT_NEEDS_PRERELEASE" }
      } else {
        assessment.questionsAndAnswers.first { it.question == "SUPPORT_NEEDS" }
      }

      status = convertFromSupportNeedAnswerToStatus(supportNeedsQuestionAndAnswer.answer)

      // Get caseNoteText out of CASE_NOTE_SUMMARY question as String (if available - as of PSFR-1690 this is no longer mandatory)
      val caseNoteQuestionAndAnswer =
        assessment.questionsAndAnswers.find { it.question == "CASE_NOTE_SUMMARY" }

      caseNoteText = convertFromStringAnswer(caseNoteQuestionAndAnswer?.answer)
    }

    // Map assessment into correct format to be added into database
    val resettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentQuestionAndAnswerList(
      assessment = assessment.questionsAndAnswers.map {
        ResettlementAssessmentSimpleQuestionAndAnswer(
          it.question,
          it.answer,
        )
      },
    )

    // Create new resettlement assessment entity and save to database
    val resettlementAssessmentEntity = ResettlementAssessmentEntity(
      id = null,
      prisonerId = prisonerEntity.id(),
      pathway = pathway,
      statusChangedTo = status,
      assessmentType = assessmentType,
      assessment = resettlementAssessmentQuestionAndAnswerList,
      creationDate = LocalDateTime.now(),
      createdBy = name,
      assessmentStatus = resettlementAssessmentStatus,
      caseNoteText = caseNoteText,
      createdByUserId = userId,
      submissionDate = if (edit) LocalDateTime.now() else null,
      version = assessment.version,
      userDeclaration = declaration,
    )

    saveAssessment(resettlementAssessmentEntity)
    generateProfileTags(prisonerEntity, assessmentType)

    // If it's an edit, send event to app insights
    if (edit) {
      val prisonCode = prisonerSearchApiService.findPrisonerPersonalDetails(nomsId).prisonId
      telemetryClient.trackEvent(
        "PSFR_ReportUpdated",
        mapOf(
          "reportType" to assessmentType.name,
          "pathway" to pathway.name,
          "prisonId" to prisonCode,
          "prisonerId" to nomsId,
          "submittedBy" to userId,
          "authSource" to authSource,
        ),
        null,
      )
    }
  }

  fun validateQuestionAndAnswerSet(
    pathway: Pathway,
    assessment: ResettlementAssessmentCompleteRequest,
    edit: Boolean,
    assessmentType: ResettlementAssessmentType = ResettlementAssessmentType.BCST2,
  ) {
    // Convert to Map of pages to List of questionsAndAnswers
    val nodeToQuestionMap = assessment.questionsAndAnswers
      .groupByTo(LinkedHashMap()) { qa ->
        getConfig(pathway, assessmentType, assessment.version).pages
          .firstOrNull { configPage ->
            if (configPage.questions != null) {
              qa.question in configPage.questions.getFlattenedListOfQuestions().map { it.id }
            } else {
              false
            }
          }
          ?: throw ServerWebInputException("Error validating questions and answers - cannot find a node with question [${qa.question}]")
      }
      .mapValues { entry ->
        entry.value.map {
          ResettlementAssessmentQuestionAndAnswer(
            getFlattenedQuestionList(
              pathway,
              assessmentType,
              assessment.version,
            ).firstOrNull { q -> q.id == it.question }
              ?: throw ServerWebInputException("Error validating questions and answers - cannot find a question with id [${it.question}]"),
            it.answer,
            findPageIdFromQuestionId(it.question, assessmentType, pathway, assessment.version),
          )
        }
      }

    // Ensure that only the correct base questions are answered in each page (validate nested questions later)
    nodeToQuestionMap.forEach { (key, value) ->
      val expectedBaseQuestions = key.questions?.map { it.mapToResettlementAssessmentQuestion(key.id) } ?: listOf()
      val nestedQuestions = key.questions.getNestedQuestions().map { it.mapToResettlementAssessmentQuestion(key.id) }
      val actualBaseQuestions = value.map { it.question } - nestedQuestions.toSet()
      if (expectedBaseQuestions.sortedBy { it.id } != actualBaseQuestions.sortedBy { it.id }) {
        throw ServerWebInputException("Error validating questions and answers - wrong questions answered on page [${key.id}]. Expected [${expectedBaseQuestions.map { it.id }}] but found [${actualBaseQuestions.map { it.id }}]")
      }
    }

    // TODO PSFR-1545 validate nested questions are valid

    // Go through the expected page flow and check that actual pages match up
    var currentNode: AssessmentConfigPage? = null
    var pageNumber = 0

    while (true) {
      try {
        val actualPage: AssessmentConfigPage? = nodeToQuestionMap.keys.elementAtOrNull(pageNumber)
        val expectedPage: String = if (currentNode != null) {
          chooseNextPage(
            currentNode,
            assessment.questionsAndAnswers,
            edit,
            assessmentType,
          )
        } else {
          getConfig(pathway, assessmentType, assessment.version).pages[0].id
        }
        if (expectedPage == "CHECK_ANSWERS") {
          break
        }
        if (expectedPage != actualPage?.id) {
          throw ServerWebInputException("Error validating questions and answers - expected page [$expectedPage] is different to actual page [${actualPage?.id}] at index [$pageNumber]")
        }
        currentNode = nodeToQuestionMap.keys.elementAt(pageNumber)
        pageNumber++
      } catch (e: Exception) {
        throw ServerWebInputException("Error validating questions and answers - error validating page flow [${e.message}]")
      }
    }

    // Ensure the actual and expected pages are the same size (i.e. no extra pages are present)
    if (pageNumber != nodeToQuestionMap.size) {
      throw ServerWebInputException("Error validating questions and answers - incorrect number of pages found - expected [$pageNumber] but found [${nodeToQuestionMap.size}]")
    }

    // Validation any mandatory and regex validation for each question
    nodeToQuestionMap.values.flatten().forEach { validateAnswer(it) }
  }

  fun getPageFromId(
    nomsId: String,
    pathway: Pathway,
    pageId: String,
    assessmentType: ResettlementAssessmentType,
    version: Int = 1,
  ): ResettlementAssessmentResponsePage {
    // Get the latest complete assessment (if exists)
    var existingAssessment = getExistingAssessment(nomsId, pathway, assessmentType)
    var resettlementPlanCopy = false
    val edit = existingAssessment?.assessmentStatus == ResettlementAssessmentStatus.SUBMITTED

    // If this is a RESETTLEMENT_PLAN (BCST3) type and there is not existing assessment we should use an existing BCST2 if available.
    if (existingAssessment == null && assessmentType == ResettlementAssessmentType.RESETTLEMENT_PLAN) {
      existingAssessment = getExistingAssessment(nomsId, pathway, ResettlementAssessmentType.BCST2)

      if (existingAssessment != null) {
        resettlementPlanCopy = true
        // remove SUPPORT_NEEDS and replace with SUPPORT_NEEDS_PRERELEASE which has more options
        val questions = existingAssessment.questionsAndAnswers.toMutableList()
        questions.removeIf { it.questionId == "SUPPORT_NEEDS" }
        if (!questions.any { it.questionId == "SUPPORT_NEEDS_PRERELEASE" }) {
          questions.add(
            ResettlementAssessmentSimpleQuestionAndAnswer(
              "SUPPORT_NEEDS_PRERELEASE",
              StringAnswer(null),
            ),
          )
        }
        if (!questions.any { it.questionId == "CASE_NOTE_SUMMARY" }) {
          questions.add(
            ResettlementAssessmentSimpleQuestionAndAnswer(
              "CASE_NOTE_SUMMARY",
              StringAnswer(null),
            ),
          )
        }
        existingAssessment =
          existingAssessment.copy(assessment = ResettlementAssessmentQuestionAndAnswerList(questions.toList()))
      }
    }

    // Get the current page
    val config = getConfig(pathway, assessmentType, version)

    val page = config.pages.find { it.id == pageId }
      ?: throw ServerWebInputException("Page requested [$pageId] does not exist in config.")

    // Convert to ResettlementAssessmentPage DTO
    var resettlementAssessmentResponsePage = ResettlementAssessmentResponsePage(
      id = page.id,
      questionsAndAnswers = page.questions?.map {
        it.mapToResettlementAssessmentQuestionAndAnswer(pageId)
      } ?: listOf(),
      title = page.title,
    )

    // If there is an existing assessment, add the answer into the question
    // Do not populate the case notes
    // PSFR-1664 Only populate if the existing assessment is at the same version
    if (existingAssessment != null && existingAssessment.version == version) {
      // Only prefill CHECK_ANSWERS if existing report is the same type as the requested type
      if (resettlementAssessmentResponsePage.id == "CHECK_ANSWERS") {
        if (existingAssessment.assessmentType == assessmentType) {
          // If the existing assessment is submitted we are in an edit and don't want to send back the ASSESSMENT_SUMMARY or PRERELEASE_ASSESSMENT_SUMMARY questions
          val questionsToExclude = if (edit) {
            config.pages.filter { it.questions != null && it.id == "ASSESSMENT_SUMMARY" || it.id == "PRERELEASE_ASSESSMENT_SUMMARY" }
              .flatMap { it.questions!! }.map { it.mapToResettlementAssessmentQuestion(page.id) }
          } else {
            listOf()
          }
          val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer> =
            existingAssessment.assessment.assessment.mapNotNull { assessment ->
              val questionList =
                getFlattenedQuestionList(
                  pathway,
                  assessmentType,
                  version,
                ).firstOrNull { q -> q.id == assessment.questionId }
              if (questionList == null) {
                null
              } else {
                ResettlementAssessmentQuestionAndAnswer(
                  question = getFlattenedQuestionList(
                    pathway,
                    assessmentType,
                    version,
                  ).first { q -> q.id == assessment.questionId }
                    .removeNestedQuestions(),
                  answer = assessment.answer,
                  originalPageId = findPageIdFromQuestionId(assessment.questionId, assessmentType, pathway, version),
                )
              }
            }.filter { it.question !in questionsToExclude }

          resettlementAssessmentResponsePage = ResettlementAssessmentResponsePage(
            resettlementAssessmentResponsePage.id,
            questionsAndAnswers = questionsAndAnswers,
          )
        }
      } else {
        // Need to add in the answer for all questions include those nested within options. Only support one level of nesting.
        (
          resettlementAssessmentResponsePage.questionsAndAnswers + resettlementAssessmentResponsePage.questionsAndAnswers.filter { it.question.options != null }
            .flatMap { it.question.options!! }.filter { it.nestedQuestions != null }
            .flatMap { it.nestedQuestions!! }
          ).forEach { q ->
          val existingAnswer = existingAssessment.assessment.assessment.find { it.questionId == q.question.id }
          // Copy in existing answers _but_ we don't want to copy case notes from BCST2 to RESETTLEMENT_PLAN
          if (existingAnswer != null && !(resettlementPlanCopy && q.question.id == "CASE_NOTE_SUMMARY")) {
            q.answer = existingAnswer.answer
          }
          if (q.question.id == "SUPPORT_NEEDS_PRERELEASE") {
            q.answer = loadPathwayStatusAnswer(pathway, nomsId) ?: existingAnswer?.answer
          }
        }
      }
    }

    return resettlementAssessmentResponsePage
  }

  fun findPageIdFromQuestionId(
    questionId: String,
    assessmentType: ResettlementAssessmentType,
    pathway: Pathway,
    version: Int,
  ): String = getConfig(
    pathway,
    assessmentType,
    version,
  ).pages.firstOrNull { p ->
    (p.questions?.getFlattenedListOfQuestions()?.any { q -> q.id == questionId } == true)
  }?.id
    ?: throw RuntimeException("Cannot find page for question [$questionId] - check that the question is used in a page!")

  fun getFlattenedQuestionList(
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    version: Int,
  ): List<ResettlementAssessmentQuestion> = getConfig(pathway, assessmentType, version).pages.filter { it.questions != null }.flatMap { it.questions!! }
    .getFlattenedListOfQuestions()
    .map { it.mapToResettlementAssessmentQuestion(findPageIdFromQuestionId(it.id, assessmentType, pathway, version)) }

  fun getFlattenedQuestionListPreserveOrder(
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    version: Int,
  ): List<ResettlementAssessmentQuestion> {
    val result = mutableListOf<AssessmentConfigQuestion>()
    val questionList = getConfig(pathway, assessmentType, version).pages.filter { it.questions != null }.flatMap { it.questions!! }

    fun addQuestionWithNested(q: AssessmentConfigQuestion) {
      result.add(q)
      q.options?.forEach { it.nestedQuestions?.forEach { addQuestionWithNested(it) } }
    }

    questionList.forEach { addQuestionWithNested(it) }
    return result.map { it.mapToResettlementAssessmentQuestion(findPageIdFromQuestionId(it.id, assessmentType, pathway, version)) }
  }

  private fun saveAssessment(assessment: ResettlementAssessmentEntity): ResettlementAssessmentEntity = resettlementAssessmentRepository.save(assessment)

  private fun loadPrisoner(nomsId: String) = (
    prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    )

  private fun getExistingAssessment(
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
  ): ResettlementAssessmentEntity? {
    // Obtain prisoner from database, if exists
    val prisonerEntity = loadPrisoner(nomsId)

    // Define COMPLETE and SUBMITTED resettlement statuses
    val resettlementAssessmentStatuses =
      listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED)

    return resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
      prisonerEntity.id(),
      pathway,
      assessmentType,
      resettlementAssessmentStatuses,
    )
  }

  private fun loadPathwayStatusAnswer(pathway: Pathway, nomsId: String): StringAnswer? {
    val prisonerEntity = loadPrisoner(nomsId)
    val pathwayStatus = pathwayStatusRepository.findByPathwayAndPrisonerId(pathway, prisonerEntity.id()) ?: return null

    return StringAnswer(pathwayStatus.status.name)
  }

  fun getLatestResettlementAssessmentVersion(
    nomsId: String,
    assessmentType: ResettlementAssessmentType,
    pathway: Pathway,
  ): ResettlementAssessmentVersion = ResettlementAssessmentVersion(getExistingAssessment(nomsId, pathway, assessmentType)?.version)

  fun generateProfileTags(prisonerEntity: PrisonerEntity, assessmentType: ResettlementAssessmentType) {
    val profileTagList = ProfileTagList(listOf())
    val profileTagsEntity: ProfileTagsEntity
    var tagList = profileTagList.tags

    Pathway.entries.forEach { pathway ->
      val resettlementAssessment =
        resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
          prisonerId = prisonerEntity.id(),
          pathway = pathway,
          assessmentStatus = listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
        )
      if (resettlementAssessment != null) {
        val pages = getConfigPages(assessmentType, pathway, resettlementAssessment.version)
        val processProfileTagList = processProfileTags(resettlementAssessment, pages)
        if (processProfileTagList.isNotEmpty()) {
          tagList = tagList + processProfileTagList
        }
      }
    }
    profileTagList.tags = tagList

    if (tagList.isNotEmpty()) {
      if (prisonerEntity.id?.let { profileTagsRepository.existsProfileTagsEntityByPrisonerId(it) } == true) {
        profileTagsEntity = profileTagsRepository.findFirstByPrisonerId(prisonerEntity.id())
        profileTagsEntity.profileTags = profileTagList
        profileTagsEntity.updatedDate = LocalDateTime.now()
        profileTagsRepository.save(profileTagsEntity)
      } else {
        profileTagsEntity = prisonerEntity.id?.let {
          ProfileTagsEntity(
            id = null,
            prisonerId = it,
            profileTags = profileTagList,
            updatedDate = LocalDateTime.now(),
          )
        }!!
        profileTagsRepository.save(profileTagsEntity)
      }
    }
  }

  fun validateAssessment(nomsId: String, pathway: Pathway, assessmentType: ResettlementAssessmentType, resettlementAssessmentCompleteRequest: ResettlementAssessmentCompleteRequest) {
    // Check if the latest assessment is submitted - if so this is an edit
    val edit = getExistingAssessment(
      nomsId,
      pathway,
      assessmentType,
    )?.assessmentStatus == ResettlementAssessmentStatus.SUBMITTED

    validateQuestionAndAnswerSet(pathway, resettlementAssessmentCompleteRequest, edit, assessmentType)
  }
}

data class AssessmentQuestionSets(
  val questionSets: List<AssessmentQuestionSet>,
)

data class AssessmentQuestionSet(
  val version: Int,
  val requireStatusQuestion: Boolean = true,
  val pathway: Pathway?,
  val pages: List<AssessmentConfigPage>,
)

data class AssessmentConfigPage(
  val id: String,
  val title: String? = null,
  val questions: List<AssessmentConfigQuestion>?,
  val nextPageLogic: List<AssessmentConfigNextPageOption>?,
)

data class AssessmentConfigQuestion(
  val id: String,
  val title: String,
  val subTitle: String? = null,
  val type: TypeOfQuestion,
  val options: List<AssessmentConfigOption>? = null,
  val validationType: ValidationType = ValidationType.MANDATORY,
  val customValidation: Validation? = null,
  val validation: Validation? = null,
  val detailsTitle: String? = null,
  val detailsContent: String? = null,
) {
  fun getCorrectValidation(): Validation = validation ?: Validation(validationType, customValidation?.regex ?: "", customValidation?.message ?: "")
}

data class AssessmentConfigOption(
  val id: String,
  val displayText: String,
  val description: String? = null,
  val exclusive: Boolean = false,
  val nestedQuestions: List<AssessmentConfigQuestion>? = null,
  val freeText: Boolean = false,
  val tag: String? = null,
)

data class AssessmentConfigNextPageOption(
  val questionId: String? = null,
  val nextPageId: String,
  val answers: List<Answer<*>>? = null,
)
