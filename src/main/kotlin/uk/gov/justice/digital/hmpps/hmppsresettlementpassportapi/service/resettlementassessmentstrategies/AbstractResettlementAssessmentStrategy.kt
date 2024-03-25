package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.transaction.Transactional
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.convertEnumStringToEnum
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDateTime
import kotlin.reflect.KClass

abstract class AbstractResettlementAssessmentStrategy<T, Q>(
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val statusRepository: StatusRepository,
  private val pathwayRepository: PathwayRepository,
  private val resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
  private val assessmentPageClass: KClass<T>,
  private val questionClass: KClass<Q>,
) :
  IResettlementAssessmentStrategy<Q> where T : Enum<*>, T : IAssessmentPage, Q : Enum<*>, Q : IResettlementAssessmentQuestion {

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

    // Get the current page
    val currentPageEnum = if (currentPage != null) {
      convertEnumStringToEnum(
        enumClass = assessmentPageClass,
        secondaryEnumClass = GenericAssessmentPage::class,
        stringValue = currentPage,
      )
    } else {
      null
    }

    // Get the next page
    val nextPage: IAssessmentPage

    val existingAssessment = getExistingAssessment(nomsId, pathway, assessmentType)

    if (currentPageEnum == null) {
      // Option 1 - If the current page is null then send back the first page unless there is already an assessment completed, in which case go straight to CHECK_ANSWERS
      nextPage = if (existingAssessment == null) {
        if (assessmentPageClass.java.enumConstants.isNotEmpty()) {
          assessmentPageClass.java.enumConstants[0] as IAssessmentPage
        } else {
          GenericAssessmentPage.ASSESSMENT_SUMMARY
        }
      } else {
        GenericAssessmentPage.CHECK_ANSWERS
      }
    } else if (currentPageEnum.id == "CHECK_ANSWERS") {
      // Option 2 - If the current page is CHECK_ANSWERS then something has gone wrong as this is the end of the flow, and we should be called the submit endpoint instead
      throw ServerWebInputException("Cannot get the next question from CHECK_ANSWERS as this is the end of the flow for this pathway.")
    } else {
      // Option 3 - If none of the above use the next page function in the question lambda to calculate the next page
      val questionLambda = getPageList().first { it.assessmentPage == currentPageEnum }
      val questions: List<ResettlementAssessmentQuestionAndAnswer> = assessment.questionsAndAnswers!!.map {
        ResettlementAssessmentQuestionAndAnswer(
          convertEnumStringToEnum(enumClass = questionClass, secondaryEnumClass = GenericResettlementAssessmentQuestion::class, stringValue = it.question),
          it.answer as Answer<*>,
        )
      }
      // If the existing assessment is SUBMITTED this is an edit
      val edit = existingAssessment?.assessmentStatus?.id == ResettlementAssessmentStatus.SUBMITTED.id
      nextPage = questionLambda.nextPage(questions, edit)
    }

    return nextPage.id
  }

  private fun getExistingAssessment(
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
  ): ResettlementAssessmentEntity? {
    // Obtain prisoner from database, if exists
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    // Obtain pathway entity from database
    val pathwayEntity = pathwayRepository.findById(pathway.id).get()

    // Obtain COMPLETE and SUBMITTED resettlement status entity from database
    val resettlementAssessmentStatusEntities = resettlementAssessmentStatusRepository.findAll().filter { it.id in listOf(ResettlementAssessmentStatus.COMPLETE.id, ResettlementAssessmentStatus.SUBMITTED.id) }

    return resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
      prisonerEntity,
      pathwayEntity,
      assessmentType,
      resettlementAssessmentStatusEntities,
    )
  }

  override fun getPageFromId(
    nomsId: String,
    pathway: Pathway,
    pageId: String,
    assessmentType: ResettlementAssessmentType,
  ): ResettlementAssessmentResponsePage {
    // Get the latest complete assessment (if exists)
    var existingAssessment = getExistingAssessment(nomsId, pathway, assessmentType)

    val edit = existingAssessment?.assessmentStatus?.id == ResettlementAssessmentStatus.SUBMITTED.id

    // If this is a RESETTLEMENT_PLAN (BCST3) type and there is not existing assessment we should use an existing BCST2 if available.
    if (existingAssessment == null && assessmentType == ResettlementAssessmentType.RESETTLEMENT_PLAN) {
      existingAssessment = getExistingAssessment(nomsId, pathway, ResettlementAssessmentType.BCST2)
    }

    // Get the current page
    val page = convertEnumStringToEnum(enumClass = assessmentPageClass, secondaryEnumClass = GenericAssessmentPage::class, stringValue = pageId)

    // Convert to ResettlementAssessmentPage DTO
    var resettlementAssessmentResponsePage = ResettlementAssessmentResponsePage(
      id = page.id,
      questionsAndAnswers = page.questionsAndAnswers.map {
        ResettlementAssessmentResponseQuestionAndAnswer(
          ResettlementAssessmentResponseQuestion(
            it.question.id,
            it.question.title,
            it.question.subTitle,
            it.question.type,
            it.question.options?.toMutableList(),
            it.question.validationType,
          ),
          it.answer,
          page.id,
        )
      },
      title = page.title,
    )

    // If there is an existing assessment, add the answer into the question
    // Do not populate the case notes
    if (existingAssessment != null) {
      if (resettlementAssessmentResponsePage.id == GenericAssessmentPage.CHECK_ANSWERS.id) {
        // If the existing assessment is submitted we are in an edit and don't want to send back the ASSESSMENT_SUMMARY questions
        val questionsToExclude = if (edit) {
          GenericAssessmentPage.ASSESSMENT_SUMMARY.questionsAndAnswers.map { it.question }
        } else {
          listOf()
        }
        val questionsAndAnswers: List<ResettlementAssessmentResponseQuestionAndAnswer> =
          existingAssessment.assessment.assessment.map { ResettlementAssessmentResponseQuestionAndAnswer(question = getQuestionList().first { q -> q.id == it.questionId }, answer = it.answer, originalPageId = findPageIdFromQuestionId(it.questionId)) }.filter { it.question !in questionsToExclude }
        resettlementAssessmentResponsePage = ResettlementAssessmentResponsePage(resettlementAssessmentResponsePage.id, questionsAndAnswers = questionsAndAnswers)
      }
      resettlementAssessmentResponsePage.questionsAndAnswers.forEach { q ->
        val existingAnswer = existingAssessment.assessment.assessment.find { it.questionId == q.question.id }
        if (existingAnswer != null && q.question != GenericResettlementAssessmentQuestion.CASE_NOTE_SUMMARY) {
          q.answer = existingAnswer.answer
        }
      }
    }

    return resettlementAssessmentResponsePage
  }

  override fun findPageIdFromQuestionId(questionId: String): String {
    val pageList = getPageList().map { it.assessmentPage } + GenericAssessmentPage.entries
    return pageList.firstOrNull { p -> (p.questionsAndAnswers.any { q -> q.question.id == questionId }) }?.id ?: throw RuntimeException("Cannot find page for question [$questionId] - check that the question is used in a page!")
  }

  abstract fun getPageList(): List<ResettlementAssessmentNode>

  fun getQuestionList(): List<IResettlementAssessmentQuestion> = questionClass.java.enumConstants.asList() + GenericResettlementAssessmentQuestion.entries

  @Transactional
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
    val edit = getExistingAssessment(nomsId, pathway, assessmentType)?.assessmentStatus?.id == ResettlementAssessmentStatus.SUBMITTED.id

    // Check that question and answer set is valid
    validateQuestionAndAnswerSet(assessment, edit)

    // Obtain prisoner from database, if exists
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    // Obtain pathway entity from database
    val pathwayEntity = pathwayRepository.findById(pathway.id).get()

    // If it's not an edit then use COMPLETE status, else use SUBMITTED
    val resettlementAssessmentStatusEntity = if (!edit) {
      resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.COMPLETE.id).get()
    } else {
      resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.SUBMITTED.id).get()
    }

    // If it's not an edit, get the status and the case note out of the questions and answers
    var statusEntity: StatusEntity? = null
    var caseNoteText: String? = null

    if (!edit) {
      // Get statusChangedTo out of SUPPORT_NEEDS question and convert to a Status
      val supportNeedsQuestionAndAnswer =
        assessment.questionsAndAnswers.first { it.question == GenericResettlementAssessmentQuestion.SUPPORT_NEEDS.id }

      val statusChangedTo = convertFromSupportNeedAnswerToStatus(supportNeedsQuestionAndAnswer.answer)
      statusEntity = statusRepository.findById(statusChangedTo.id).get()

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
      },
    )

    // Create new resettlement assessment entity and save to database
    val resettlementAssessmentEntity = ResettlementAssessmentEntity(
      id = null,
      prisoner = prisonerEntity,
      pathway = pathwayEntity,
      statusChangedTo = statusEntity,
      assessmentType = assessmentType,
      assessment = resettlementAssessmentQuestionAndAnswerList,
      creationDate = LocalDateTime.now(),
      createdBy = name,
      assessmentStatus = resettlementAssessmentStatusEntity,
      caseNoteText = caseNoteText,
      createdByUserId = userId,
    )

    resettlementAssessmentRepository.save(resettlementAssessmentEntity)
  }

  private fun convertFromSupportNeedAnswerToStatus(supportNeed: Answer<*>?): Status {
    if (supportNeed is StringAnswer) {
      return when (supportNeed.answer) {
        "SUPPORT_REQUIRED" -> Status.SUPPORT_REQUIRED
        "SUPPORT_NOT_REQUIRED" -> Status.SUPPORT_NOT_REQUIRED
        "SUPPORT_DECLINED" -> Status.SUPPORT_DECLINED
        else -> throw ServerWebInputException("Support need [$supportNeed] is not a valid option")
      }
    } else {
      throw ServerWebInputException("Support need [$supportNeed] must be a StringAnswer")
    }
  }

  private fun convertFromStringAnswer(answer: Answer<*>?): String {
    if (answer is StringAnswer) {
      return answer.answer ?: throw ServerWebInputException("Answer [$answer] must not be null")
    } else {
      throw ServerWebInputException("Answer [$answer] must be a StringAnswer")
    }
  }

  override fun getQuestionClass(): KClass<Q> = questionClass

  fun validateQuestionAndAnswerSet(assessment: ResettlementAssessmentCompleteRequest, edit: Boolean) {
    // Convert to Map of pages to List of questionsAndAnswers
    val nodeToQuestionMap = assessment.questionsAndAnswers
      .groupByTo(LinkedHashMap()) { qa ->
        getPageList()
          .firstOrNull { n ->
            qa.question in n.assessmentPage.questionsAndAnswers
              .map { q -> q.question.id }
          } ?: throw ServerWebInputException("Error validating questions and answers - cannot find a node with question [${qa.question}]")
      }
      .mapValues { entry ->
        entry.value.map {
          ResettlementAssessmentQuestionAndAnswer(getQuestionList().firstOrNull { q -> q.id == it.question } ?: throw ServerWebInputException("Error validating questions and answers - cannot find a question with id [${it.question}]"), it.answer)
        }
      }

    // Ensure the correct number of questions are answered in each page
    nodeToQuestionMap.forEach { entry ->
      val expectedQuestions = entry.key.assessmentPage.questionsAndAnswers.map { it.question }
      val actualQuestions = entry.value.map { it.question }
      if (expectedQuestions != actualQuestions) {
        throw ServerWebInputException("Error validating questions and answers - wrong questions answered on page [${entry.key.assessmentPage.id}]. Expected [$expectedQuestions] but found [$actualQuestions]")
      }
    }

    // Go through the expected page flow and check that actual pages match up
    var currentNode: ResettlementAssessmentNode? = null
    var pageNumber = 0

    while (true) {
      try {
        val actualPage: IAssessmentPage? = nodeToQuestionMap.keys.elementAtOrNull(pageNumber)?.assessmentPage
        val expectedPage: IAssessmentPage =
          currentNode?.nextPage?.invoke(nodeToQuestionMap[currentNode]!!, edit) ?: getPageList()[0].assessmentPage
        if (expectedPage == GenericAssessmentPage.CHECK_ANSWERS) {
          break
        }
        if (expectedPage != actualPage) {
          throw ServerWebInputException("Error validating questions and answers - expected page [$expectedPage] is different to actual page [$actualPage] at index [$pageNumber]")
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
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class GenericAssessmentPage(
  override val id: String,
  override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>,
  override val title: String? = null,
) : IAssessmentPage {
  CHECK_ANSWERS(
    id = "CHECK_ANSWERS",
    questionsAndAnswers = listOf(),
  ), // in this case questionsAndAnswers will be dynamically populated later by the UI cache
  ASSESSMENT_SUMMARY(
    id = "ASSESSMENT_SUMMARY",
    questionsAndAnswers = listOf(
      ResettlementAssessmentQuestionAndAnswer(GenericResettlementAssessmentQuestion.SUPPORT_NEEDS),
      ResettlementAssessmentQuestionAndAnswer(GenericResettlementAssessmentQuestion.CASE_NOTE_SUMMARY),
    ),
  ),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class GenericResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
  override val validationType: ValidationType = ValidationType.MANDATORY,
) : IResettlementAssessmentQuestion {
  SUPPORT_NEEDS(
    id = "SUPPORT_NEEDS",
    title = "",
    type = TypeOfQuestion.RADIO,
    options = listOf(
      Option(
        id = "SUPPORT_REQUIRED",
        displayText = "Support required",
        description = "a need for support has been identified and is accepted",
      ),
      Option(id = "SUPPORT_NOT_REQUIRED", displayText = "Support not required", description = "no need was identified"),
      Option(
        id = "SUPPORT_DECLINED",
        displayText = "Support declined",
        description = "a need has been identified but support is declined",
      ),
    ),
  ),
  CASE_NOTE_SUMMARY(
    id = "CASE_NOTE_SUMMARY",
    title = "Add a case note summary",
    subTitle = "This will be displayed as a case note in both DPS and nDelius",
    type = TypeOfQuestion.LONG_TEXT,
  ),
}

fun finalQuestionNextPage(@Suppress("unused") questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, edit: Boolean): IAssessmentPage {
  return if (!edit) {
    GenericAssessmentPage.ASSESSMENT_SUMMARY
  } else {
    GenericAssessmentPage.CHECK_ANSWERS
  }
}

val assessmentSummaryNode = ResettlementAssessmentNode(
  assessmentPage = GenericAssessmentPage.ASSESSMENT_SUMMARY,
  nextPage = fun(_: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
    return GenericAssessmentPage.CHECK_ANSWERS
  },
)
