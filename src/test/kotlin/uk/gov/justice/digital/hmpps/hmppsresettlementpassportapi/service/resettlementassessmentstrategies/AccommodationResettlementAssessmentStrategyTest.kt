package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.councilOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class AccommodationResettlementAssessmentStrategyTest {
  private lateinit var resettlementAssessmentService: AccommodationResettlementAssessmentStrategy

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var pathwayRepository: PathwayRepository

  @Mock
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Mock
  private lateinit var statusRepository: StatusRepository

  @Mock
  private lateinit var resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentService = AccommodationResettlementAssessmentStrategy(
      resettlementAssessmentRepository,
      prisonerRepository,
      statusRepository,
      pathwayRepository,
      resettlementAssessmentStatusRepository,
    )
  }

  @ParameterizedTest
  @MethodSource("test next page function flow - no existing assessment data")
  fun `test next page function flow - no existing assessment`(
    questionsAndAnswers: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>,
    currentPage: String?,
    expectedPage: String,
  ) {
    val nomsId = "123"

    if (currentPage == null) {
      setUpMocks(nomsId, false)
    }

    val assessment = ResettlementAssessmentRequest(
      questionsAndAnswers = questionsAndAnswers,
    )
    val nextPage = resettlementAssessmentService.getNextPageId(
      assessment = assessment,
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      currentPage = currentPage,
    )
    Assertions.assertEquals(expectedPage, nextPage)
  }

  private fun `test next page function flow - no existing assessment data`() = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "WHERE_WILL_THEY_LIVE",
    ),
    // If the answer to WHERE_WILL_THEY_LIVE is NO_PLACE_TO_LIVE, go to CONSENT_FOR_CRS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NO_PLACE_TO_LIVE")),
      ),
      "WHERE_WILL_THEY_LIVE",
      "CONSENT_FOR_CRS",
    ),
    // If the answer to WHERE_WILL_THEY_LIVE is PREVIOUS_ADDRESS, go to WHO_WILL_THEY_LIVE_WITH
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("PREVIOUS_ADDRESS")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_IS_THE_ADDRESS", answer = MapAnswer(listOf(mapOf(Pair("ADDRESS_LINE_1", "12 High Street"), Pair("CITY", "Leeds"), Pair("POSTCODE", "LS1 1AA"))))),
      ),
      "WHERE_WILL_THEY_LIVE",
      "WHO_WILL_THEY_LIVE_WITH",
    ),
    // If the answer to WHERE_WILL_THEY_LIVE is NEW_ADDRESS, go to WHO_WILL_THEY_LIVE_WITH
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_IS_THE_ADDRESS", answer = MapAnswer(listOf(mapOf(Pair("ADDRESS_LINE_1", "12 High Street"), Pair("CITY", "Leeds"), Pair("POSTCODE", "LS1 1AA"))))),
      ),
      "WHERE_WILL_THEY_LIVE",
      "WHO_WILL_THEY_LIVE_WITH",
    ),
    // Any answer to WHO_WILL_THEY_LIVE_WITH, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_IS_THE_ADDRESS", answer = MapAnswer(listOf(mapOf(Pair("ADDRESS_LINE_1", "12 High Street"), Pair("CITY", "Leeds"), Pair("POSTCODE", "LS1 1AA"))))),
        ResettlementAssessmentRequestQuestionAndAnswer("WHO_WILL_THEY_LIVE_WITH", answer = StringAnswer("Something")),
      ),
      "WHO_WILL_THEY_LIVE_WITH",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to CONSENT_FOR_CRS, go to WHAT_COUNCIL_AREA
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NO_PLACE_TO_LIVE")),
        ResettlementAssessmentRequestQuestionAndAnswer("ACCOM_CRS", answer = StringAnswer("Yes")),
      ),
      "CONSENT_FOR_CRS",
      "WHAT_COUNCIL_AREA",
    ),
    // Any answer to WHAT_COUNCIL_AREA, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NO_PLACE_TO_LIVE")),
        ResettlementAssessmentRequestQuestionAndAnswer("ACCOM_CRS", answer = StringAnswer("Yes")),
        ResettlementAssessmentRequestQuestionAndAnswer("COUNCIL_AREA", answer = StringAnswer("Leeds City Council")),
        ResettlementAssessmentRequestQuestionAndAnswer("COUNCIL_AREA_REASON", answer = StringAnswer("Some additional long text")),
      ),
      "WHAT_COUNCIL_AREA",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NO_PLACE_TO_LIVE")),
        ResettlementAssessmentRequestQuestionAndAnswer("ACCOM_CRS", answer = StringAnswer("Yes")),
        ResettlementAssessmentRequestQuestionAndAnswer("COUNCIL_AREA", answer = StringAnswer("Leeds City Council")),
        ResettlementAssessmentRequestQuestionAndAnswer("COUNCIL_AREA_REASON", answer = StringAnswer("Some additional long text")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_NEEDS", answer = StringAnswer("SUPPORT_REQUIRED")),
        ResettlementAssessmentRequestQuestionAndAnswer("CASE_NOTE_SUMMARY", answer = StringAnswer("My case note summary.")),
      ),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  @Test
  fun `test next page function start - existing assessment`() {
    // If there is an existing assessment we should go to CHECK_ANSWERS at the start
    val nomsId = "123456"
    setUpMocks(nomsId, true)

    val assessment = ResettlementAssessmentRequest(
      questionsAndAnswers = null,
    )
    val nextPage = resettlementAssessmentService.getNextPageId(
      assessment = assessment,
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      currentPage = null,
    )
    Assertions.assertEquals("CHECK_ANSWERS", nextPage)
  }

  @Test
  fun `test next page function - error case from CHECK_ANSWERS`() {
    // We should send back an error if CHECK_ANSWERS is the current page as there is no next page to get
    val nomsId = "123456"
    val assessment = ResettlementAssessmentRequest(
      questionsAndAnswers = listOf(ResettlementAssessmentRequestQuestionAndAnswer("ANY_QUESTION", StringAnswer("Any answer"))),
    )

    val exception = assertThrows<ServerWebInputException> {
      resettlementAssessmentService.getNextPageId(
        assessment = assessment,
        nomsId = nomsId,
        pathway = Pathway.ACCOMMODATION,
        assessmentType = ResettlementAssessmentType.BCST2,
        currentPage = "CHECK_ANSWERS",
      )
    }
    Assertions.assertEquals("400 BAD_REQUEST \"Cannot get the next question from CHECK_ANSWERS as this is the end of the flow for this pathway.\"", exception.message)
  }

  @Test
  fun `test next page function - error case current page given with no questions`() {
    // We should send back an error a current page is given but with no questions and answers
    val nomsId = "123456"
    val assessment = ResettlementAssessmentRequest(
      questionsAndAnswers = null,
    )

    val exception = assertThrows<ServerWebInputException> {
      resettlementAssessmentService.getNextPageId(
        assessment = assessment,
        nomsId = nomsId,
        pathway = Pathway.ACCOMMODATION,
        assessmentType = ResettlementAssessmentType.BCST2,
        currentPage = "MY_PAGE",
      )
    }
    Assertions.assertEquals("400 BAD_REQUEST \"If current page is defined, questions must also be defined.\"", exception.message)
  }

  @ParameterizedTest
  @MethodSource("test get page from Id - no existing assessment data")
  fun `test get page from Id - no existing assessment`(pageIdInput: String, expectedPage: ResettlementAssessmentResponsePage) {
    val nomsId = "123"
    setUpMocks("123", false)

    val page = resettlementAssessmentService.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
    )
    Assertions.assertEquals(expectedPage.id, page.id)
    Assertions.assertEquals(expectedPage.questionsAndAnswers, page.questionsAndAnswers)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "WHERE_WILL_THEY_LIVE",
      ResettlementAssessmentResponsePage(
        id = "WHERE_WILL_THEY_LIVE",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            ResettlementAssessmentResponseQuestion(
              id = "WHERE_WILL_THEY_LIVE",
              title = "Where will they live when released from custody?",
              subTitle = null,
              type = TypeOfQuestion.RADIO_WITH_ADDRESS,
              options = mutableListOf(
                Option(id = "PREVIOUS_ADDRESS", displayText = "Returning to a previous address"),
                Option(id = "NEW_ADDRESS", displayText = "Moving to new address"),
                Option(id = "NO_PLACE_TO_LIVE", displayText = "No place to live"),
              ),
            ),
          ),
          ResettlementAssessmentResponseQuestionAndAnswer(ResettlementAssessmentResponseQuestion(id = "WHAT_IS_THE_ADDRESS", title = "", type = TypeOfQuestion.ADDRESS)),
        ),
      ),
    ),
    Arguments.of(
      "WHO_WILL_THEY_LIVE_WITH",
      ResettlementAssessmentResponsePage(
        id = "WHO_WILL_THEY_LIVE_WITH",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(ResettlementAssessmentResponseQuestion(id = "WHO_WILL_THEY_LIVE_WITH", title = "What are the names and ages of all residents at this property and the prisoner's relationship to them?", type = TypeOfQuestion.LIST_OF_PEOPLE)),
        ),
      ),
    ),
    Arguments.of(
      "CONSENT_FOR_CRS",
      ResettlementAssessmentResponsePage(
        id = "CONSENT_FOR_CRS",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(ResettlementAssessmentResponseQuestion(id = "ACCOM_CRS", title = "Do they give consent for a Commissioned Rehabilitative Service (CRS)?", type = TypeOfQuestion.RADIO, options = yesNoOptions.toMutableList())),
        ),
      ),
    ),
    Arguments.of(
      "WHAT_COUNCIL_AREA",
      ResettlementAssessmentResponsePage(
        id = "WHAT_COUNCIL_AREA",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(ResettlementAssessmentResponseQuestion(id = "COUNCIL_AREA", title = "Which council area are they intending to move to on release?", type = TypeOfQuestion.DROPDOWN, options = councilOptions.toMutableList())),
          ResettlementAssessmentResponseQuestionAndAnswer(ResettlementAssessmentResponseQuestion(id = "COUNCIL_AREA_REASON", title = "Why do they intend to move to this council area on release?", type = TypeOfQuestion.LONG_TEXT)),
        ),
      ),
    ),
    Arguments.of(
      "ASSESSMENT_SUMMARY",
      ResettlementAssessmentResponsePage(
        id = "ASSESSMENT_SUMMARY",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            ResettlementAssessmentResponseQuestion(
              id = "SUPPORT_NEEDS",
              title = "",
              type = TypeOfQuestion.RADIO,
              options = mutableListOf(
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
          ),
          ResettlementAssessmentResponseQuestionAndAnswer(
            ResettlementAssessmentResponseQuestion(
              id = "CASE_NOTE_SUMMARY",
              title = "Add a case note summary",
              subTitle = "This will be displayed as a case note in both DPS and nDelius",
              type = TypeOfQuestion.LONG_TEXT,
            ),
          ),
        ),
      ),
    ),
    Arguments.of(
      "CHECK_ANSWERS",
      ResettlementAssessmentResponsePage(
        id = "CHECK_ANSWERS",
        questionsAndAnswers = mutableListOf(),
      ),
    ),
  )

  @Test
  fun `test get page from Id - existing assessment`() {
    val nomsId = "123"

    val existingAssessment = ResettlementAssessmentQuestionAndAnswerList(
      listOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE", StringAnswer("NO_PLACE_TO_LIVE")),
      ),
    )

    setUpMocks("123", true, existingAssessment)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "WHERE_WILL_THEY_LIVE",
      questionsAndAnswers = mutableListOf(
        ResettlementAssessmentResponseQuestionAndAnswer(
          ResettlementAssessmentResponseQuestion(
            id = "WHERE_WILL_THEY_LIVE",
            title = "Where will they live when released from custody?",
            subTitle = null,
            type = TypeOfQuestion.RADIO_WITH_ADDRESS,
            options = mutableListOf(
              Option(id = "PREVIOUS_ADDRESS", displayText = "Returning to a previous address"),
              Option(id = "NEW_ADDRESS", displayText = "Moving to new address"),
              Option(id = "NO_PLACE_TO_LIVE", displayText = "No place to live"),
            ),
          ),
          answer = StringAnswer("NO_PLACE_TO_LIVE"),
        ),
        ResettlementAssessmentResponseQuestionAndAnswer(ResettlementAssessmentResponseQuestion(id = "WHAT_IS_THE_ADDRESS", title = "", type = TypeOfQuestion.ADDRESS)),
      ),
    )

    val page = resettlementAssessmentService.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = "WHERE_WILL_THEY_LIVE",
    )
    Assertions.assertEquals(expectedPage, page)
  }

  @ParameterizedTest
  @MethodSource("test complete assessment data")
  fun `test complete assessment`(assessment: ResettlementAssessmentCompleteRequest, expectedEntity: ResettlementAssessmentEntity?, expectedException: Throwable?) {
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken("string", "name") } returns "System user"
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns testDate

    val nomsId = "abc"
    val pathway = Pathway.ACCOMMODATION
    val assessmentType = ResettlementAssessmentType.BCST2

    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val pathwayEntity = PathwayEntity(1, "Accommodation", true, testDate)
    val resettlementAssessmentStatusEntity = ResettlementAssessmentStatusEntity(3, "Complete", true, testDate)
    val statusEntity = StatusEntity(1, "Not started", true, testDate)

    Mockito.`when`(pathwayRepository.findById(Pathway.ACCOMMODATION.id)).thenReturn(Optional.of(pathwayEntity))
    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.COMPLETE.id))
      .thenReturn(Optional.of(resettlementAssessmentStatusEntity))
    Mockito.lenient().`when`(statusRepository.findById(Status.NOT_STARTED.id)).thenReturn(Optional.of(statusEntity))

    if (expectedException == null) {
      resettlementAssessmentService.completeAssessment(nomsId, pathway, assessmentType, assessment, "string")
      Mockito.verify(resettlementAssessmentRepository).save(expectedEntity!!)
    } else {
      val actualException = assertThrows<Throwable> {
        resettlementAssessmentService.completeAssessment(nomsId, pathway, assessmentType, assessment, "string")
      }
      Assertions.assertEquals(expectedException::class, actualException::class)
      Assertions.assertEquals(expectedException.message, actualException.message)
    }

    unmockkAll()
  }

  private fun `test complete assessment data`() = Stream.of(
    // Throw exception if SUPPORT_NEEDS question not answered
    Arguments.of(
      ResettlementAssessmentCompleteRequest(questionsAndAnswers = listOf()),
      null,
      ServerWebInputException("Answer to question SUPPORT_NEEDS must be provided."),
    ),
    // Throw exception if SUPPORT_NEEDS answer in wrong format
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = ListAnswer(listOf("SUPPORT_REQUIRED", "SUPPORT_DECLINED")),
          ),
        ),
      ),
      null,
      ServerWebInputException("Support need [ListAnswer(answer=[SUPPORT_REQUIRED, SUPPORT_DECLINED])] must be a StringAnswer"),
    ),
    // Throw exception if SUPPORT_NEEDS answer is null
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer(null),
          ),
        ),
      ),
      null,
      ServerWebInputException("Support need [StringAnswer(answer=null)] is not a valid option"),
    ),
    // Throw exception if SUPPORT_NEEDS answer is not a valid option
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_WANTED"),
          ),
        ),
      ),
      null,
      ServerWebInputException("Support need [StringAnswer(answer=SUPPORT_WANTED)] is not a valid option"),
    ),
    // Throw exception if CASE_NOTE_SUMMARY question not answered
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
        ),
      ),
      null,
      ServerWebInputException("Answer to question CASE_NOTE_SUMMARY must be provided."),
    ),
    // Throw exception if CASE_NOTE_SUMMARY answer in wrong format
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "CASE_NOTE_SUMMARY",
            answer = ListAnswer(listOf("hello", "world")),
          ),
        ),
      ),
      null,
      ServerWebInputException("Answer [ListAnswer(answer=[hello, world])] must be a StringAnswer"),
    ),
    // Throw exception if CASE_NOTE_SUMMARY is null
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "CASE_NOTE_SUMMARY",
            answer = StringAnswer(null),
          ),
        ),
      ),
      null,
      ServerWebInputException("Answer [StringAnswer(answer=null)] must not be null"),
    ),
    // Happy path
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "QUESTION_1",
            answer = ListAnswer(listOf("Part 1", "Part 2", "Part 3")),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "QUESTION_2",
            answer = MapAnswer(answer = listOf(mapOf("Key 1" to "Value 1", "Key 2" to "Value 2"), mapOf("Something" to "Something else"))),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "CASE_NOTE_SUMMARY",
            answer = StringAnswer("My case note summary..."),
          ),
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisoner = PrisonerEntity(id = 1, nomsId = "abc", creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), crn = "abc", prisonId = "ABC", releaseDate = LocalDate.parse("2025-01-23")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), statusChangedTo = StatusEntity(id = 1, name = "Not started", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "QUESTION_1", answer = ListAnswer(answer = listOf("Part 1", "Part 2", "Part 3"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "QUESTION_2", answer = MapAnswer(answer = listOf(mapOf("Key 1" to "Value 1", "Key 2" to "Value 2"), mapOf("Something" to "Something else")))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), caseNoteText = "My case note summary..."),
      null,
    ),
  )

  private fun setUpMocks(nomsId: String, returnResettlementAssessmentEntity: Boolean, assessment: ResettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentQuestionAndAnswerList(listOf())) {
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val pathwayEntity = PathwayEntity(1, "Accommodation", true, testDate)
    val resettlementAssessmentStatusEntities = listOf(ResettlementAssessmentStatusEntity(3, "Complete", true, testDate), ResettlementAssessmentStatusEntity(4, "Submitted", true, testDate))
    val resettlementAssessmentEntity = if (returnResettlementAssessmentEntity) ResettlementAssessmentEntity(1, prisonerEntity, pathwayEntity, null, ResettlementAssessmentType.BCST2, assessment, testDate, "", resettlementAssessmentStatusEntities[0]) else null
    Mockito.`when`(pathwayRepository.findById(Pathway.ACCOMMODATION.id)).thenReturn(Optional.of(pathwayEntity))
    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(resettlementAssessmentStatusRepository.findAll())
      .thenReturn(resettlementAssessmentStatusEntities)
    Mockito.`when`(
      resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        prisonerEntity,
        pathwayEntity,
        ResettlementAssessmentType.BCST2,
        resettlementAssessmentStatusEntities,
      ),
    ).thenReturn(resettlementAssessmentEntity)
  }
}
