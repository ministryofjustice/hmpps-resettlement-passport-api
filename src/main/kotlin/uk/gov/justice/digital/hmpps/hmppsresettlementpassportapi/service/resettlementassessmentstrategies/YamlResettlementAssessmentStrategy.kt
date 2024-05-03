package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDateTime

class YamlResettlementAssessmentStrategy(
  private val config: AssessmentConfig,
  private val assessmentDataService: AssessmentDataService,
) : IResettlementAssessmentStrategy {
  override fun appliesTo(pathway: Pathway): Boolean = config.pathway == pathway

  override fun getNextPageId(
    assessment: ResettlementAssessmentRequest,
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    currentPage: String?,
  ): String {
    return if (currentPage == null) {
      val existingAssessment = assessmentDataService.getExistingAssessment(nomsId, pathway, assessmentType)
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
      val nextPage = chooseNextPage(pageConfig, assessment.questionsAndAnswers ?: emptyList())

      return nextPage?.nextPageId ?: throw ServerWebInputException("Unable to get next page $currentPage on pathway $pathway")
    }
  }

  private fun chooseNextPage(
    pageConfig: AssessmentConfigPage,
    questionsAndAnswers: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>,
  ): AssessmentConfigNextPageOption? = pageConfig.nextPage.find { nextPageOptions ->
    // TODO: Handle end of report pages, need to make a decision whether to go to SUPPORT_NEEDS or CHECK_YOUR_ANSWER based on edit or no
    if (nextPageOptions.answer == null) {
      true
    } else {
      questionsAndAnswers.map { it.answer }.containsAll(nextPageOptions.answer)
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
    validateQuestionAndAnswerSet(assessment, edit, assessmentType)

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
        assessment.questionsAndAnswers.first { it.question == GenericResettlementAssessmentQuestion.SUPPORT_NEEDS_PRERELEASE.id }
      } else {
        assessment.questionsAndAnswers.first { it.question == GenericResettlementAssessmentQuestion.SUPPORT_NEEDS.id }
      }

      status = convertFromSupportNeedAnswerToStatus(supportNeedsQuestionAndAnswer.answer)

      // Get caseNoteText out of CASE_NOTE_SUMMARY question as String
      val caseNoteQuestionAndAnswer =
        assessment.questionsAndAnswers.first { it.question == GenericResettlementAssessmentQuestion.CASE_NOTE_SUMMARY.id }

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
    )

    assessmentDataService.saveAssessment(resettlementAssessmentEntity)
  }

  fun validateQuestionAndAnswerSet(
    assessment: ResettlementAssessmentCompleteRequest,
    edit: Boolean,
    assessmentType: ResettlementAssessmentType = ResettlementAssessmentType.BCST2,
  ) {
    // Convert to Map of pages to List of questionsAndAnswers
    val nodeToQuestionMap = assessment.questionsAndAnswers
      .groupByTo(LinkedHashMap()) { qa ->
        config.pages
          .firstOrNull { configPage ->
            configPage.questions.any { qa.question == it }
          } ?: throw ServerWebInputException("Error validating questions and answers - cannot find a node with question [${qa.question}]")
      }
      .mapValues { entry ->
        entry.value.map {
          ResettlementAssessmentQuestionAndAnswer(getQuestionList().firstOrNull { q -> q.id == it.question } ?: throw ServerWebInputException("Error validating questions and answers - cannot find a question with id [${it.question}]"), it.answer)
        }
      }

    // Ensure the correct number of questions are answered in each page
    nodeToQuestionMap.forEach { (key, value) ->
      val expectedQuestions = key.questions.toSet()
      val actualQuestions = value.map { it.question }.toSet()
      if (expectedQuestions != actualQuestions) {
        throw ServerWebInputException("Error validating questions and answers - wrong questions answered on page [${key.id}]. Expected [$expectedQuestions] but found [$actualQuestions]")
      }
    }

    // Go through the expected page flow and check that actual pages match up
    var currentNode: AssessmentConfigPage = config.pages[0]
    var pageNumber = 0

    while (true) {
      try {
        val actualPage: AssessmentConfigPage? = nodeToQuestionMap.keys.elementAtOrNull(pageNumber)
        val expectedPage: String? = chooseNextPage(currentNode, assessment.questionsAndAnswers)?.nextPageId
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
    TODO("Not yet implemented")
  }

  override fun getQuestionById(id: String): IResettlementAssessmentQuestion {
    TODO("Not yet implemented")
  }

  override fun findPageIdFromQuestionId(questionId: String, assessmentType: ResettlementAssessmentType): String {
    TODO("Not yet implemented")
  }

  private fun getQuestionList(): List<IResettlementAssessmentQuestion> = config.questions + GenericResettlementAssessmentQuestion.entries
}

data class AssessmentConfig(
  val version: Int,
  val pathway: Pathway,
  val pages: List<AssessmentConfigPage>,
  val questions: List<AssessmentConfigQuestion>,
)

data class AssessmentConfigPage(
  val id: String,
  val questions: List<String>,
  val nextPage: List<AssessmentConfigNextPageOption>,
)

data class AssessmentConfigNextPageOption(val questionId: String, val nextPageId: String, val answer: List<Answer<*>>?)

data class AssessmentConfigQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String?,
  override val type: TypeOfQuestion,
  override val options: List<Option>?,
) : IResettlementAssessmentQuestion {
  override val validationType: ValidationType = ValidationType.MANDATORY
}
