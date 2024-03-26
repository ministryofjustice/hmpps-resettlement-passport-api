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
import java.util.Optional
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
    setUpMocks(nomsId, false)

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
      "WHERE_DID_THEY_LIVE",
    ),
    // If the answer to WHERE_DID_THEY_LIVE is PRIVATE_RENTED_HOUSING, go to WHERE_DID_THEY_LIVE_ADDRESS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("PRIVATE_RENTED_HOUSING")),
      ),
      "WHERE_DID_THEY_LIVE",
      "WHERE_DID_THEY_LIVE_ADDRESS",
    ),
    // If the answer to WHERE_DID_THEY_LIVE is SOCIAL_HOUSING, go to WHERE_DID_THEY_LIVE_ADDRESS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("SOCIAL_HOUSING")),
      ),
      "WHERE_DID_THEY_LIVE",
      "WHERE_DID_THEY_LIVE_ADDRESS",
    ),
    // If the answer to WHERE_DID_THEY_LIVE is HOMEOWNER, go to WHERE_DID_THEY_LIVE_ADDRESS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("HOMEOWNER")),
      ),
      "WHERE_DID_THEY_LIVE",
      "WHERE_DID_THEY_LIVE_ADDRESS",
    ),
    // If the answer to WHERE_DID_THEY_LIVE is NO_PERMANENT_OR_FIXED, go to WHERE_WILL_THEY_LIVE_2
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("NO_PERMANENT_OR_FIXED")),
      ),
      "WHERE_DID_THEY_LIVE",
      "WHERE_WILL_THEY_LIVE_2",
    ),
    // If the answer to WHERE_DID_THEY_LIVE is NO_ANSWER, go to WHERE_WILL_THEY_LIVE_2
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("NO_ANSWER")),
      ),
      "WHERE_DID_THEY_LIVE",
      "WHERE_WILL_THEY_LIVE_2",
    ),
    // Any answer to WHERE_DID_THEY_LIVE_ADDRESS, go to HELP_TO_KEEP_HOME
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("HOMEOWNER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", answer = MapAnswer(listOf(mapOf("ADDRESS_LINE_1" to "12 High Street", "CITY" to "Leeds", "POSTCODE" to "LS1 1AA")))),
      ),
      "WHERE_DID_THEY_LIVE_ADDRESS",
      "HELP_TO_KEEP_HOME",
    ),
    // Any answer to HELP_TO_KEEP_HOME, go to WHERE_WILL_THEY_LIVE_1
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("HOMEOWNER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", answer = MapAnswer(listOf(mapOf("ADDRESS_LINE_1" to "12 High Street", "CITY" to "Leeds", "POSTCODE" to "LS1 1AA")))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_KEEP_HOME", answer = StringAnswer("YES")),
      ),
      "HELP_TO_KEEP_HOME",
      "WHERE_WILL_THEY_LIVE_1",
    ),
    // If the answer to WHERE_WILL_THEY_LIVE_1 is MOVE_TO_NEW_ADDRESS, go to WHERE_WILL_THEY_LIVE_ADDRESS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("HOMEOWNER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", answer = MapAnswer(listOf(mapOf("ADDRESS_LINE_1" to "12 High Street", "CITY" to "Leeds", "POSTCODE" to "LS1 1AA")))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_KEEP_HOME", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE_1", answer = StringAnswer("MOVE_TO_NEW_ADDRESS")),
      ),
      "WHERE_WILL_THEY_LIVE_1",
      "WHERE_WILL_THEY_LIVE_ADDRESS",
    ),
    // If the answer to WHERE_WILL_THEY_LIVE_1 is RETURN_TO_PREVIOUS_ADDRESS, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("HOMEOWNER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", answer = MapAnswer(listOf(mapOf("ADDRESS_LINE_1" to "12 High Street", "CITY" to "Leeds", "POSTCODE" to "LS1 1AA")))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_KEEP_HOME", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE_1", answer = StringAnswer("RETURN_TO_PREVIOUS_ADDRESS")),
      ),
      "WHERE_WILL_THEY_LIVE_1",
      "ASSESSMENT_SUMMARY",
    ),
    // If the answer to WHERE_WILL_THEY_LIVE_1 is DOES_NOT_HAVE_ANYWHERE, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("HOMEOWNER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", answer = MapAnswer(listOf(mapOf("ADDRESS_LINE_1" to "12 High Street", "CITY" to "Leeds", "POSTCODE" to "LS1 1AA")))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_KEEP_HOME", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE_1", answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE")),
      ),
      "WHERE_WILL_THEY_LIVE_1",
      "ASSESSMENT_SUMMARY",
    ),
    // If the answer to WHERE_WILL_THEY_LIVE_1 is NO_ANSWER, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("HOMEOWNER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", answer = MapAnswer(listOf(mapOf("ADDRESS_LINE_1" to "12 High Street", "CITY" to "Leeds", "POSTCODE" to "LS1 1AA")))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_KEEP_HOME", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE_1", answer = StringAnswer("NO_ANSWER")),
      ),
      "WHERE_WILL_THEY_LIVE_1",
      "ASSESSMENT_SUMMARY",
    ),
    // If the answer to WHERE_WILL_THEY_LIVE_2 is MOVE_TO_NEW_ADDRESS, go to WHERE_WILL_THEY_LIVE_ADDRESS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("HOMEOWNER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", answer = MapAnswer(listOf(mapOf("ADDRESS_LINE_1" to "12 High Street", "CITY" to "Leeds", "POSTCODE" to "LS1 1AA")))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_KEEP_HOME", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE_2", answer = StringAnswer("MOVE_TO_NEW_ADDRESS")),
      ),
      "WHERE_WILL_THEY_LIVE_2",
      "WHERE_WILL_THEY_LIVE_ADDRESS",
    ),
    // If the answer to WHERE_WILL_THEY_LIVE_2 is DOES_NOT_HAVE_ANYWHERE, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("HOMEOWNER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", answer = MapAnswer(listOf(mapOf("ADDRESS_LINE_1" to "12 High Street", "CITY" to "Leeds", "POSTCODE" to "LS1 1AA")))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_KEEP_HOME", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE_2", answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE")),
      ),
      "WHERE_WILL_THEY_LIVE_2",
      "ASSESSMENT_SUMMARY",
    ),
    // If the answer to WHERE_WILL_THEY_LIVE_2 is NO_ANSWER, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("HOMEOWNER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", answer = MapAnswer(listOf(mapOf("ADDRESS_LINE_1" to "12 High Street", "CITY" to "Leeds", "POSTCODE" to "LS1 1AA")))),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_KEEP_HOME", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE_2", answer = StringAnswer("NO_ANSWER")),
      ),
      "WHERE_WILL_THEY_LIVE_2",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("NO_ANSWER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE_2", answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_NEEDS", answer = StringAnswer("SUPPORT_REQUIRED")),
        ResettlementAssessmentRequestQuestionAndAnswer("CASE_NOTE_SUMMARY", answer = StringAnswer("My case note summary.")),
      ),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  @Test
  fun `test next page function start - existing COMPLETE assessment`() {
    // If there is an existing COMPLETE assessment we should go to CHECK_ANSWERS at the start
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
  fun `test next page function - when SUBMITTED assessment skip over ASSESSMENT_SUMMARY`() {
    // If there is an existing COMPLETE assessment we should go to CHECK_ANSWERS at the start
    val nomsId = "123456"
    setUpMocks(nomsId, true, assessmentStatus = ResettlementAssessmentStatus.SUBMITTED)

    val assessment = ResettlementAssessmentRequest(
      questionsAndAnswers = listOf(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("NO_ANSWER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE_2", answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE")),
      ),
    )
    val nextPage = resettlementAssessmentService.getNextPageId(
      assessment = assessment,
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      currentPage = "WHERE_WILL_THEY_LIVE_2",
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

    setUpMocks(nomsId, true)

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
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "WHERE_DID_THEY_LIVE",
      ResettlementAssessmentResponsePage(
        id = "WHERE_DID_THEY_LIVE",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "WHERE_DID_THEY_LIVE",
              title = "Where did the person in prison live before custody?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = mutableListOf(
                Option(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
                Option(id = "SOCIAL_HOUSING", displayText = "Social housing"),
                Option(id = "HOMEOWNER", displayText = "Homeowner"),
                Option(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
                Option(id = "NO_ANSWER", displayText = "No answer provided"),
              ),
            ),
            originalPageId = "WHERE_DID_THEY_LIVE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "WHERE_DID_THEY_LIVE_ADDRESS",
      ResettlementAssessmentResponsePage(
        id = "WHERE_DID_THEY_LIVE_ADDRESS",
        title = "Where did the person in prison live before custody?",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(id = "WHERE_DID_THEY_LIVE_ADDRESS", title = "Enter the address", type = TypeOfQuestion.ADDRESS),
            originalPageId = "WHERE_DID_THEY_LIVE_ADDRESS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_TO_KEEP_HOME",
      ResettlementAssessmentResponsePage(
        id = "HELP_TO_KEEP_HOME",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "HELP_TO_KEEP_HOME",
              title = "Does the person in prison or their family need help to keep their home while they are in prison?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "HELP_TO_KEEP_HOME",
          ),
        ),
      ),
    ),
    Arguments.of(
      "WHERE_WILL_THEY_LIVE_1",
      ResettlementAssessmentResponsePage(
        id = "WHERE_WILL_THEY_LIVE_1",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "WHERE_WILL_THEY_LIVE_1",
              title = "Where will the person in prison live when they are released?",
              type = TypeOfQuestion.RADIO,
              options = mutableListOf(
                Option(id = "RETURN_TO_PREVIOUS_ADDRESS", displayText = "Return to their previous address"),
                Option(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
                Option(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
                Option(id = "NO_ANSWER", displayText = "No answer provided"),
              ),
            ),
            originalPageId = "WHERE_WILL_THEY_LIVE_1",
          ),
        ),
      ),
    ),
    Arguments.of(
      "WHERE_WILL_THEY_LIVE_2",
      ResettlementAssessmentResponsePage(
        id = "WHERE_WILL_THEY_LIVE_2",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "WHERE_WILL_THEY_LIVE_2",
              title = "Where will the person in prison live when they are released?",
              type = TypeOfQuestion.RADIO,
              options = mutableListOf(
                Option(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
                Option(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
                Option(id = "NO_ANSWER", displayText = "No answer provided"),
              ),
            ),
            originalPageId = "WHERE_WILL_THEY_LIVE_2",
          ),
        ),
      ),
    ),
    Arguments.of(
      "WHERE_WILL_THEY_LIVE_ADDRESS",
      ResettlementAssessmentResponsePage(
        id = "WHERE_WILL_THEY_LIVE_ADDRESS",
        title = "Where will the person in prison live when they are released?",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "WHERE_WILL_THEY_LIVE_ADDRESS",
              title = "Enter the address",
              type = TypeOfQuestion.ADDRESS,
            ),
            originalPageId = "WHERE_WILL_THEY_LIVE_ADDRESS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ASSESSMENT_SUMMARY",
      ResettlementAssessmentResponsePage(
        id = "ASSESSMENT_SUMMARY",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
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
            originalPageId = "ASSESSMENT_SUMMARY",
          ),
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "CASE_NOTE_SUMMARY",
              title = "Add a case note summary",
              subTitle = "This will be displayed as a case note in both DPS and nDelius",
              type = TypeOfQuestion.LONG_TEXT,
            ),
            originalPageId = "ASSESSMENT_SUMMARY",
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
      mutableListOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("SOCIAL_HOUSING")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", MapAnswer(mutableListOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
      ),
    )

    setUpMocks("123", true, existingAssessment)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "WHERE_DID_THEY_LIVE",
      questionsAndAnswers = mutableListOf(
        ResettlementAssessmentResponseQuestionAndAnswer(
          ResettlementAssessmentResponseQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = mutableListOf(
              Option(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
              Option(id = "SOCIAL_HOUSING", displayText = "Social housing"),
              Option(id = "HOMEOWNER", displayText = "Homeowner"),
              Option(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
              Option(id = "NO_ANSWER", displayText = "No answer provided"),
            ),
          ),
          answer = StringAnswer("SOCIAL_HOUSING"),
          originalPageId = "WHERE_DID_THEY_LIVE",
        ),
      ),
    )

    val page = resettlementAssessmentService.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = "WHERE_DID_THEY_LIVE",
    )
    Assertions.assertEquals(expectedPage, page)
  }

  @Test
  fun `test get page from Id check answers - existing assessment`() {
    val nomsId = "123"

    val existingAssessment = ResettlementAssessmentQuestionAndAnswerList(
      mutableListOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("SOCIAL_HOUSING")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
      ),
    )

    setUpMocks("123", true, existingAssessment)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "CHECK_ANSWERS",
      questionsAndAnswers = mutableListOf(
        ResettlementAssessmentResponseQuestionAndAnswer(
          AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE,
          answer = StringAnswer("SOCIAL_HOUSING"),
          originalPageId = "WHERE_DID_THEY_LIVE",
        ),
        ResettlementAssessmentResponseQuestionAndAnswer(
          AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE_ADDRESS,
          answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123"))),
          originalPageId = "WHERE_DID_THEY_LIVE_ADDRESS",
        ),
      ),
    )

    val page = resettlementAssessmentService.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = "CHECK_ANSWERS",
    )
    Assertions.assertEquals(expectedPage, page)
  }

  @Test
  fun `test get page from Id check answers - existing submitted assessment`() {
    val nomsId = "123"

    val existingAssessment = ResettlementAssessmentQuestionAndAnswerList(
      mutableListOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("SOCIAL_HOUSING")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
        ResettlementAssessmentSimpleQuestionAndAnswer("SUPPORT_NEEDS", StringAnswer("SUPPORT_NOT_REQUIRED")),
        ResettlementAssessmentSimpleQuestionAndAnswer("CASE_NOTE_SUMMARY", StringAnswer("Some case notes text...")),
      ),
    )

    setUpMocks("123", true, existingAssessment, ResettlementAssessmentStatus.SUBMITTED)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "CHECK_ANSWERS",
      questionsAndAnswers = mutableListOf(
        ResettlementAssessmentResponseQuestionAndAnswer(
          AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE,
          answer = StringAnswer("SOCIAL_HOUSING"),
          originalPageId = "WHERE_DID_THEY_LIVE",
        ),
        ResettlementAssessmentResponseQuestionAndAnswer(
          AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE_ADDRESS,
          answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123"))),
          originalPageId = "WHERE_DID_THEY_LIVE_ADDRESS",
        ),
      ),
    )

    val page = resettlementAssessmentService.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = "CHECK_ANSWERS",
    )
    Assertions.assertEquals(expectedPage, page)
  }

  @Test
  fun `test get page from Id check answers - RESETTLEMENT_PLAN existing submitted edit BCST2 assessment`() {
    val nomsId = "123"

    val existingAssessment = ResettlementAssessmentQuestionAndAnswerList(
      mutableListOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("SOCIAL_HOUSING")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
      ),
    )

    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val pathwayEntity = PathwayEntity(1, "Accommodation", true, testDate)
    val resettlementAssessmentStatusEntities = listOf(ResettlementAssessmentStatusEntity(3, "Complete", true, testDate), ResettlementAssessmentStatusEntity(4, "Submitted", true, testDate))
    val resettlementAssessmentEntity = ResettlementAssessmentEntity(1, prisonerEntity, pathwayEntity, StatusEntity(1, "Not Started", true, testDate), ResettlementAssessmentType.BCST2, existingAssessment, testDate, "", resettlementAssessmentStatusEntities.first { it.id == ResettlementAssessmentStatus.SUBMITTED.id }, "some text", "USER_1")
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
    Mockito.`when`(
      resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        prisonerEntity,
        pathwayEntity,
        ResettlementAssessmentType.RESETTLEMENT_PLAN,
        resettlementAssessmentStatusEntities,
      ),
    ).thenReturn(null)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "CHECK_ANSWERS",
      questionsAndAnswers = mutableListOf(
        ResettlementAssessmentResponseQuestionAndAnswer(
          AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE,
          answer = StringAnswer("SOCIAL_HOUSING"),
          originalPageId = "WHERE_DID_THEY_LIVE",
        ),
        ResettlementAssessmentResponseQuestionAndAnswer(
          AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE_ADDRESS,
          answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123"))),
          originalPageId = "WHERE_DID_THEY_LIVE_ADDRESS",
        ),
        ResettlementAssessmentResponseQuestionAndAnswer(
          GenericResettlementAssessmentQuestion.SUPPORT_NEEDS,
          answer = StringAnswer(answer = null),
          originalPageId = "ASSESSMENT_SUMMARY",
        ),
        ResettlementAssessmentResponseQuestionAndAnswer(
          GenericResettlementAssessmentQuestion.CASE_NOTE_SUMMARY,
          answer = StringAnswer(answer = null),
          originalPageId = "ASSESSMENT_SUMMARY",
        ),
      ),
    )

    val page = resettlementAssessmentService.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN,
      pageId = "CHECK_ANSWERS",
    )
    Assertions.assertEquals(expectedPage, page)
  }

  @ParameterizedTest
  @MethodSource("test complete assessment data")
  fun `test complete assessment`(assessment: ResettlementAssessmentCompleteRequest, expectedEntity: ResettlementAssessmentEntity?, expectedException: Throwable?, existingAssessment: ResettlementAssessmentEntity?) {
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken("string", "name") } returns "System user"
    every { getClaimFromJWTToken("string", "auth_source") } returns "nomis"
    every { getClaimFromJWTToken("string", "sub") } returns "USER_1"
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns testDate

    val nomsId = "abc"
    val pathway = Pathway.ACCOMMODATION
    val assessmentType = ResettlementAssessmentType.BCST2

    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val pathwayEntity = PathwayEntity(1, "Accommodation", true, testDate)
    val completeResettlementAssessmentStatusEntity = ResettlementAssessmentStatusEntity(3, "Complete", true, testDate)
    val submittedResettlementAssessmentStatusEntity = ResettlementAssessmentStatusEntity(4, "Submitted", true, testDate)
    val statusEntity = StatusEntity(6, "Support Required", true, testDate)

    Mockito.lenient().`when`(pathwayRepository.findById(Pathway.ACCOMMODATION.id)).thenReturn(Optional.of(pathwayEntity))
    Mockito.lenient().`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.lenient().`when`(resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.COMPLETE.id))
      .thenReturn(Optional.of(completeResettlementAssessmentStatusEntity))
    Mockito.lenient().`when`(resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.SUBMITTED.id))
      .thenReturn(Optional.of(submittedResettlementAssessmentStatusEntity))
    Mockito.lenient().`when`(statusRepository.findById(Status.SUPPORT_REQUIRED.id)).thenReturn(Optional.of(statusEntity))

    if (existingAssessment != null) {
      val resettlementAssessmentStatusEntities = listOf(ResettlementAssessmentStatusEntity(3, "Complete", true, testDate), ResettlementAssessmentStatusEntity(4, "Submitted", true, testDate))
      Mockito.`when`(resettlementAssessmentStatusRepository.findAll()).thenReturn(resettlementAssessmentStatusEntities)
      Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(prisonerEntity, pathwayEntity, assessmentType, resettlementAssessmentStatusEntities)).thenReturn(existingAssessment)
    }

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
      ServerWebInputException("Error validating questions and answers - error validating page flow [400 BAD_REQUEST \"Error validating questions and answers - expected page [WHERE_DID_THEY_LIVE] is different to actual page [null] at index [0]\"]"),
      null,
    ),
    // Throw exception if SUPPORT_NEEDS answer in wrong format
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = ListAnswer(listOf("SUPPORT_REQUIRED", "SUPPORT_DECLINED")),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "CASE_NOTE_SUMMARY",
            answer = StringAnswer("Some text"),
          ),
        ),
      ),
      null,
      ServerWebInputException("Support need [ListAnswer(answer=[SUPPORT_REQUIRED, SUPPORT_DECLINED])] must be a StringAnswer"),
      null,
    ),
    // Throw exception if SUPPORT_NEEDS answer is null
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer(null),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "CASE_NOTE_SUMMARY",
            answer = StringAnswer("Some text"),
          ),
        ),
      ),
      null,
      ServerWebInputException("Support need [StringAnswer(answer=null)] is not a valid option"),
      null,
    ),
    // Throw exception if SUPPORT_NEEDS answer is not a valid option
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_WANTED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "CASE_NOTE_SUMMARY",
            answer = StringAnswer("Some text"),
          ),
        ),
      ),
      null,
      ServerWebInputException("Support need [StringAnswer(answer=SUPPORT_WANTED)] is not a valid option"),
      null,
    ),
    // Throw exception if CASE_NOTE_SUMMARY question not answered
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
        ),
      ),
      null,
      ServerWebInputException("Error validating questions and answers - wrong questions answered on page [ASSESSMENT_SUMMARY]. Expected [[SUPPORT_NEEDS, CASE_NOTE_SUMMARY]] but found [[SUPPORT_NEEDS]]"),
      null,
    ),
    // Throw exception if CASE_NOTE_SUMMARY answer in wrong format
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
          ),
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
      null,
    ),
    // Throw exception if CASE_NOTE_SUMMARY is null
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
          ),
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
      null,
    ),
    // Happy path - no existing assessment
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
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
      ResettlementAssessmentEntity(id = null, prisoner = PrisonerEntity(id = 1, nomsId = "abc", creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), crn = "abc", prisonId = "ABC", releaseDate = LocalDate.parse("2025-01-23")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), statusChangedTo = StatusEntity(id = 6, name = "Support Required", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(mutableListOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), caseNoteText = "My case note summary...", createdByUserId = "USER_1"),
      null,
      null,
    ),
    // Happy path - existing COMPLETE assessment
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
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
      ResettlementAssessmentEntity(id = null, prisoner = PrisonerEntity(id = 1, nomsId = "abc", creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), crn = "abc", prisonId = "ABC", releaseDate = LocalDate.parse("2025-01-23")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), statusChangedTo = StatusEntity(id = 6, name = "Support Required", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(mutableListOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), caseNoteText = "My case note summary...", createdByUserId = "USER_1"),
      null,
      ResettlementAssessmentEntity(id = 12, prisoner = PrisonerEntity(id = 1, nomsId = "abc", creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), crn = "abc", prisonId = "ABC", releaseDate = LocalDate.parse("2025-01-23")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), statusChangedTo = StatusEntity(id = 3, name = "Support Not Required", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(mutableListOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_NOT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), caseNoteText = "My case note summary...", createdByUserId = "USER_1"),
    ),
    // Happy path - existing SUBMITTED assessment
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
          ),
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisoner = PrisonerEntity(id = 1, nomsId = "abc", creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), crn = "abc", prisonId = "ABC", releaseDate = LocalDate.parse("2025-01-23")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), statusChangedTo = null, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(mutableListOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatusEntity(id = 4, name = "Submitted", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), caseNoteText = null, createdByUserId = "USER_1"),
      null,
      ResettlementAssessmentEntity(id = 12, prisoner = PrisonerEntity(id = 1, nomsId = "abc", creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), crn = "abc", prisonId = "ABC", releaseDate = LocalDate.parse("2025-01-23")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), statusChangedTo = StatusEntity(id = 3, name = "Support Not Required", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(mutableListOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_NOT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatusEntity(id = 4, name = "Submitted", active = true, creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000")), caseNoteText = "My case note summary...", createdByUserId = "USER_1"),
    ),
  )

  private fun setUpMocks(nomsId: String, returnResettlementAssessmentEntity: Boolean, assessment: ResettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentQuestionAndAnswerList(mutableListOf()), assessmentStatus: ResettlementAssessmentStatus = ResettlementAssessmentStatus.COMPLETE) {
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val pathwayEntity = PathwayEntity(1, "Accommodation", true, testDate)
    val resettlementAssessmentStatusEntities = listOf(ResettlementAssessmentStatusEntity(3, "Complete", true, testDate), ResettlementAssessmentStatusEntity(4, "Submitted", true, testDate))
    val resettlementAssessmentEntity = if (returnResettlementAssessmentEntity) ResettlementAssessmentEntity(1, prisonerEntity, pathwayEntity, StatusEntity(1, "Not Started", true, testDate), ResettlementAssessmentType.BCST2, assessment, testDate, "", resettlementAssessmentStatusEntities.first { it.id == assessmentStatus.id }, "some text", "USER_1") else null
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

  @ParameterizedTest
  @MethodSource("test findPageIdFromQuestionId data")
  fun `test findPageIdFromQuestionId`(questionId: String, expectedPageId: String) {
    Assertions.assertEquals(expectedPageId, resettlementAssessmentService.findPageIdFromQuestionId(questionId))
  }

  private fun `test findPageIdFromQuestionId data`() = Stream.of(
    Arguments.of(AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE.id, AccommodationAssessmentPage.WHERE_DID_THEY_LIVE.id),
    Arguments.of(AccommodationResettlementAssessmentQuestion.WHERE_DID_THEY_LIVE_ADDRESS.id, AccommodationAssessmentPage.WHERE_DID_THEY_LIVE_ADDRESS.id),
    Arguments.of(AccommodationResettlementAssessmentQuestion.HELP_TO_KEEP_HOME.id, AccommodationAssessmentPage.HELP_TO_KEEP_HOME.id),
    Arguments.of(AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_1.id, AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE_1.id),
    Arguments.of(AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_ADDRESS.id, AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE_ADDRESS.id),
    Arguments.of(AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE_2.id, AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE_2.id),
    Arguments.of(GenericResettlementAssessmentQuestion.SUPPORT_NEEDS.id, GenericAssessmentPage.ASSESSMENT_SUMMARY.id),
    Arguments.of(GenericResettlementAssessmentQuestion.CASE_NOTE_SUMMARY.id, GenericAssessmentPage.ASSESSMENT_SUMMARY.id),
  )

  @Test
  fun `test getQuestionList`() {
    Assertions.assertEquals(AccommodationResettlementAssessmentQuestion.entries + GenericResettlementAssessmentQuestion.entries, resettlementAssessmentService.getQuestionList())
  }

  @ParameterizedTest
  @MethodSource("test validateQuestionAndAnswerSet data")
  fun `test validateQuestionAndAnswerSet`(assessment: ResettlementAssessmentCompleteRequest, valid: Boolean) {
    if (valid) {
      resettlementAssessmentService.validateQuestionAndAnswerSet(assessment, false)
    } else {
      assertThrows<ServerWebInputException> { resettlementAssessmentService.validateQuestionAndAnswerSet(assessment, false) }
    }
  }

  private fun `test validateQuestionAndAnswerSet data`() = Stream.of(
    // Happy path 1 - correct set of questions asked in single path through tree
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("PRIVATE_RENTED_HOUSING"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDRESS",
            answer = MapAnswer(answer = listOf(mapOf("Key 1" to "Value 1", "Key 2" to "Value 2"), mapOf("Something" to "Something else"))),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "HELP_TO_KEEP_HOME",
            answer = StringAnswer("YES"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_1",
            answer = StringAnswer("RETURN_TO_PREVIOUS_ADDRESS"),
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
      true,
    ),
    // Happy path 2 - correct set of questions asked in single path through tree
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
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
      true,
    ),
    // Happy path 3 - correct set of questions asked in single path through tree
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS",
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
      true,
    ),
    // Error case - not all questions answered on ASSESSMENT_SUMMARY page
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS",
            answer = MapAnswer(answer = listOf(mapOf("Key 1" to "Value 1", "Key 2" to "Value 2"), mapOf("Something" to "Something else"))),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
        ),
      ),
      false,
    ),
    // Error case - questions not starting on first page
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS",
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
      false,
    ),
    // Error case - same question answered multiple times
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("NO_ANSWER"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS",
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
      false,
    ),
    // Error case - questions answered from multiple logical branches
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_1",
            answer = StringAnswer("RETURN_TO_PREVIOUS_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS",
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
      false,
    ),
    // Error case - logic not followed correctly
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDRESS",
            answer = MapAnswer(answer = listOf(mapOf("Key 1" to "Value 1", "Key 2" to "Value 2"), mapOf("Something" to "Something else"))),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_1",
            answer = StringAnswer("RETURN_TO_PREVIOUS_ADDRESS"),
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
      false,
    ),
    // Error case - unknown questions answered
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "RANDOM_QUESTION",
            answer = StringAnswer("RANDOM_ANSWER"),
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
      false,
    ),
    // Error case - answered no questions
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(),
      ),
      false,
    ),
    // Error case - bad answers to questions
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NOT_A_VALID_ANSWER"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
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
      false,
    ),
    // Error case - missing assessment summary
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_2",
            answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE"),
          ),
        ),
      ),
      false,
    ),
  )
}
