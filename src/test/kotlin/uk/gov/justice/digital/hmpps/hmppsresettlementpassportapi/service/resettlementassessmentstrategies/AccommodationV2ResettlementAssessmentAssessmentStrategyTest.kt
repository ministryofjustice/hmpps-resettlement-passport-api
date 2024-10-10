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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream

class AccommodationV2ResettlementAssessmentAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.ACCOMMODATION, 2) {

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
      version = 2,
    )
    Assertions.assertEquals(expectedPage, nextPage)
  }

  private fun `test next page function flow - no existing assessment data`() = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "PAST_AND_FUTURE_ACCOMMODATION",
    ),
    // Any answer to PAST_AND_FUTURE_ACCOMMODATION, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "PAST_AND_FUTURE_ACCOMMODATION",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
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
      version = 2,
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
      currentPage = "PAST_AND_FUTURE_ACCOMMODATION",
      version = 2,
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
        version = 2,
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
        version = 2,
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
      version = 2,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "PAST_AND_FUTURE_ACCOMMODATION",
      ResettlementAssessmentResponsePage(
        id = "PAST_AND_FUTURE_ACCOMMODATION",
        title = "Accommodation report",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WHERE_DID_THEY_LIVE",
              title = "Where did the person in prison live before custody?",
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "PRIVATE_RENTED_HOUSING",
                  displayText = "Private rented housing",
                  tag = null,
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "PRIVATE_HOUSING_OWNED",
                  displayText = "Private housing owned by them",
                  tag = null,
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_HOUSING_OWNED",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "FAMILY_OR_FRIENDS",
                  displayText = "With family or friends",
                  tag = null,
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "SOCIAL_HOUSING",
                  displayText = "Social housing",
                  tag = null,
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_SOCIAL_HOUSING",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                  displayText = "Local authority care or supported housing",
                  tag = null,
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "HOSTEL",
                  displayText = "Hostel",
                  tag = null,
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_HOSTEL",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "APPROVED_PREMISES",
                  displayText = "Approved premises",
                  tag = null,
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_APPROVED_PREMISES",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "NO_PERMANENT_OR_FIXED",
                  displayText = "No permanent or fixed address",
                  tag = null,
                ),
                ResettlementAssessmentOption(
                  id = "NO_ANSWER",
                  displayText = "No answer provided",
                  tag = null,
                ),
              ),
            ),
            originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
              title = "Additional information",
              subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
              type = TypeOfQuestion.LONG_TEXT,
            ),
            originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WHERE_WILL_THEY_LIVE",
              title = "Where will the person in prison live when they are released?",
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "RETURN_TO_PREVIOUS_ADDRESS",
                  displayText = "Return to their previous address",
                  tag = null,
                ),
                ResettlementAssessmentOption(
                  id = "MOVE_TO_NEW_ADDRESS",
                  displayText = "Move to a new address",
                  tag = null,
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                    ),
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS",
                        title = "Additional information",
                        type = TypeOfQuestion.LONG_TEXT,
                      ),
                      originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "DOES_NOT_HAVE_ANYWHERE",
                  displayText = "Does not have anywhere to live",
                  tag = null,
                ),
                ResettlementAssessmentOption(
                  id = "NO_ANSWER",
                  displayText = "No answer provided",
                  tag = null,
                ),
              ),
            ),
            originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
              title = "Additional information",
              subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
              type = TypeOfQuestion.LONG_TEXT,
            ),
            originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
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
              id = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
              title = "Support needs",
              subTitle = "Select all that apply.",
              type = TypeOfQuestion.CHECKBOX,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "HELP_TO_FIND_ACCOMMODATION",
                  displayText = "Help to find accommodation",
                  tag = "NO_FIXED_ABODE",
                ),
                ResettlementAssessmentOption(
                  id = "HOME_ADAPTATIONS",
                  displayText = "Home adaptations",
                  tag = "HOME_ADAPTATIONS_POST_RELEASE",
                ),
                ResettlementAssessmentOption(
                  id = "HELP_TO_KEEP_HOME",
                  displayText = "Help to keep their home while in prison",
                  tag = "KEEP_THEIR_HOME",
                ),
                ResettlementAssessmentOption(
                  id = "HOMELESS_APPLICATION",
                  displayText = "Homeless application",
                ),
                ResettlementAssessmentOption(
                  id = "CANCEL_A_TENANCY",
                  displayText = "Cancel a tenancy",
                  tag = "CANCEL_TENANCY",
                ),
                ResettlementAssessmentOption(
                  id = "SET_UP_RENT_ARREARS",
                  displayText = "Set up rent arrears",
                  tag = "PAYMENT_FOR_RENT_ARREARS",
                ),
                ResettlementAssessmentOption(
                  id = "ARRANGE_STORAGE",
                  displayText = "Arrange storage for personal possessions",
                  tag = "ARRANGE_STORAGE_FOR_PERSONAL",
                ),
              ),
              validationType = ValidationType.OPTIONAL,
            ),
            originalPageId = "ASSESSMENT_SUMMARY",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Accommodation resettlement status",
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "SUPPORT_REQUIRED",
                  displayText = "Support required",
                  description = "a need for support has been identified and is accepted",
                  tag = null,
                ),
                ResettlementAssessmentOption(
                  id = "SUPPORT_NOT_REQUIRED",
                  displayText = "Support not required",
                  description = "no need was identified",
                  tag = null,
                ),
                ResettlementAssessmentOption(
                  id = "SUPPORT_DECLINED",
                  displayText = "Support declined",
                  description = "a need has been identified but support is declined",
                  tag = null,
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
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("FAMILY_OR_FRIENDS")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS", MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
        ResettlementAssessmentSimpleQuestionAndAnswer("ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE", StringAnswer("Some random additional text here")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE", StringAnswer("MOVE_TO_NEW_ADDRESS")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "45 Street Lane", "city" to "Bradford", "postcode" to "BD12 345")))),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS", StringAnswer("Some additional info on the new address")),
        ResettlementAssessmentSimpleQuestionAndAnswer("ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE", StringAnswer("Long text")),
      ),
    )

    setUpMocks("123", true, existingAssessment)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "PAST_AND_FUTURE_ACCOMMODATION",
      title = "Accommodation report",
      questionsAndAnswers = listOf(
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(
                id = "PRIVATE_RENTED_HOUSING",
                displayText = "Private rented housing",
                tag = null,
                nestedQuestions = listOf(
                  ResettlementAssessmentQuestionAndAnswer(
                    question = ResettlementAssessmentQuestion(
                      id = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING",
                      title = "Enter the address",
                      type = TypeOfQuestion.ADDRESS,
                    ),
                    originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                  ),
                ),
              ),
              ResettlementAssessmentOption(
                id = "PRIVATE_HOUSING_OWNED",
                displayText = "Private housing owned by them",
                tag = null,
                nestedQuestions = listOf(
                  ResettlementAssessmentQuestionAndAnswer(
                    question = ResettlementAssessmentQuestion(
                      id = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_HOUSING_OWNED",
                      title = "Enter the address",
                      type = TypeOfQuestion.ADDRESS,
                    ),
                    originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                  ),
                ),
              ),
              ResettlementAssessmentOption(
                id = "FAMILY_OR_FRIENDS",
                displayText = "With family or friends",
                tag = null,
                nestedQuestions = listOf(
                  ResettlementAssessmentQuestionAndAnswer(
                    question = ResettlementAssessmentQuestion(
                      id = "WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS",
                      title = "Enter the address",
                      type = TypeOfQuestion.ADDRESS,
                    ),
                    answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123"))),
                    originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                  ),
                ),
              ),
              ResettlementAssessmentOption(
                id = "SOCIAL_HOUSING",
                displayText = "Social housing",
                tag = null,
                nestedQuestions = listOf(
                  ResettlementAssessmentQuestionAndAnswer(
                    question = ResettlementAssessmentQuestion(
                      id = "WHERE_DID_THEY_LIVE_ADDRESS_SOCIAL_HOUSING",
                      title = "Enter the address",
                      type = TypeOfQuestion.ADDRESS,
                    ),
                    originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                  ),
                ),
              ),
              ResettlementAssessmentOption(
                id = "LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                displayText = "Local authority care or supported housing",
                tag = null,
                nestedQuestions = listOf(
                  ResettlementAssessmentQuestionAndAnswer(
                    question = ResettlementAssessmentQuestion(
                      id = "WHERE_DID_THEY_LIVE_ADDRESS_LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                      title = "Enter the address",
                      type = TypeOfQuestion.ADDRESS,
                    ),
                    originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                  ),
                ),
              ),
              ResettlementAssessmentOption(
                id = "HOSTEL",
                displayText = "Hostel",
                tag = null,
                nestedQuestions = listOf(
                  ResettlementAssessmentQuestionAndAnswer(
                    question = ResettlementAssessmentQuestion(
                      id = "WHERE_DID_THEY_LIVE_ADDRESS_HOSTEL",
                      title = "Enter the address",
                      type = TypeOfQuestion.ADDRESS,
                    ),
                    originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                  ),
                ),
              ),
              ResettlementAssessmentOption(
                id = "APPROVED_PREMISES",
                displayText = "Approved premises",
                tag = null,
                nestedQuestions = listOf(
                  ResettlementAssessmentQuestionAndAnswer(
                    question = ResettlementAssessmentQuestion(
                      id = "WHERE_DID_THEY_LIVE_ADDRESS_APPROVED_PREMISES",
                      title = "Enter the address",
                      type = TypeOfQuestion.ADDRESS,
                    ),
                    originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                  ),
                ),
              ),
              ResettlementAssessmentOption(
                id = "NO_PERMANENT_OR_FIXED",
                displayText = "No permanent or fixed address",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "NO_ANSWER",
                displayText = "No answer provided",
                tag = null,
              ),
            ),
          ),
          answer = StringAnswer("FAMILY_OR_FRIENDS"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            title = "Additional information",
            subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
            type = TypeOfQuestion.LONG_TEXT,
          ),
          answer = StringAnswer("Some random additional text here"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_WILL_THEY_LIVE",
            title = "Where will the person in prison live when they are released?",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(
                id = "RETURN_TO_PREVIOUS_ADDRESS",
                displayText = "Return to their previous address",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "MOVE_TO_NEW_ADDRESS",
                displayText = "Move to a new address",
                tag = null,
                nestedQuestions = listOf(
                  ResettlementAssessmentQuestionAndAnswer(
                    question = ResettlementAssessmentQuestion(
                      id = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
                      title = "Enter the address",
                      type = TypeOfQuestion.ADDRESS,
                    ),
                    answer = MapAnswer(listOf(mapOf("addressLine1" to "45 Street Lane", "city" to "Bradford", "postcode" to "BD12 345"))),
                    originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                  ),
                  ResettlementAssessmentQuestionAndAnswer(
                    question = ResettlementAssessmentQuestion(
                      id = "WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS",
                      title = "Additional information",
                      type = TypeOfQuestion.LONG_TEXT,
                    ),
                    answer = StringAnswer("Some additional info on the new address"),
                    originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
                  ),
                ),
              ),
              ResettlementAssessmentOption(
                id = "DOES_NOT_HAVE_ANYWHERE",
                displayText = "Does not have anywhere to live",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "NO_ANSWER",
                displayText = "No answer provided",
                tag = null,
              ),
            ),
          ),
          answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            title = "Additional information",
            subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
            type = TypeOfQuestion.LONG_TEXT,
          ),
          answer = StringAnswer("Long text"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
      ),
    )

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = "PAST_AND_FUTURE_ACCOMMODATION",
      version = 2,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  @Test
  fun `test get page from Id check answers - existing assessment`() {
    val nomsId = "123"

    val existingAssessment = ResettlementAssessmentQuestionAndAnswerList(
      listOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("FAMILY_OR_FRIENDS")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS", MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
        ResettlementAssessmentSimpleQuestionAndAnswer("ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE", StringAnswer("Some random additional text here")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE", StringAnswer("MOVE_TO_NEW_ADDRESS")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "45 Street Lane", "city" to "Bradford", "postcode" to "BD12 345")))),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS", StringAnswer("Some additional info on the new address")),
        ResettlementAssessmentSimpleQuestionAndAnswer("ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE", StringAnswer("Long text")),
        ResettlementAssessmentSimpleQuestionAndAnswer("ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES", ListAnswer(listOf("HELP_TO_FIND_ACCOMMODATION", "HELP_TO_KEEP_HOME", "SET_UP_RENT_ARREARS"))),
        ResettlementAssessmentSimpleQuestionAndAnswer("SUPPORT_NEEDS", StringAnswer("SUPPORT_REQUIRED")),
        ResettlementAssessmentSimpleQuestionAndAnswer("CASE_NOTE_SUMMARY", StringAnswer("This is a case note summary")),
      ),
    )

    setUpMocks("123", true, existingAssessment)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "CHECK_ANSWERS",
      questionsAndAnswers = listOf(
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(
                id = "PRIVATE_RENTED_HOUSING",
                displayText = "Private rented housing",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "PRIVATE_HOUSING_OWNED",
                displayText = "Private housing owned by them",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "FAMILY_OR_FRIENDS",
                displayText = "With family or friends",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "SOCIAL_HOUSING",
                displayText = "Social housing",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                displayText = "Local authority care or supported housing",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "HOSTEL",
                displayText = "Hostel",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "APPROVED_PREMISES",
                displayText = "Approved premises",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "NO_PERMANENT_OR_FIXED",
                displayText = "No permanent or fixed address",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "NO_ANSWER",
                displayText = "No answer provided",
                tag = null,
              ),
            ),
          ),
          answer = StringAnswer("FAMILY_OR_FRIENDS"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS",
            title = "Enter the address",
            type = TypeOfQuestion.ADDRESS,
          ),
          answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123"))),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            title = "Additional information",
            subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
            type = TypeOfQuestion.LONG_TEXT,
          ),
          answer = StringAnswer("Some random additional text here"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_WILL_THEY_LIVE",
            title = "Where will the person in prison live when they are released?",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(
                id = "RETURN_TO_PREVIOUS_ADDRESS",
                displayText = "Return to their previous address",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "MOVE_TO_NEW_ADDRESS",
                displayText = "Move to a new address",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "DOES_NOT_HAVE_ANYWHERE",
                displayText = "Does not have anywhere to live",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "NO_ANSWER",
                displayText = "No answer provided",
                tag = null,
              ),
            ),
          ),
          answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
            title = "Enter the address",
            type = TypeOfQuestion.ADDRESS,
          ),
          answer = MapAnswer(listOf(mapOf("addressLine1" to "45 Street Lane", "city" to "Bradford", "postcode" to "BD12 345"))),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS",
            title = "Additional information",
            type = TypeOfQuestion.LONG_TEXT,
          ),
          answer = StringAnswer("Some additional info on the new address"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            title = "Additional information",
            subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
            type = TypeOfQuestion.LONG_TEXT,
          ),
          answer = StringAnswer("Long text"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
            title = "Support needs",
            subTitle = "Select all that apply.",
            type = TypeOfQuestion.CHECKBOX,
            options = listOf(
              ResettlementAssessmentOption(
                id = "HELP_TO_FIND_ACCOMMODATION",
                displayText = "Help to find accommodation",
                tag = "NO_FIXED_ABODE",
              ),
              ResettlementAssessmentOption(
                id = "HOME_ADAPTATIONS",
                displayText = "Home adaptations",
                tag = "HOME_ADAPTATIONS_POST_RELEASE",
              ),
              ResettlementAssessmentOption(
                id = "HELP_TO_KEEP_HOME",
                displayText = "Help to keep their home while in prison",
                tag = "KEEP_THEIR_HOME",
              ),
              ResettlementAssessmentOption(
                id = "HOMELESS_APPLICATION",
                displayText = "Homeless application",
              ),
              ResettlementAssessmentOption(
                id = "CANCEL_A_TENANCY",
                displayText = "Cancel a tenancy",
                tag = "CANCEL_TENANCY",
              ),
              ResettlementAssessmentOption(
                id = "SET_UP_RENT_ARREARS",
                displayText = "Set up rent arrears",
                tag = "PAYMENT_FOR_RENT_ARREARS",
              ),
              ResettlementAssessmentOption(
                id = "ARRANGE_STORAGE",
                displayText = "Arrange storage for personal possessions",
                tag = "ARRANGE_STORAGE_FOR_PERSONAL",
              ),
            ),
            validationType = ValidationType.OPTIONAL,
          ),
          answer = ListAnswer(listOf("HELP_TO_FIND_ACCOMMODATION", "HELP_TO_KEEP_HOME", "SET_UP_RENT_ARREARS")),
          originalPageId = "ASSESSMENT_SUMMARY",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "SUPPORT_NEEDS",
            title = "Accommodation resettlement status",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(
                id = "SUPPORT_REQUIRED",
                displayText = "Support required",
                tag = null,
                description = "a need for support has been identified and is accepted",
              ),
              ResettlementAssessmentOption(
                id = "SUPPORT_NOT_REQUIRED",
                displayText = "Support not required",
                tag = null,
                description = "no need was identified",
              ),
              ResettlementAssessmentOption(
                id = "SUPPORT_DECLINED",
                displayText = "Support declined",
                tag = null,
                description = "a need has been identified but support is declined",
              ),
            ),
          ),
          answer = StringAnswer("SUPPORT_REQUIRED"),
          originalPageId = "ASSESSMENT_SUMMARY",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "CASE_NOTE_SUMMARY",
            title = "Case note summary",
            subTitle = "This will be displayed as a case note in both DPS and nDelius",
            type = TypeOfQuestion.LONG_TEXT,
          ),
          answer = StringAnswer("This is a case note summary"),
          originalPageId = "ASSESSMENT_SUMMARY",
        ),
      ),
    )

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = "CHECK_ANSWERS",
      version = 2,
    )
    org.assertj.core.api.Assertions.assertThat(page).usingRecursiveComparison().isEqualTo(expectedPage)
  }

  @Test
  fun `test get page from Id check answers - existing submitted assessment`() {
    val nomsId = "123"

    val existingAssessment = ResettlementAssessmentQuestionAndAnswerList(
      listOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("FAMILY_OR_FRIENDS")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS", MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
        ResettlementAssessmentSimpleQuestionAndAnswer("ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE", StringAnswer("Some random additional text here")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE", StringAnswer("MOVE_TO_NEW_ADDRESS")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "45 Street Lane", "city" to "Bradford", "postcode" to "BD12 345")))),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS", StringAnswer("Some additional info on the new address")),
        ResettlementAssessmentSimpleQuestionAndAnswer("ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE", StringAnswer("Long text")),
        ResettlementAssessmentSimpleQuestionAndAnswer("ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES", ListAnswer(listOf("HELP_TO_FIND_ACCOMMODATION", "HELP_TO_KEEP_HOME", "SET_UP_RENT_ARREARS"))),
        ResettlementAssessmentSimpleQuestionAndAnswer("SUPPORT_NEEDS", StringAnswer("SUPPORT_REQUIRED")),
        ResettlementAssessmentSimpleQuestionAndAnswer("CASE_NOTE_SUMMARY", StringAnswer("This is a case note summary")),
      ),
    )

    setUpMocks("123", true, existingAssessment, ResettlementAssessmentStatus.SUBMITTED)

    val expectedPage = ResettlementAssessmentResponsePage(
      id = "CHECK_ANSWERS",
      questionsAndAnswers = listOf(
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(
                id = "PRIVATE_RENTED_HOUSING",
                displayText = "Private rented housing",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "PRIVATE_HOUSING_OWNED",
                displayText = "Private housing owned by them",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "FAMILY_OR_FRIENDS",
                displayText = "With family or friends",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "SOCIAL_HOUSING",
                displayText = "Social housing",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                displayText = "Local authority care or supported housing",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "HOSTEL",
                displayText = "Hostel",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "APPROVED_PREMISES",
                displayText = "Approved premises",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "NO_PERMANENT_OR_FIXED",
                displayText = "No permanent or fixed address",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "NO_ANSWER",
                displayText = "No answer provided",
                tag = null,
              ),
            ),
          ),
          answer = StringAnswer("FAMILY_OR_FRIENDS"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS",
            title = "Enter the address",
            type = TypeOfQuestion.ADDRESS,
          ),
          answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123"))),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            title = "Additional information",
            subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
            type = TypeOfQuestion.LONG_TEXT,
          ),
          answer = StringAnswer("Some random additional text here"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_WILL_THEY_LIVE",
            title = "Where will the person in prison live when they are released?",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              ResettlementAssessmentOption(
                id = "RETURN_TO_PREVIOUS_ADDRESS",
                displayText = "Return to their previous address",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "MOVE_TO_NEW_ADDRESS",
                displayText = "Move to a new address",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "DOES_NOT_HAVE_ANYWHERE",
                displayText = "Does not have anywhere to live",
                tag = null,
              ),
              ResettlementAssessmentOption(
                id = "NO_ANSWER",
                displayText = "No answer provided",
                tag = null,
              ),
            ),
          ),
          answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
            title = "Enter the address",
            type = TypeOfQuestion.ADDRESS,
          ),
          answer = MapAnswer(listOf(mapOf("addressLine1" to "45 Street Lane", "city" to "Bradford", "postcode" to "BD12 345"))),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS",
            title = "Additional information",
            type = TypeOfQuestion.LONG_TEXT,
          ),
          answer = StringAnswer("Some additional info on the new address"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
        ResettlementAssessmentQuestionAndAnswer(
          question = ResettlementAssessmentQuestion(
            id = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            title = "Additional information",
            subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
            type = TypeOfQuestion.LONG_TEXT,
          ),
          answer = StringAnswer("Long text"),
          originalPageId = "PAST_AND_FUTURE_ACCOMMODATION",
        ),
      ),
    )

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = "CHECK_ANSWERS",
      version = 2,
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
        resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
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
      resettlementAssessmentStrategy.completeAssessment(nomsId, pathway, assessmentType, assessment, "string", true)
      Mockito.verify(resettlementAssessmentRepository).save(expectedEntity!!)
    } else {
      val actualException = assertThrows<Throwable> {
        resettlementAssessmentStrategy.completeAssessment(nomsId, pathway, assessmentType, assessment, "string", true)
      }
      Assertions.assertEquals(expectedException::class, actualException::class)
      Assertions.assertEquals(expectedException.message, actualException.message)
    }

    unmockkAll()
  }

  private fun `test complete assessment data`() = Stream.of(
    // Happy path - BCST2 and no existing assessment
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 2,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            answer = StringAnswer("Some extra info here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
            answer = ListAnswer(listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE")),
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
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "Some extra info here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "My case note summary...", createdByUserId = "USER_1", version = 2, submissionDate = null, userDeclaration = true),
      null,
      null,
    ),
    // Happy path - RESETTLEMENT_PLAN and no existing assessment
    Arguments.of(
      ResettlementAssessmentType.RESETTLEMENT_PLAN,
      ResettlementAssessmentCompleteRequest(
        version = 2,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            answer = StringAnswer("Some extra info here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
            answer = ListAnswer(listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE")),
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
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "Some extra info here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS_PRERELEASE", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "My case note summary...", createdByUserId = "USER_1", version = 2, submissionDate = null, userDeclaration = true),
      null,
      null,
    ),
    // Happy path - existing COMPLETE assessment
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 2,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            answer = StringAnswer("Some extra info here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
            answer = ListAnswer(listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE")),
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
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "Some extra info here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "My case note summary...", createdByUserId = "USER_1", version = 2, submissionDate = null, userDeclaration = true),
      null,
      ResettlementAssessmentEntity(id = 12, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "Some extra info here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-15T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "My case note summary...", createdByUserId = "USER_1", version = 2, submissionDate = null, userDeclaration = true),
    ),
    // Happy path - existing SUBMITTED assessment
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 2,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            answer = StringAnswer("Some extra info here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("Some more information here"),
          ),
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = null, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "Some extra info here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "Some more information here")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "USER_1", version = 2, submissionDate = LocalDateTime.parse("2023-08-16T12:00:00"), userDeclaration = true),
      null,
      ResettlementAssessmentEntity(id = 12, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "Some extra info here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "NO_ANSWER")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2023-08-15T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "My case note summary...", createdByUserId = "USER_1", version = 2, submissionDate = null, userDeclaration = true),
    ),
  )

  @ParameterizedTest
  @MethodSource("test findPageIdFromQuestionId data")
  fun `test findPageIdFromQuestionId`(questionId: String, expectedPageId: String) {
    Assertions.assertEquals(expectedPageId, resettlementAssessmentStrategy.findPageIdFromQuestionId(questionId, ResettlementAssessmentType.BCST2, Pathway.ACCOMMODATION, 2))
  }

  private fun `test findPageIdFromQuestionId data`() = Stream.of(
    Arguments.of("WHERE_DID_THEY_LIVE", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_HOUSING_OWNED", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("WHERE_DID_THEY_LIVE_ADDRESS_SOCIAL_HOUSING", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("WHERE_DID_THEY_LIVE_ADDRESS_LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("WHERE_DID_THEY_LIVE_ADDRESS_HOSTEL", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("WHERE_DID_THEY_LIVE_ADDRESS_APPROVED_PREMISES", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("WHERE_WILL_THEY_LIVE", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE", "PAST_AND_FUTURE_ACCOMMODATION"),
    Arguments.of("ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES", "ASSESSMENT_SUMMARY"),
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
    // Happy path 1 - correct full set of questions asked - no nested
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        version = 2,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            answer = StringAnswer("Some extra info here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
            answer = ListAnswer(listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE")),
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
    // Happy path 1a - correct full set of questions asked - no nested - different order
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        version = 2,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            answer = StringAnswer("Some extra info here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "CASE_NOTE_SUMMARY",
            answer = StringAnswer("My case note summary..."),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
            answer = ListAnswer(listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE")),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("Some more information here"),
          ),
        ),
      ),
      true,
    ),
    // Happy path 2 - correct full set of questions - with nested
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        version = 2,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("PRIVATE_HOUSING_OWNED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_HOUSING_OWNED",
            answer = MapAnswer(listOf(mapOf("addressLine1" to "123 Fake Street"))),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            answer = StringAnswer("Some extra info here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
            answer = MapAnswer(listOf(mapOf("addressLine1" to "124 Fake Street"))),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS",
            answer = StringAnswer("This is some additional info"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
            answer = ListAnswer(listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE")),
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
    // Happy path 3 - correct full set of questions - with nested - optional questions null
    Arguments.of(
      ResettlementAssessmentCompleteRequest(
        version = 2,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("PRIVATE_HOUSING_OWNED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_HOUSING_OWNED",
            answer = MapAnswer(listOf(mapOf("addressLine1" to "123 Fake Street"))),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            answer = StringAnswer("Some extra info here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
            answer = MapAnswer(listOf(mapOf("addressLine1" to "124 Fake Street"))),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS",
            answer = StringAnswer("This is some additional info"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
            answer = ListAnswer(),
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
    // TODO PSFR-1545 once validation is complete, uncomment this and it should start passing
    // Error case - nested question not answered
//    Arguments.of(
//      ResettlementAssessmentCompleteRequest(
//        version = 2,
//        questionsAndAnswers = listOf(
//          ResettlementAssessmentRequestQuestionAndAnswer(
//            question = "WHERE_DID_THEY_LIVE",
//            answer = StringAnswer("PRIVATE_HOUSING_OWNED"),
//          ),
//          ResettlementAssessmentRequestQuestionAndAnswer(
//            question = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
//            answer = StringAnswer("Some extra info here"),
//          ),
//          ResettlementAssessmentRequestQuestionAndAnswer(
//            question = "WHERE_WILL_THEY_LIVE",
//            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
//          ),
//          ResettlementAssessmentRequestQuestionAndAnswer(
//            question = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
//            answer = MapAnswer(listOf(mapOf("addressLine1" to "124 Fake Street"))),
//          ),
//          ResettlementAssessmentRequestQuestionAndAnswer(
//            question = "WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS",
//            answer = StringAnswer("This is some additional info"),
//          ),
//          ResettlementAssessmentRequestQuestionAndAnswer(
//            question = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
//            answer = StringAnswer("Some more information here"),
//          ),
//          ResettlementAssessmentRequestQuestionAndAnswer(
//            question = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
//            answer = ListAnswer(),
//          ),
//          ResettlementAssessmentRequestQuestionAndAnswer(
//            question = "SUPPORT_NEEDS",
//            answer = StringAnswer("SUPPORT_REQUIRED"),
//          ),
//          ResettlementAssessmentRequestQuestionAndAnswer(
//            question = "CASE_NOTE_SUMMARY",
//            answer = StringAnswer("My case note summary..."),
//          ),
//        ),
//      ),
//      false,
//    ),
  )

  @Test
  fun `test get config for v2 report`() {
    val expectedPages = listOf(
      AssessmentConfigPage(
        id = "PAST_AND_FUTURE_ACCOMMODATION",
        title = "Accommodation report",
        questions = listOf(
          AssessmentConfigQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              AssessmentConfigOption(
                id = "PRIVATE_RENTED_HOUSING",
                displayText = "Private rented housing",
                tag = null,
                nestedQuestions = listOf(
                  AssessmentConfigQuestion(
                    id = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING",
                    title = "Enter the address",
                    type = TypeOfQuestion.ADDRESS,
                  ),
                ),
              ),
              AssessmentConfigOption(
                id = "PRIVATE_HOUSING_OWNED",
                displayText = "Private housing owned by them",
                nestedQuestions = listOf(
                  AssessmentConfigQuestion(
                    id = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_HOUSING_OWNED",
                    title = "Enter the address",
                    type = TypeOfQuestion.ADDRESS,
                  ),
                ),
              ),
              AssessmentConfigOption(
                id = "FAMILY_OR_FRIENDS",
                displayText = "With family or friends",
                nestedQuestions = listOf(
                  AssessmentConfigQuestion(
                    id = "WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS",
                    title = "Enter the address",
                    type = TypeOfQuestion.ADDRESS,
                  ),
                ),
              ),
              AssessmentConfigOption(
                id = "SOCIAL_HOUSING",
                displayText = "Social housing",
                nestedQuestions = listOf(
                  AssessmentConfigQuestion(
                    id = "WHERE_DID_THEY_LIVE_ADDRESS_SOCIAL_HOUSING",
                    title = "Enter the address",
                    type = TypeOfQuestion.ADDRESS,
                  ),
                ),
              ),
              AssessmentConfigOption(
                id = "LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                displayText = "Local authority care or supported housing",
                nestedQuestions = listOf(
                  AssessmentConfigQuestion(
                    id = "WHERE_DID_THEY_LIVE_ADDRESS_LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                    title = "Enter the address",
                    type = TypeOfQuestion.ADDRESS,
                  ),
                ),
              ),
              AssessmentConfigOption(
                id = "HOSTEL",
                displayText = "Hostel",
                nestedQuestions = listOf(
                  AssessmentConfigQuestion(
                    id = "WHERE_DID_THEY_LIVE_ADDRESS_HOSTEL",
                    title = "Enter the address",
                    type = TypeOfQuestion.ADDRESS,
                  ),
                ),
              ),
              AssessmentConfigOption(
                id = "APPROVED_PREMISES",
                displayText = "Approved premises",
                nestedQuestions = listOf(
                  AssessmentConfigQuestion(
                    id = "WHERE_DID_THEY_LIVE_ADDRESS_APPROVED_PREMISES",
                    title = "Enter the address",
                    type = TypeOfQuestion.ADDRESS,
                  ),
                ),
              ),
              AssessmentConfigOption(
                id = "NO_PERMANENT_OR_FIXED",
                displayText = "No permanent or fixed address",
              ),
              AssessmentConfigOption(
                id = "NO_ANSWER",
                displayText = "No answer provided",
              ),
            ),
          ),
          AssessmentConfigQuestion(
            id = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            title = "Additional information",
            subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
            type = TypeOfQuestion.LONG_TEXT,
          ),
          AssessmentConfigQuestion(
            id = "WHERE_WILL_THEY_LIVE",
            title = "Where will the person in prison live when they are released?",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              AssessmentConfigOption(
                id = "RETURN_TO_PREVIOUS_ADDRESS",
                displayText = "Return to their previous address",
              ),
              AssessmentConfigOption(
                id = "MOVE_TO_NEW_ADDRESS",
                displayText = "Move to a new address",
                nestedQuestions = listOf(
                  AssessmentConfigQuestion(
                    id = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
                    title = "Enter the address",
                    type = TypeOfQuestion.ADDRESS,
                  ),
                  AssessmentConfigQuestion(
                    id = "WHERE_WILL_THEY_LIVE_ADDRESS_ADDITIONAL_INFO_MOVE_TO_NEW_ADDRESS",
                    title = "Additional information",
                    type = TypeOfQuestion.LONG_TEXT,
                  ),
                ),
              ),
              AssessmentConfigOption(
                id = "DOES_NOT_HAVE_ANYWHERE",
                displayText = "Does not have anywhere to live",
              ),
              AssessmentConfigOption(
                id = "NO_ANSWER",
                displayText = "No answer provided",
              ),
            ),
          ),
          AssessmentConfigQuestion(
            id = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            title = "Additional information",
            subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
            type = TypeOfQuestion.LONG_TEXT,
          ),
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            nextPageId = "FINAL_QUESTION_NEXT_PAGE",
          ),
        ),
      ),
      AssessmentConfigPage(
        id = "ASSESSMENT_SUMMARY",
        title = "Accommodation report summary",
        questions = listOf(
          AssessmentConfigQuestion(
            id = "ACCOMMODATION_SUPPORT_NEEDS_CHECKBOXES",
            title = "Support needs",
            subTitle = "Select all that apply.",
            type = TypeOfQuestion.CHECKBOX,
            options = listOf(
              AssessmentConfigOption(
                id = "HELP_TO_FIND_ACCOMMODATION",
                displayText = "Help to find accommodation",
                tag = "NO_FIXED_ABODE",
              ),
              AssessmentConfigOption(
                id = "HOME_ADAPTATIONS",
                displayText = "Home adaptations",
                tag = "HOME_ADAPTATIONS_POST_RELEASE",
              ),
              AssessmentConfigOption(
                id = "HELP_TO_KEEP_HOME",
                displayText = "Help to keep their home while in prison",
                tag = "KEEP_THEIR_HOME",
              ),
              AssessmentConfigOption(
                id = "HOMELESS_APPLICATION",
                displayText = "Homeless application",
              ),
              AssessmentConfigOption(
                id = "CANCEL_A_TENANCY",
                displayText = "Cancel a tenancy",
                tag = "CANCEL_TENANCY",
              ),
              AssessmentConfigOption(
                id = "SET_UP_RENT_ARREARS",
                displayText = "Set up rent arrears",
                tag = "PAYMENT_FOR_RENT_ARREARS",
              ),
              AssessmentConfigOption(
                id = "ARRANGE_STORAGE",
                displayText = "Arrange storage for personal possessions",
                tag = "ARRANGE_STORAGE_FOR_PERSONAL",
              ),
            ),
            validationType = ValidationType.OPTIONAL,
          ),
          AssessmentConfigQuestion(
            id = "SUPPORT_NEEDS",
            title = "Accommodation resettlement status",
            type = TypeOfQuestion.RADIO,
            options = listOf(
              AssessmentConfigOption(
                id = "SUPPORT_REQUIRED",
                displayText = "Support required",
                description = "a need for support has been identified and is accepted",
              ),
              AssessmentConfigOption(
                id = "SUPPORT_NOT_REQUIRED",
                displayText = "Support not required",
                description = "no need was identified",
              ),
              AssessmentConfigOption(
                id = "SUPPORT_DECLINED",
                displayText = "Support declined",
                description = "a need has been identified but support is declined",
              ),
            ),
          ),
          AssessmentConfigQuestion(
            id = "CASE_NOTE_SUMMARY",
            title = "Case note summary",
            subTitle = "This will be displayed as a case note in both DPS and nDelius",
            type = TypeOfQuestion.LONG_TEXT,
          ),
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            nextPageId = "CHECK_ANSWERS",
          ),
        ),
      ),
      AssessmentConfigPage(
        id = "CHECK_ANSWERS",
        questions = null,
        nextPageLogic = null,
      ),
    )

    val expectedQuestionSet = AssessmentQuestionSet(
      version = 2,
      pathway = Pathway.ACCOMMODATION,
      pages = expectedPages,
    )

    val assessmentQuestionSet = resettlementAssessmentStrategy.getConfig(Pathway.ACCOMMODATION, ResettlementAssessmentType.BCST2, version = 2)
    Assertions.assertEquals(expectedQuestionSet, assessmentQuestionSet)
  }
}
