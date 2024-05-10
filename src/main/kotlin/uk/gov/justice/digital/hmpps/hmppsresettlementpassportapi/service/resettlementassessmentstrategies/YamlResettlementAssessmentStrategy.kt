package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDateTime

@Service
class YamlResettlementAssessmentStrategy(
  private val config: AssessmentQuestionSets,
  private val assessmentDataService: AssessmentDataService,
  @Value("\${resettlement-assessment.useYaml}") private val useYaml: Boolean,
) : IResettlementAssessmentStrategy {
  override fun appliesTo(pathway: Pathway): Boolean = useYaml && pathway == Pathway.ACCOMMODATION // For now can only be used for ACCOMMODATION

  fun getConfig(pathway: Pathway, version: Int = 1): AssessmentQuestionSet {
    val pathwayConfig = config.questionSets.first { it.pathway == pathway && it.version == version }
    val genericConfig = config.questionSets.first { it.generic && it.version == pathwayConfig.genericAssessmentVersion }
    return pathwayConfig + genericConfig
  }

  operator fun AssessmentQuestionSet.plus(secondaryQuestionSet: AssessmentQuestionSet) =
    AssessmentQuestionSet(
      version = this.version,
      genericAssessmentVersion = this.genericAssessmentVersion,
      pathway = this.pathway,
      pages = this.pages + secondaryQuestionSet.pages,
      questions = this.questions + secondaryQuestionSet.questions,
    )

  override fun getNextPageId(
    assessment: ResettlementAssessmentRequest,
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    currentPage: String?,
  ): String {
    // Validate the request
    if (currentPage != null && assessment.questionsAndAnswers == null) {
      throw ServerWebInputException("If current page is defined, questions must also be defined.")
    }

    val existingAssessment = assessmentDataService.getExistingAssessment(nomsId, pathway, assessmentType)
    val edit = existingAssessment?.assessmentStatus == ResettlementAssessmentStatus.SUBMITTED

    val config = getConfig(pathway)
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

  override fun completeAssessment(
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
    val edit = assessmentDataService.getExistingAssessment(
      nomsId,
      pathway,
      assessmentType,
    )?.assessmentStatus == ResettlementAssessmentStatus.SUBMITTED

    // Check that question and answer set is valid
    validateQuestionAndAnswerSet(pathway, assessment, edit, assessmentType)

    // Obtain prisoner from database, if exists
    val prisonerEntity = assessmentDataService.loadPrisoner(nomsId)
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
      }.toMutableList(),
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
    )

    assessmentDataService.saveAssessment(resettlementAssessmentEntity)
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
        getConfig(pathway).pages
          .firstOrNull { configPage ->
            configPage.questions?.any { qa.question == it } == true
          } ?: throw ServerWebInputException("Error validating questions and answers - cannot find a node with question [${qa.question}]")
      }
      .mapValues { entry ->
        entry.value.map {
          ResettlementAssessmentQuestionAndAnswer(getQuestionList(pathway).firstOrNull { q -> q.id == it.question } ?: throw ServerWebInputException("Error validating questions and answers - cannot find a question with id [${it.question}]"), it.answer)
        }
      }

    // Ensure the correct number of questions are answered in each page
    nodeToQuestionMap.forEach { (key, value) ->
      val expectedQuestions = key.questions?.toSet()
      val actualQuestions = value.map { it.question.id }.toSet()
      if (expectedQuestions != actualQuestions) {
        throw ServerWebInputException("Error validating questions and answers - wrong questions answered on page [${key.id}]. Expected [$expectedQuestions] but found [$actualQuestions]")
      }
    }

    // Go through the expected page flow and check that actual pages match up
    var currentNode: AssessmentConfigPage? = null
    var pageNumber = 0

    while (true) {
      try {
        val actualPage: AssessmentConfigPage? = nodeToQuestionMap.keys.elementAtOrNull(pageNumber)
        val expectedPage: String = if (currentNode != null) chooseNextPage(currentNode, assessment.questionsAndAnswers, edit, assessmentType) else getConfig(pathway).pages[0].id
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

  override fun getPageFromId(
    nomsId: String,
    pathway: Pathway,
    pageId: String,
    assessmentType: ResettlementAssessmentType,
  ): ResettlementAssessmentResponsePage {
    // Get the latest complete assessment (if exists)
    var existingAssessment = assessmentDataService.getExistingAssessment(nomsId, pathway, assessmentType)

    val edit = existingAssessment?.assessmentStatus == ResettlementAssessmentStatus.SUBMITTED

    // If this is a RESETTLEMENT_PLAN (BCST3) type and there is not existing assessment we should use an existing BCST2 if available.
    if (existingAssessment == null && assessmentType == ResettlementAssessmentType.RESETTLEMENT_PLAN) {
      existingAssessment = assessmentDataService.getExistingAssessment(nomsId, pathway, ResettlementAssessmentType.BCST2)

      if (existingAssessment != null) {
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
    val config = getConfig(pathway)
    val page = config.pages.first { it.id == pageId }

    // Convert to ResettlementAssessmentPage DTO
    var resettlementAssessmentResponsePage = ResettlementAssessmentResponsePage(
      id = page.id,
      questionsAndAnswers = page.questions?.map {
        val question = config.questions.first { q -> q.id == it }
        ResettlementAssessmentResponseQuestionAndAnswer(
          ResettlementAssessmentResponseQuestion(
            question.id,
            question.title,
            question.subTitle,
            question.type,
            question.options?.toMutableList(),
            question.validationType,
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
      if (resettlementAssessmentResponsePage.id == GenericAssessmentPage.CHECK_ANSWERS.id) {
        // If the existing assessment is submitted we are in an edit and don't want to send back the ASSESSMENT_SUMMARY or PRERELEASE_ASSESSMENT_SUMMARY questions
        val questionsToExclude = if (edit) {
          config.pages.filter { it.questions != null && it.id == "ASSESSMENT_SUMMARY" || it.id == "PRERELEASE_ASSESSMENT_SUMMARY" }.flatMap { it.questions!! }
        } else {
          listOf()
        }
        val questionsAndAnswers: List<ResettlementAssessmentResponseQuestionAndAnswer> =
          existingAssessment.assessment.assessment.map {
            ResettlementAssessmentResponseQuestionAndAnswer(
              question = getQuestionList(pathway).first { q -> q.id == it.questionId },
              answer = it.answer,
              originalPageId = findPageIdFromQuestionId(it.questionId, assessmentType, pathway),
            )
          }.filter { it.question.id !in questionsToExclude }
        resettlementAssessmentResponsePage = ResettlementAssessmentResponsePage(resettlementAssessmentResponsePage.id, questionsAndAnswers = questionsAndAnswers)
      }

      resettlementAssessmentResponsePage.questionsAndAnswers.forEach { q ->
        val existingAnswer = existingAssessment.assessment.assessment.find { it.questionId == q.question.id }
        if (existingAnswer != null && q.question.id != "CASE_NOTE_SUMMARY") {
          q.answer = existingAnswer.answer
        }
        if (q.question.id == "SUPPORT_NEEDS_PRERELEASE") {
          q.answer = assessmentDataService.loadPathwayStatusAnswer(pathway, nomsId) ?: existingAnswer?.answer
        }
      }
    }

    return resettlementAssessmentResponsePage
  }

  override fun getQuestionById(id: String, pathway: Pathway): IResettlementAssessmentQuestion {
    return getConfig(pathway).questions.first { it.id == id }
  }

  override fun findPageIdFromQuestionId(questionId: String, assessmentType: ResettlementAssessmentType, pathway: Pathway): String {
    val pageList = getConfig(pathway).pages
    return pageList.firstOrNull { p -> (p.questions?.any { q -> q == questionId } == true) }?.id ?: throw RuntimeException("Cannot find page for question [$questionId] - check that the question is used in a page!")
  }

  private fun getQuestionList(pathway: Pathway): List<IResettlementAssessmentQuestion> = getConfig(pathway).questions
}

data class AssessmentQuestionSets(
  val questionSets: List<AssessmentQuestionSet>,
)

data class AssessmentQuestionSet(
  val version: Int,
  val generic: Boolean = false,
  val pathway: Pathway?,
  val genericAssessmentVersion: Int?,
  val pages: List<AssessmentConfigPage>,
  val questions: List<AssessmentConfigQuestion>,
)

data class AssessmentConfigPage(
  val id: String,
  val title: String?,
  val questions: List<String>?,
  val nextPageLogic: List<AssessmentConfigNextPageOption>?,
)

data class AssessmentConfigNextPageOption(val questionId: String?, val nextPageId: String, val answers: List<Answer<*>>?)

data class AssessmentConfigQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String?,
  override val type: TypeOfQuestion,
  override val options: List<Option>?,
) : IResettlementAssessmentQuestion {
  override val validationType: ValidationType = ValidationType.MANDATORY
}
