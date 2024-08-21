package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.util.stream.Stream

class AccommodationV3ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.ACCOMMODATION) {

  @ParameterizedTest(name = "{1} -> {2}")
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
      version = 3,
    )
    Assertions.assertEquals(expectedPage, nextPage)
  }

  private fun `test next page function flow - no existing assessment data`() = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "ACCOMMODATION_REPORT",
    ),
    // Any answer to ACCOMMODATION_REPORT, go to SUPPORT_REQUIREMENTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "ACCOMMODATION_REPORT",
      "SUPPORT_REQUIREMENTS",
    ),
    // Any answer to SUPPORT_REQUIREMENTS, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "SUPPORT_REQUIREMENTS",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  @ParameterizedTest(name = "{0} page")
  @MethodSource("test get page from Id - no existing assessment data")
  fun `test get page from Id - no existing assessment`(pageIdInput: String, expectedPage: ResettlementAssessmentResponsePage) {
    val nomsId = "123"
    setUpMocks("123", false)

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
      version = 3,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "ACCOMMODATION_REPORT",
      ResettlementAssessmentResponsePage(
        id = "ACCOMMODATION_REPORT",
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
                  displayText = "Private housing rented by them",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "ACCOMMODATION_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "PRIVATE_HOUSING_OWNED",
                  displayText = "Private housing owned by them",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_HOUSING_OWNED",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "ACCOMMODATION_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "FAMILY_OR_FRIENDS",
                  displayText = "With family or friends",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "ACCOMMODATION_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "SOCIAL_HOUSING",
                  displayText = "Social housing",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_SOCIAL_HOUSING",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "ACCOMMODATION_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                  displayText = "Local authority care or supported housing",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "ACCOMMODATION_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "HOSTEL",
                  displayText = "Hostel",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_HOSTEL",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "ACCOMMODATION_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "EMERGENCY_HOUSING",
                  displayText = "Emergency housing from the council",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_EMERGENCY_HOUSING",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "ACCOMMODATION_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "APPROVED_PREMISES",
                  displayText = "Community accommodation, including approved premises, CAS2 and CAS3",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_APPROVED_PREMISES",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "ACCOMMODATION_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "IMMIGRATION_ACCOMMODATION",
                  displayText = "Immigration accommodation provided by the Home Office",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_DID_THEY_LIVE_ADDRESS_IMMIGRATION_ACCOMMODATION",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "ACCOMMODATION_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "NO_PERMANENT_OR_FIXED",
                  displayText = "No permanent or fixed address",
                ),
                ResettlementAssessmentOption(
                  id = "NO_ANSWER",
                  displayText = "No answer provided",
                ),
              ),
            ),
            originalPageId = "ACCOMMODATION_REPORT",
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
                ),
                ResettlementAssessmentOption(
                  id = "MOVE_TO_NEW_ADDRESS",
                  displayText = "Move to a new address",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
                        title = "Enter the address",
                        type = TypeOfQuestion.ADDRESS,
                      ),
                      originalPageId = "ACCOMMODATION_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "DOES_NOT_HAVE_ANYWHERE",
                  displayText = "Does not have anywhere to live",
                ),
                ResettlementAssessmentOption(
                  id = "NO_ANSWER",
                  displayText = "No answer provided",
                ),
              ),
            ),
            originalPageId = "ACCOMMODATION_REPORT",
          ),
        ),
      ),
    ),
    Arguments.of(
      "SUPPORT_REQUIREMENTS",
      ResettlementAssessmentResponsePage(
        id = "SUPPORT_REQUIREMENTS",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_REQUIREMENTS",
              title = "Support needs",
              subTitle = "Select any needs you have identified that could be met by prison or probation staff.",
              type = TypeOfQuestion.CHECKBOX,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "HELP_TO_FIND_ACCOMMODATION",
                  displayText = "Help to find accommodation",
                ),
                ResettlementAssessmentOption(
                  id = "HOME_ADAPTATIONS",
                  displayText = "Home adaptations",
                ),
                ResettlementAssessmentOption(
                  id = "HELP_TO_KEEP_HOME",
                  displayText = "Help to keep their home while in prison",
                ),
                ResettlementAssessmentOption(
                  id = "HOMELESS_APPLICATION",
                  displayText = "Homeless application",
                ),
                ResettlementAssessmentOption(
                  id = "CANCEL_A_TENANCY",
                  displayText = "Cancel a tenancy",
                ),
                ResettlementAssessmentOption(
                  id = "SET_UP_RENT_ARREARS",
                  displayText = "Set up payment for rent arrears",
                ),
                ResettlementAssessmentOption(
                  id = "ARRANGE_STORAGE",
                  displayText = "Arrange storage for personal possessions",
                ),
                ResettlementAssessmentOption(
                  id = "NO_SUPPORT_NEEDS",
                  displayText = "No support needs identified",
                  exclusive = true,
                ),
              ),
            ),
            originalPageId = "SUPPORT_REQUIREMENTS",
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
              title = "Accommodation resettlement status",
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
              title = "Case note",
              subTitle = "Include any relevant information about why you have chosen that resettlement status. This information will only be displayed in PSfR. Do not include any information that could identify anyone other than the person in prison, or any  special category data.",
              type = TypeOfQuestion.LONG_TEXT,
              detailsTitle = "Help with special category data",
              detailsContent = "Special category data includes any personal data concerning someone's health, sex life or sexual orientation. Or any personal data revealing someone's racial or ethnic origin, religious or philosophical beliefs or trade union membership.",
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
}
