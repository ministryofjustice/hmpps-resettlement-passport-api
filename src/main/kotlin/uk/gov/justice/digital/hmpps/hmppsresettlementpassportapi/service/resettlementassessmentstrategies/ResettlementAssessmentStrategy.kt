package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDateTime

@Service
class YamlResettlementAssessmentStrategy(
  private val config: AssessmentQuestionSets,
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val pathwayStatusRepository: PathwayStatusRepository,
) {

  fun getConfig(pathway: Pathway, assessmentType: ResettlementAssessmentType, version: Int): AssessmentQuestionSet {
    val pathwayConfig = config.questionSets.first { it.pathway == pathway && it.version == version }

    return AssessmentQuestionSet(
      version = pathwayConfig.version,
      pathway = pathwayConfig.pathway,
      // Need to remove either ASSESSMENT_SUMMARY or PRERELEASE_ASSESSMENT_SUMMARY page based on whether it's a BCST2 or RESETTLEMENT_PLAN
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
      val pageConfig = config.pages.find { it.id == currentPage } ?: throw ServerWebInputException("Cannot find page $currentPage on pathway $pathway")
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
    val nextPageId = if (pageConfig.nextPageLogic != null && pageConfig.nextPageLogic.size == 1 && pageConfig.nextPageLogic[0].questionId == null) {
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

  private fun finalQuestionNextPage(edit: Boolean, assessmentType: ResettlementAssessmentType): String {
    return if (!edit && assessmentType == ResettlementAssessmentType.BCST2) {
      "ASSESSMENT_SUMMARY"
    } else if (!edit && assessmentType == ResettlementAssessmentType.RESETTLEMENT_PLAN) {
      "PRERELEASE_ASSESSMENT_SUMMARY"
    } else {
      "CHECK_ANSWERS"
    }
  }

  fun completeAssessment(
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    assessment: ResettlementAssessmentCompleteRequest,
    auth: String,
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

    // If it's not an edit, get the status and the case note out of the questions and answers
    var status: Status? = null
    var caseNoteText: String? = null

    if (!edit) {
      // Get statusChangedTo out of SUPPORT_NEEDS question and convert to a Status
      val supportNeedsQuestionAndAnswer = if (assessmentType == ResettlementAssessmentType.RESETTLEMENT_PLAN) {
        assessment.questionsAndAnswers.first { it.question == "SUPPORT_NEEDS_PRERELEASE" }
      } else {
        assessment.questionsAndAnswers.first { it.question == "SUPPORT_NEEDS" }
      }

      status = convertFromSupportNeedAnswerToStatus(supportNeedsQuestionAndAnswer.answer)

      // Get caseNoteText out of CASE_NOTE_SUMMARY question as String
      val caseNoteQuestionAndAnswer =
        assessment.questionsAndAnswers.first { it.question == "CASE_NOTE_SUMMARY" }

      caseNoteText = convertFromStringAnswer(caseNoteQuestionAndAnswer.answer)
    }

    // Map assessment into correct format to be added into database
    val resettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentQuestionAndAnswerList(
      assessment = assessment.questionsAndAnswers.map {
        ResettlementAssessmentSimpleQuestionAndAnswer(
          it.question,
          it.answer,
        )
      }
    )

    // Create new resettlement assessment entity and save to database
    val resettlementAssessmentEntity = ResettlementAssessmentEntity(
      id = null,
      prisoner = prisonerEntity,
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
    )

    saveAssessment(resettlementAssessmentEntity)
  }

  fun validateQuestionAndAnswerSet(
    pathway: Pathway,
    assessment: ResettlementAssessmentCompleteRequest,
    edit: Boolean,
    assessmentType: ResettlementAssessmentType = ResettlementAssessmentType.BCST2,
    version: Int = 1,
  ) {
    // Convert to Map of pages to List of questionsAndAnswers
    val nodeToQuestionMap = assessment.questionsAndAnswers // TODO deal with flattened nested questions
      .groupByTo(LinkedHashMap()) { qa ->
        getConfig(pathway, assessmentType, version).pages
          .firstOrNull { configPage ->
            if (configPage.questions != null) {
              qa.question in configPage.questions.map { q -> q.id }
            } else {
              false
            }
          } ?: throw ServerWebInputException("Error validating questions and answers - cannot find a node with question [${qa.question}]")
      }
      .mapValues { entry ->
        entry.value.map {
          ResettlementAssessmentQuestionAndAnswer(getQuestionList(pathway, assessmentType).firstOrNull { q -> q.id == it.question } ?: throw ServerWebInputException("Error validating questions and answers - cannot find a question with id [${it.question}]"), it.answer)
        }
      }

    // Ensure the correct number of questions are answered in each page
    nodeToQuestionMap.forEach { (key, value) ->
      val expectedQuestions = key.questions
      val actualQuestions = value.map { it.question }
      if (expectedQuestions != actualQuestions) {
        throw ServerWebInputException("Error validating questions and answers - wrong questions answered on page [${key.id}]. Expected [${expectedQuestions?.map { it.id }}] but found [${actualQuestions.map { it.id }}]")
      }
    }

    // Go through the expected page flow and check that actual pages match up
    var currentNode: AssessmentConfigPage? = null
    var pageNumber = 0

    while (true) {
      try {
        val actualPage: AssessmentConfigPage? = nodeToQuestionMap.keys.elementAtOrNull(pageNumber)
        val expectedPage: String = if (currentNode != null) chooseNextPage(currentNode, assessment.questionsAndAnswers, edit, assessmentType) else getConfig(pathway, assessmentType, version).pages[0].id
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
        existingAssessment = existingAssessment.copy(assessment = ResettlementAssessmentQuestionAndAnswerList(questions.toList()))
      }
    }

    // Get the current page
    val config = getConfig(pathway, assessmentType, version)
    val page = config.pages.first { it.id == pageId }

    // Convert to ResettlementAssessmentPage DTO
    var resettlementAssessmentResponsePage = ResettlementAssessmentResponsePage(
      id = page.id,
      questionsAndAnswers = page.questions?.map {
        ResettlementAssessmentResponseQuestionAndAnswer(
          ResettlementAssessmentQuestion(
            it.id,
            it.title,
            it.subTitle,
            it.type,
            it.options,
            it.validationType,
          ),
          answer = null,
          originalPageId = page.id,
        )
      } ?: listOf(),
      title = page.title,
    )

    // If there is an existing assessment, add the answer into the question
    // Do not populate the case notes
    if (existingAssessment != null) {
      if (resettlementAssessmentResponsePage.id == "CHECK_ANSWERS") {
        // If the existing assessment is submitted we are in an edit and don't want to send back the ASSESSMENT_SUMMARY or PRERELEASE_ASSESSMENT_SUMMARY questions
        val questionsToExclude = if (edit) {
          config.pages.filter { it.questions != null && it.id == "ASSESSMENT_SUMMARY" || it.id == "PRERELEASE_ASSESSMENT_SUMMARY" }.flatMap { it.questions!! }
        } else {
          listOf()
        }
        val questionsAndAnswers: List<ResettlementAssessmentResponseQuestionAndAnswer> =
          existingAssessment.assessment.assessment.map {
            ResettlementAssessmentResponseQuestionAndAnswer(
              question = getQuestionList(pathway, assessmentType).first { q -> q.id == it.questionId },
              answer = it.answer,
              originalPageId = findPageIdFromQuestionId(it.questionId, assessmentType, pathway),
            )
          }.filter { it.question !in questionsToExclude }
        resettlementAssessmentResponsePage = ResettlementAssessmentResponsePage(resettlementAssessmentResponsePage.id, questionsAndAnswers = questionsAndAnswers)
      }

      resettlementAssessmentResponsePage.questionsAndAnswers.forEach { q ->
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

    return resettlementAssessmentResponsePage
  }

  fun getQuestionById(id: String, pathway: Pathway, assessmentType: ResettlementAssessmentType): ResettlementAssessmentQuestion {
    return getQuestionList(pathway, assessmentType).first { it.id == id }
  }

  fun findPageIdFromQuestionId(
    questionId: String,
    assessmentType: ResettlementAssessmentType,
    pathway: Pathway,
    version: Int = 1,
  ): String {
    // TODO deal with flattened nested questions
    return getConfig(pathway, assessmentType, version).pages.firstOrNull { p -> (p.questions?.any { q -> q.id == questionId } == true) }?.id ?: throw RuntimeException("Cannot find page for question [$questionId] - check that the question is used in a page!")
  }

  // TODO deal with flattened nested questions
  private fun getQuestionList(pathway: Pathway, assessmentType: ResettlementAssessmentType, version: Int = 1): List<ResettlementAssessmentQuestion> = getConfig(pathway, assessmentType, version).pages.filter { it.questions != null }.flatMap { it.questions!! }

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

    // Obtain COMPLETE and SUBMITTED resettlement status entity from database
    val resettlementAssessmentStatusEntities = listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED)

    return resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
      prisonerEntity,
      pathway,
      assessmentType,
      resettlementAssessmentStatusEntities,
    )
  }

  private fun loadPathwayStatusAnswer(pathway: Pathway, nomsId: String): StringAnswer? {
    val prisonerEntity = loadPrisoner(nomsId)
    val pathwayStatus = pathwayStatusRepository.findByPathwayAndPrisoner(pathway, prisonerEntity) ?: return null

    return StringAnswer(pathwayStatus.status.name)
  }
}

data class AssessmentQuestionSets(
  val questionSets: List<AssessmentQuestionSet>,
)

data class AssessmentQuestionSet(
  val version: Int,
  val pathway: Pathway?,
  val pages: List<AssessmentConfigPage>,
)

data class AssessmentConfigPage(
  val id: String,
  val title: String?,
  val questions: List<ResettlementAssessmentQuestion>?,
  val nextPageLogic: List<AssessmentConfigNextPageOption>?,
)

data class AssessmentConfigNextPageOption(val questionId: String?, val nextPageId: String, val answers: List<Answer<*>>?)
