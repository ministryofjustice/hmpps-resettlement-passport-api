package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream

class AccommodationV1ResettlementAssessmentAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.ACCOMMODATION) {

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
    val nextPage = resettlementAssessmentStrategy.getNextPageId(
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
    val nextPage = resettlementAssessmentStrategy.getNextPageId(
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
    val nomsId = "123456"
    setUpMocks(nomsId, true, assessmentStatus = ResettlementAssessmentStatus.SUBMITTED)

    val assessment = ResettlementAssessmentRequest(
      questionsAndAnswers = listOf(
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("NO_ANSWER")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE_2", answer = StringAnswer("DOES_NOT_HAVE_ANYWHERE")),
      ),
    )
    val nextPage = resettlementAssessmentStrategy.getNextPageId(
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
      resettlementAssessmentStrategy.getNextPageId(
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
      resettlementAssessmentStrategy.getNextPageId(
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

    val page = resettlementAssessmentStrategy.getPageFromId(
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WHERE_DID_THEY_LIVE",
              title = "Where did the person in prison live before custody?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
                ResettlementAssessmentOption(id = "SOCIAL_HOUSING", displayText = "Social housing"),
                ResettlementAssessmentOption(id = "HOMEOWNER", displayText = "Homeowner"),
                ResettlementAssessmentOption(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
                ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided"),
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(id = "WHERE_DID_THEY_LIVE_ADDRESS", title = "Enter the address", type = TypeOfQuestion.ADDRESS),
            originalPageId = "WHERE_DID_THEY_LIVE_ADDRESS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_TO_KEEP_HOME",
      ResettlementAssessmentResponsePage(
        id = "HELP_TO_KEEP_HOME",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HELP_TO_KEEP_HOME",
              title = "Does the person in prison or their family need help to keep their home while they are in prison?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WHERE_WILL_THEY_LIVE_1",
              title = "Where will the person in prison live when they are released?",
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(id = "RETURN_TO_PREVIOUS_ADDRESS", displayText = "Return to their previous address"),
                ResettlementAssessmentOption(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
                ResettlementAssessmentOption(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
                ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided"),
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WHERE_WILL_THEY_LIVE_2",
              title = "Where will the person in prison live when they are released?",
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
                ResettlementAssessmentOption(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
                ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided"),
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
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
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
        title = "Accommodation report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Accommodation support needs",
              subTitle = "Select one option.",
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "SUPPORT_REQUIRED",
                  displayText = "Support required",
                  description = "a need for support has been identified and is accepted",
                ),
                ResettlementAssessmentOption(id = "SUPPORT_NOT_REQUIRED", displayText = "Support not required", description = "no need was identified"),
                ResettlementAssessmentOption(
                  id = "SUPPORT_DECLINED",
                  displayText = "Support declined",
                  description = "a need has been identified but support is declined",
                ),
              ),
            ),
            originalPageId = "ASSESSMENT_SUMMARY",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CASE_NOTE_SUMMARY",
              title = "Case note summary",
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
        questionsAndAnswers = listOf(),
      ),
    ),
  )

  @Test
  fun `test get page from Id - existing assessment`() {
    val nomsId = "123"

    val existingAssessment = ResettlementAssessmentQuestionAndAnswerList(
      listOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("SOCIAL_HOUSING")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
      ),
    )

    setUpMocks("123", true, existingAssessment)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "WHERE_DID_THEY_LIVE",
      questionsAndAnswers = listOf(
        ResettlementAssessmentQuestionAndAnswer(
          ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
              ResettlementAssessmentOption(id = "SOCIAL_HOUSING", displayText = "Social housing"),
              ResettlementAssessmentOption(id = "HOMEOWNER", displayText = "Homeowner"),
              ResettlementAssessmentOption(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
              ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided"),
            ),
          ),
          answer = StringAnswer("SOCIAL_HOUSING"),
          originalPageId = "WHERE_DID_THEY_LIVE",
        ),
      ),
    )

    val page = resettlementAssessmentStrategy.getPageFromId(
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
      listOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("SOCIAL_HOUSING")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
      ),
    )

    setUpMocks("123", true, existingAssessment)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "CHECK_ANSWERS",
      questionsAndAnswers = listOf(
        ResettlementAssessmentQuestionAndAnswer(
          ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
              ResettlementAssessmentOption(id = "SOCIAL_HOUSING", displayText = "Social housing"),
              ResettlementAssessmentOption(id = "HOMEOWNER", displayText = "Homeowner"),
              ResettlementAssessmentOption(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
              ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided"),
            ),
          ),
          answer = StringAnswer("SOCIAL_HOUSING"),
          originalPageId = "WHERE_DID_THEY_LIVE",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE_ADDRESS",
            title = "Enter the address",
            subTitle = null,
            type = TypeOfQuestion.ADDRESS,
            options = null,
          ),
          answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123"))),
          originalPageId = "WHERE_DID_THEY_LIVE_ADDRESS",
        ),
      ),
    )

    val page = resettlementAssessmentStrategy.getPageFromId(
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
      listOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("SOCIAL_HOUSING")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
        ResettlementAssessmentSimpleQuestionAndAnswer("SUPPORT_NEEDS", StringAnswer("SUPPORT_NOT_REQUIRED")),
        ResettlementAssessmentSimpleQuestionAndAnswer("CASE_NOTE_SUMMARY", StringAnswer("Some case notes text...")),
      ),
    )

    setUpMocks("123", true, existingAssessment, ResettlementAssessmentStatus.SUBMITTED)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "CHECK_ANSWERS",
      questionsAndAnswers = listOf(
        ResettlementAssessmentQuestionAndAnswer(
          ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
              ResettlementAssessmentOption(id = "SOCIAL_HOUSING", displayText = "Social housing"),
              ResettlementAssessmentOption(id = "HOMEOWNER", displayText = "Homeowner"),
              ResettlementAssessmentOption(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
              ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided"),
            ),
          ),
          answer = StringAnswer("SOCIAL_HOUSING"),
          originalPageId = "WHERE_DID_THEY_LIVE",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE_ADDRESS",
            title = "Enter the address",
            subTitle = null,
            type = TypeOfQuestion.ADDRESS,
            options = null,
          ),
          answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123"))),
          originalPageId = "WHERE_DID_THEY_LIVE_ADDRESS",
        ),
      ),
    )

    val page = resettlementAssessmentStrategy.getPageFromId(
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
      listOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("SOCIAL_HOUSING")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
      ),
    )

    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val resettlementAssessmentEntity = ResettlementAssessmentEntity(1, 1, Pathway.ACCOMMODATION, Status.NOT_STARTED, ResettlementAssessmentType.BCST2, existingAssessment, testDate, "", ResettlementAssessmentStatus.SUBMITTED, "some text", "USER_1", submissionDate = null, version = 1, userDeclaration = false)
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    whenever(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        1,
        Pathway.ACCOMMODATION,
        ResettlementAssessmentType.BCST2,
        listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
      ),
    ).thenReturn(resettlementAssessmentEntity)
    whenever(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        1,
        Pathway.ACCOMMODATION,
        ResettlementAssessmentType.RESETTLEMENT_PLAN,
        listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
      ),
    ).thenReturn(null)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "CHECK_ANSWERS",
      questionsAndAnswers = listOf(
        ResettlementAssessmentQuestionAndAnswer(
          ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
              ResettlementAssessmentOption(id = "SOCIAL_HOUSING", displayText = "Social housing"),
              ResettlementAssessmentOption(id = "HOMEOWNER", displayText = "Homeowner"),
              ResettlementAssessmentOption(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
              ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided"),
            ),
          ),
          answer = StringAnswer("SOCIAL_HOUSING"),
          originalPageId = "WHERE_DID_THEY_LIVE",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE_ADDRESS",
            title = "Enter the address",
            subTitle = null,
            type = TypeOfQuestion.ADDRESS,
            options = null,
          ),
          answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123"))),
          originalPageId = "WHERE_DID_THEY_LIVE_ADDRESS",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          ResettlementAssessmentQuestion(
            id = "SUPPORT_NEEDS_PRERELEASE",
            title = "Accommodation support needs",
            subTitle = "Select one option.",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(id = "SUPPORT_REQUIRED", displayText = "Support required", description = "a need for support has been identified and is accepted"),
              ResettlementAssessmentOption(id = "SUPPORT_NOT_REQUIRED", displayText = "Support not required", description = "no need was identified"),
              ResettlementAssessmentOption(id = "SUPPORT_DECLINED", displayText = "Support declined", description = "a need has been identified but support is declined"),
              ResettlementAssessmentOption(id = "IN_PROGRESS", displayText = "In progress", description = "work is ongoing"),
              ResettlementAssessmentOption(id = "DONE", displayText = "Done", description = "all required work has been completed successfully"),
            ),
          ),
          answer = StringAnswer(answer = null),
          originalPageId = "PRERELEASE_ASSESSMENT_SUMMARY",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          ResettlementAssessmentQuestion(
            id = "CASE_NOTE_SUMMARY",
            title = "Case note summary",
            subTitle = "This will be displayed as a case note in both DPS and nDelius",
            type = TypeOfQuestion.LONG_TEXT,
            options = null,
          ),
          answer = StringAnswer(answer = null),
          originalPageId = "PRERELEASE_ASSESSMENT_SUMMARY",
        ),
      ),
    )

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN,
      pageId = "CHECK_ANSWERS",
    )
    Assertions.assertEquals(expectedPage, page)
  }

  @ParameterizedTest
  @MethodSource("test complete assessment data")
  fun `test complete assessment`(assessmentType: ResettlementAssessmentType, assessment: ResettlementAssessmentCompleteRequest, expectedEntity: ResettlementAssessmentEntity?, expectedException: Throwable?, existingAssessment: ResettlementAssessmentEntity?) {
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken("string", "name") } returns "System user"
    every { getClaimFromJWTToken("string", "auth_source") } returns "nomis"
    every { getClaimFromJWTToken("string", "sub") } returns "USER_1"
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns testDate

    val nomsId = "abc"
    val pathway = Pathway.ACCOMMODATION

    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))

    Mockito.lenient().`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)

    if (existingAssessment != null) {
      whenever(
        resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
          1,
          Pathway.ACCOMMODATION,
          assessmentType,
          listOf(
            ResettlementAssessmentStatus.COMPLETE,
            ResettlementAssessmentStatus.SUBMITTED,
          ),
        ),
      ).thenReturn(existingAssessment)
    }

    if (expectedException == null) {
      stubSave()
      resettlementAssessmentStrategy.completeAssessment(nomsId, pathway, assessmentType, assessment, "string", false)
      Mockito.verify(resettlementAssessmentRepository).save(expectedEntity!!)
    } else {
      val actualException = assertThrows<Throwable> {
        resettlementAssessmentStrategy.completeAssessment(nomsId, pathway, assessmentType, assessment, "string", false)
      }
      Assertions.assertEquals(expectedException::class, actualException::class)
      Assertions.assertEquals(expectedException.message, actualException.message)
    }

    unmockkAll()
  }

  private fun `test complete assessment data`() = Stream.of(
    // Throw exception if SUPPORT_NEEDS question not answered 1
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(questionsAndAnswers = listOf(), version = 1),
      null,
      ServerWebInputException("Error validating questions and answers - error validating page flow [400 BAD_REQUEST \"Error validating questions and answers - expected page [WHERE_DID_THEY_LIVE] is different to actual page [null] at index [0]\"]"),
      null,
    ),
    // Throw exception if SUPPORT_NEEDS answer in wrong format 2
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
    // Throw exception if SUPPORT_NEEDS answer is null 3
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
      ServerWebInputException("No answer provided for mandatory question [SUPPORT_NEEDS]"),
      null,
    ),
    // Throw exception if SUPPORT_NEEDS answer is not a valid option 4
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
    // Throw exception if CASE_NOTE_SUMMARY question not answered 5
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
    // Throw exception if CASE_NOTE_SUMMARY answer in wrong format 6
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
    // Throw exception if CASE_NOTE_SUMMARY is null 7
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
      ServerWebInputException("No answer provided for mandatory question [CASE_NOTE_SUMMARY]"),
      null,
    ),
    // Happy path - BCST2 and no existing assessment 8
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "My case note summary...", createdByUserId = "USER_1", submissionDate = null, version = 1, userDeclaration = false),
      null,
      null,
    ),
    // Happy path - RESETTLEMENT_PLAN and no existing assessment 9
    Arguments.of(
      ResettlementAssessmentType.RESETTLEMENT_PLAN,
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
            question = "SUPPORT_NEEDS_PRERELEASE",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "CASE_NOTE_SUMMARY",
            answer = StringAnswer("My case note summary..."),
          ),
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS_PRERELEASE", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "My case note summary...", createdByUserId = "USER_1", submissionDate = null, version = 1, userDeclaration = false),
      null,
      null,
    ),
    // Happy path - existing COMPLETE assessment 10
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "My case note summary...", createdByUserId = "USER_1", submissionDate = null, version = 1, userDeclaration = false),
      null,
      ResettlementAssessmentEntity(id = 12, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_NOT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_NOT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "My case note summary...", createdByUserId = "USER_1", submissionDate = null, version = 1, userDeclaration = false),
    ),
    // Happy path - existing SUBMITTED assessment 11
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = null, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "USER_1", submissionDate = testDate, version = 1, userDeclaration = false),
      null,
      ResettlementAssessmentEntity(id = 12, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_NOT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_NOT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "My case note summary...", createdByUserId = "USER_1", submissionDate = LocalDateTime.parse("2024-05-01T12:00:01"), version = 1, userDeclaration = false),
    ),
  )

  @ParameterizedTest
  @MethodSource("test findPageIdFromQuestionId data")
  fun `test findPageIdFromQuestionId`(questionId: String, expectedPageId: String) {
    Assertions.assertEquals(expectedPageId, resettlementAssessmentStrategy.findPageIdFromQuestionId(questionId, ResettlementAssessmentType.BCST2, Pathway.ACCOMMODATION, 1))
  }

  private fun `test findPageIdFromQuestionId data`() = Stream.of(
    Arguments.of("WHERE_DID_THEY_LIVE", "WHERE_DID_THEY_LIVE"),
    Arguments.of("WHERE_DID_THEY_LIVE_ADDRESS", "WHERE_DID_THEY_LIVE_ADDRESS"),
    Arguments.of("HELP_TO_KEEP_HOME", "HELP_TO_KEEP_HOME"),
    Arguments.of("WHERE_WILL_THEY_LIVE_1", "WHERE_WILL_THEY_LIVE_1"),
    Arguments.of("WHERE_WILL_THEY_LIVE_ADDRESS", "WHERE_WILL_THEY_LIVE_ADDRESS"),
    Arguments.of("WHERE_WILL_THEY_LIVE_2", "WHERE_WILL_THEY_LIVE_2"),
    Arguments.of("SUPPORT_NEEDS", "ASSESSMENT_SUMMARY"),
    Arguments.of("CASE_NOTE_SUMMARY", "ASSESSMENT_SUMMARY"),
  )

  @ParameterizedTest
  @MethodSource("test validateQuestionAndAnswerSet data")
  fun `test validateQuestionAndAnswerSet`(assessment: ResettlementAssessmentCompleteRequest, valid: Boolean) {
    if (valid) {
      resettlementAssessmentStrategy.validateQuestionAndAnswerSet(Pathway.ACCOMMODATION, assessment, false)
    } else {
      assertThrows<ServerWebInputException> {
        resettlementAssessmentStrategy.validateQuestionAndAnswerSet(
          Pathway.ACCOMMODATION,
          assessment,
          false,
        )
      }
    }
  }

  private fun `test validateQuestionAndAnswerSet data`() = Stream.of(
    // Happy path 1 - correct set of questions asked in single path through tree
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
        version = 1,
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
        version = 1,
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
        version = 1,
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
        version = 1,
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
        version = 1,
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
        version = 1,
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
        version = 1,
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
        version = 1,
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
        version = 1,
      ),
      false,
    ),
    // Error case - bad answers to questions
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        version = 1,
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
        version = 1,
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

  @Test
  fun `test get config for valid pathway and assessment type`() {
    val expectedPages = listOf(
      AssessmentConfigPage(
        id = "WHERE_DID_THEY_LIVE",
        title = null,
        questions = listOf(
          AssessmentConfigQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              AssessmentConfigOption(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
              AssessmentConfigOption(id = "SOCIAL_HOUSING", displayText = "Social housing"),
              AssessmentConfigOption(id = "HOMEOWNER", displayText = "Homeowner"),
              AssessmentConfigOption(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
              AssessmentConfigOption(id = "NO_ANSWER", displayText = "No answer provided"),
            ),
          ),
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            questionId = "WHERE_DID_THEY_LIVE",
            nextPageId = "WHERE_DID_THEY_LIVE_ADDRESS",
            answers = listOf(
              StringAnswer(answer = "PRIVATE_RENTED_HOUSING"),
              StringAnswer(answer = "SOCIAL_HOUSING"),
              StringAnswer(answer = "HOMEOWNER"),
            ),
          ),
          AssessmentConfigNextPageOption(
            questionId = "WHERE_DID_THEY_LIVE",
            nextPageId = "WHERE_WILL_THEY_LIVE_2",
            answers = listOf(
              StringAnswer(answer = "NO_PERMANENT_OR_FIXED"),
              StringAnswer(answer = "NO_ANSWER"),
            ),
          ),
        ),
      ),
      AssessmentConfigPage(
        id = "WHERE_DID_THEY_LIVE_ADDRESS",
        title = "Where did the person in prison live before custody?",
        questions = listOf(
          AssessmentConfigQuestion(
            id = "WHERE_DID_THEY_LIVE_ADDRESS",
            title = "Enter the address",
            subTitle = null,
            type = TypeOfQuestion.ADDRESS,
            options = null,
          ),
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            questionId = null,
            nextPageId = "HELP_TO_KEEP_HOME",
            answers = null,
          ),
        ),
      ),
      AssessmentConfigPage(
        id = "HELP_TO_KEEP_HOME",
        title = null,
        questions = listOf(
          AssessmentConfigQuestion(
            id = "HELP_TO_KEEP_HOME",
            title = "Does the person in prison or their family need help to keep their home while they are in prison?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              AssessmentConfigOption(id = "YES", displayText = "Yes"),
              AssessmentConfigOption(id = "NO", displayText = "No"),
              AssessmentConfigOption(id = "NO_ANSWER", displayText = "No answer provided"),
            ),
          ),
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            questionId = null,
            nextPageId = "WHERE_WILL_THEY_LIVE_1",
            answers = null,
          ),
        ),
      ),
      AssessmentConfigPage(
        id = "WHERE_WILL_THEY_LIVE_1",
        title = null,
        questions = listOf(
          AssessmentConfigQuestion(
            id = "WHERE_WILL_THEY_LIVE_1",
            title = "Where will the person in prison live when they are released?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              AssessmentConfigOption(id = "RETURN_TO_PREVIOUS_ADDRESS", displayText = "Return to their previous address"),
              AssessmentConfigOption(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
              AssessmentConfigOption(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
              AssessmentConfigOption(id = "NO_ANSWER", displayText = "No answer provided"),
            ),
          ),
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            questionId = "WHERE_WILL_THEY_LIVE_1",
            nextPageId = "WHERE_WILL_THEY_LIVE_ADDRESS",
            answers = listOf(
              StringAnswer(answer = "MOVE_TO_NEW_ADDRESS"),
            ),
          ),
          AssessmentConfigNextPageOption(
            questionId = "WHERE_WILL_THEY_LIVE_1",
            nextPageId = "FINAL_QUESTION_NEXT_PAGE",
            answers = listOf(
              StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS"),
              StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE"),
              StringAnswer(answer = "NO_ANSWER"),
            ),
          ),
        ),
      ),
      AssessmentConfigPage(
        id = "WHERE_WILL_THEY_LIVE_2",
        title = null,
        questions = listOf(
          AssessmentConfigQuestion(
            id = "WHERE_WILL_THEY_LIVE_2",
            title = "Where will the person in prison live when they are released?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              AssessmentConfigOption(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
              AssessmentConfigOption(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
              AssessmentConfigOption(id = "NO_ANSWER", displayText = "No answer provided"),
            ),
          ),
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            questionId = "WHERE_WILL_THEY_LIVE_2",
            nextPageId = "WHERE_WILL_THEY_LIVE_ADDRESS",
            answers = listOf(
              StringAnswer(answer = "MOVE_TO_NEW_ADDRESS"),
            ),
          ),
          AssessmentConfigNextPageOption(
            questionId = "WHERE_WILL_THEY_LIVE_2",
            nextPageId = "FINAL_QUESTION_NEXT_PAGE",
            answers = listOf(
              StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE"),
              StringAnswer(answer = "NO_ANSWER"),
            ),
          ),
        ),
      ),
      AssessmentConfigPage(
        id = "WHERE_WILL_THEY_LIVE_ADDRESS",
        title = "Where will the person in prison live when they are released?",
        questions = listOf(
          AssessmentConfigQuestion(
            id = "WHERE_WILL_THEY_LIVE_ADDRESS",
            title = "Enter the address",
            subTitle = null,
            type = TypeOfQuestion.ADDRESS,
            options = null,
          ),
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            questionId = null,
            nextPageId = "FINAL_QUESTION_NEXT_PAGE",
            answers = null,
          ),
        ),
      ),
      AssessmentConfigPage(
        id = "ASSESSMENT_SUMMARY",
        title = "Accommodation report summary",
        questions = listOf(
          AssessmentConfigQuestion(
            id = "SUPPORT_NEEDS",
            title = "Accommodation support needs",
            subTitle = "Select one option.",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              AssessmentConfigOption(id = "SUPPORT_REQUIRED", displayText = "Support required", description = "a need for support has been identified and is accepted"),
              AssessmentConfigOption(id = "SUPPORT_NOT_REQUIRED", displayText = "Support not required", description = "no need was identified"),
              AssessmentConfigOption(id = "SUPPORT_DECLINED", displayText = "Support declined", description = "a need has been identified but support is declined"),
            ),
          ),
          AssessmentConfigQuestion(
            id = "CASE_NOTE_SUMMARY",
            title = "Case note summary",
            subTitle = "This will be displayed as a case note in both DPS and nDelius",
            type = TypeOfQuestion.LONG_TEXT,
            options = null,
          ),
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            questionId = null,
            nextPageId = "CHECK_ANSWERS",
            answers = null,
          ),
        ),
      ),
      AssessmentConfigPage(
        id = "CHECK_ANSWERS",
        title = null,
        questions = null,
        nextPageLogic = null,
      ),
    )

    val expectedQuestionSet = AssessmentQuestionSet(
      version = 1,
      pathway = Pathway.ACCOMMODATION,
      pages = expectedPages,
    )

    val assessmentQuestionSet = resettlementAssessmentStrategy.getConfig(Pathway.ACCOMMODATION, ResettlementAssessmentType.BCST2, version = 1)
    Assertions.assertEquals(expectedQuestionSet, assessmentQuestionSet)
  }

  @Test
  fun `test get config for invalid pathway`() {
    val invalidPathway = "INVALID_PATHWAY"
    Assertions.assertThrows(IllegalArgumentException::class.java) {
      resettlementAssessmentStrategy.getConfig(Pathway.valueOf(invalidPathway), ResettlementAssessmentType.BCST2, version = 1)
    }
  }
}
