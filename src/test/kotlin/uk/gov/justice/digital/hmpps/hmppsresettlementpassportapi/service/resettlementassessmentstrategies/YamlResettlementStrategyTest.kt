package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResettlementAssessmentConfig
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
open class YamlResettlementStrategyTest {
  lateinit var resettlementAssessmentService: YamlResettlementAssessmentStrategy

  @Mock
  lateinit var prisonerRepository: PrisonerRepository

  @Mock
  lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Mock
  lateinit var pathwayStatusRepository: PathwayStatusRepository

  val testDate: LocalDateTime = LocalDateTime.parse("2023-08-16T12:00:00")

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentService = YamlResettlementAssessmentStrategy(
      getTestConfig(),
      resettlementAssessmentRepository,
      prisonerRepository,
      pathwayStatusRepository,
    )
  }

  private fun getTestConfig() = ResettlementAssessmentConfig().assessmentQuestionSets(
    PathMatchingResourcePatternResolver(),
  )

  @ParameterizedTest(name = "{3} -> {4}")
  @MethodSource("test getNextPageId data")
  fun `test getNextPageId`(
    assessment: ResettlementAssessmentRequest,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    currentPage: String?,
    expectedNextPage: String,
  ) {
    val nomsId = "123"
    setUpMocks(nomsId, false)

    // handling CHECK_ANSWERS scenario
    if (currentPage == "CHECK_ANSWERS") {
      Assertions.assertThrows(ServerWebInputException::class.java) {
        resettlementAssessmentService.getNextPageId(
          assessment = assessment,
          nomsId = nomsId,
          pathway = pathway,
          assessmentType = assessmentType,
          currentPage = currentPage,
        )
      }
      return
    }

    val nextPageId = resettlementAssessmentService.getNextPageId(
      assessment = assessment,
      nomsId = nomsId,
      pathway = pathway,
      assessmentType = assessmentType,
      currentPage = currentPage,
    )

    Assertions.assertEquals(expectedNextPage, nextPageId)
  }

  private fun `test getNextPageId data`() = Stream.of(
    Arguments.of(
      ResettlementAssessmentRequest(emptyList()),
      Pathway.ACCOMMODATION,
      ResettlementAssessmentType.BCST2,
      null,
      "WHERE_DID_THEY_LIVE",
    ),
    Arguments.of(
      ResettlementAssessmentRequest(emptyList()),
      Pathway.ACCOMMODATION,
      ResettlementAssessmentType.BCST2,
      "CHECK_ANSWERS",
      "Cannot get the next question from CHECK_ANSWERS as this is the end of the flow for this pathway.",
    ),
    Arguments.of(
      ResettlementAssessmentRequest(
        listOf(
          ResettlementAssessmentRequestQuestionAndAnswer("Sample Question", StringAnswer("Sample Answer")),
        ),
      ),
      Pathway.ACCOMMODATION,
      ResettlementAssessmentType.BCST2,
      "WHERE_DID_THEY_LIVE_ADDRESS",
      "HELP_TO_KEEP_HOME",
    ),
  )

  private fun setUpMocks(nomsId: String, returnResettlementAssessmentEntity: Boolean, assessment: ResettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentQuestionAndAnswerList(mutableListOf())) {
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val resettlementAssessmentEntity = if (returnResettlementAssessmentEntity) ResettlementAssessmentEntity(1, prisonerEntity, Pathway.ACCOMMODATION, Status.NOT_STARTED, ResettlementAssessmentType.BCST2, assessment, testDate, "", ResettlementAssessmentStatus.COMPLETE, "some text", "USER_1", submissionDate = null, version = 1) else null
    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(
      resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        prisonerEntity,
        Pathway.ACCOMMODATION,
        ResettlementAssessmentType.BCST2,
        listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
      ),
    ).thenReturn(resettlementAssessmentEntity)
  }

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
              Option(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
              Option(id = "SOCIAL_HOUSING", displayText = "Social housing"),
              Option(id = "HOMEOWNER", displayText = "Homeowner"),
              Option(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
              Option(id = "NO_ANSWER", displayText = "No answer provided"),
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
              Option(id = "YES", displayText = "Yes"),
              Option(id = "NO", displayText = "No"),
              Option(id = "NO_ANSWER", displayText = "No answer provided"),
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
              Option(id = "RETURN_TO_PREVIOUS_ADDRESS", displayText = "Return to their previous address"),
              Option(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
              Option(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
              Option(id = "NO_ANSWER", displayText = "No answer provided"),
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
              Option(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
              Option(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
              Option(id = "NO_ANSWER", displayText = "No answer provided"),
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
        title = null,
        questions = listOf(
          AssessmentConfigQuestion(
            id = "SUPPORT_NEEDS",
            title = "",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              Option(id = "SUPPORT_REQUIRED", displayText = "Support required", description = "a need for support has been identified and is accepted"),
              Option(id = "SUPPORT_NOT_REQUIRED", displayText = "Support not required", description = "no need was identified"),
              Option(id = "SUPPORT_DECLINED", displayText = "Support declined", description = "a need has been identified but support is declined"),
            ),
          ),
          AssessmentConfigQuestion(
            id = "CASE_NOTE_SUMMARY",
            title = "Add a case note summary",
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
      generic = false,
      pathway = Pathway.ACCOMMODATION,
      genericAssessmentVersion = 1,
      pages = expectedPages,
    )

    val assessmentQuestionSet = resettlementAssessmentService.getConfig(Pathway.ACCOMMODATION, ResettlementAssessmentType.BCST2, version = 1)
    Assertions.assertNotNull(assessmentQuestionSet)
    Assertions.assertEquals(expectedQuestionSet, assessmentQuestionSet)
  }

  @Test
  fun `test get config for specific version`() {
    val expectedPages = listOf(
      AssessmentConfigPage(
        id = "PAST_AND_FUTURE_ACCOMMODATION",
        title = null,
        questions = listOf(
          AssessmentConfigQuestion(
            id = "WHERE_DID_THEY_LIVE",
            title = "Where did the person in prison live before custody?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              Option(id = "PRIVATE_RENTED_HOUSING", displayText = "Private rented housing"),
              Option(id = "PRIVATE_HOUSING_OWNED", displayText = "Private housing owned by them"),
              Option(id = "FAMILY_OR_FRIENDS", displayText = "With family or friends"),
              Option(id = "SOCIAL_HOUSING", displayText = "Social housing"),
              Option(id = "LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING", displayText = "Local authority care or supported housing"),
              Option(id = "HOSTEL", displayText = "Hostel"),
              Option(id = "APPROVED_PREMISES", displayText = "Approved premises"),
              Option(id = "NO_PERMANENT_OR_FIXED", displayText = "No permanent or fixed address"),
              Option(id = "NO_ANSWER", displayText = "No answer provided")
            )
          ),
          AssessmentConfigQuestion(
            id = "ADDITIONAL_INFORMATION_WHERE_DID_THEY_LIVE",
            title = "Additional information",
            subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
            type = TypeOfQuestion.LONG_TEXT,
            options = null
          ),
          AssessmentConfigQuestion(
            id = "WHERE_WILL_THEY_LIVE",
            title = "Where will the person in prison live when they are released?",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              Option(id = "RETURN_TO_PREVIOUS_ADDRESS", displayText = "Return to their previous address"),
              Option(id = "MOVE_TO_NEW_ADDRESS", displayText = "Move to a new address"),
              Option(id = "DOES_NOT_HAVE_ANYWHERE", displayText = "Does not have anywhere to live"),
              Option(id = "NO_ANSWER", displayText = "No answer provided")
            )
          ),
          AssessmentConfigQuestion(
            id = "ADDITIONAL_INFORMATION_WHERE_WILL_THEY_LIVE",
            title = "Additional information",
            subTitle = "Include details of who else lived at the address and how the accommodation was paid for. If no fixed address, specify the council area where they have a local connection.",
            type = TypeOfQuestion.LONG_TEXT,
            options = null
          )
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            questionId = "WHERE_WILL_THEY_LIVE",
            nextPageId = "WHERE_WILL_THEY_LIVE_ADDRESS",
            answers = listOf(
              StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")
            )
          ),
          AssessmentConfigNextPageOption(
            questionId = "WHERE_WILL_THEY_LIVE",
            nextPageId = "FINAL_QUESTION_NEXT_PAGE",
            answers = listOf(
              StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS"),
              StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE"),
              StringAnswer(answer = "NO_ANSWER")
            )
          )
        )
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
            options = null
          )
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            questionId = null,
            nextPageId = "FINAL_QUESTION_NEXT_PAGE",
            answers = null
          )
        )
      ),
      AssessmentConfigPage(
        id = "ASSESSMENT_SUMMARY",
        title = null,
        questions = listOf(
          AssessmentConfigQuestion(
            id = "SUPPORT_NEEDS",
            title = "",
            subTitle = null,
            type = TypeOfQuestion.RADIO,
            options = listOf(
              Option(id = "SUPPORT_REQUIRED", displayText = "Support required", description = "a need for support has been identified and is accepted"),
              Option(id = "SUPPORT_NOT_REQUIRED", displayText = "Support not required", description = "no need was identified"),
              Option(id = "SUPPORT_DECLINED", displayText = "Support declined", description = "a need has been identified but support is declined")
            )
          ),
          AssessmentConfigQuestion(
            id = "CASE_NOTE_SUMMARY",
            title = "Add a case note summary",
            subTitle = "This will be displayed as a case note in both DPS and nDelius",
            type = TypeOfQuestion.LONG_TEXT,
            options = null
          )
        ),
        nextPageLogic = listOf(
          AssessmentConfigNextPageOption(
            questionId = null,
            nextPageId = "CHECK_ANSWERS",
            answers = null
          )
        )
      ),
      AssessmentConfigPage(
        id = "CHECK_ANSWERS",
        title = null,
        questions = null,
        nextPageLogic = null
      )
    )

    val expectedQuestionSet = AssessmentQuestionSet(
      version = 2,
      generic = false,
      pathway = Pathway.ACCOMMODATION,
      genericAssessmentVersion = 1,
      pages = expectedPages
    )

    val assessmentQuestionSet = resettlementAssessmentService.getConfig(Pathway.ACCOMMODATION, ResettlementAssessmentType.BCST2, version = 2)
    Assertions.assertNotNull(assessmentQuestionSet)
    Assertions.assertEquals(expectedQuestionSet, assessmentQuestionSet)
  }

  @Test
  fun `test get config for invalid pathway`() {
    val invalidPathway = "INVALID_PATHWAY"
    Assertions.assertThrows(IllegalArgumentException::class.java) {
      resettlementAssessmentService.getConfig(Pathway.valueOf(invalidPathway), ResettlementAssessmentType.BCST2, version = 1)
    }
  }
}
