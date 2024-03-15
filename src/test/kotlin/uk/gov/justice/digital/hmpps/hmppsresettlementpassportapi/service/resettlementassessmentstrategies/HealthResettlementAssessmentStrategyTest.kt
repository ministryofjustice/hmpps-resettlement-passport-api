package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class HealthResettlementAssessmentStrategyTest {
  private lateinit var resettlementAssessmentService: HealthResettlementAssessmentStrategy

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
    resettlementAssessmentService = HealthResettlementAssessmentStrategy(
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
      pathway = Pathway.HEALTH,
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
      "REGISTERED_WITH_GP",
    ),
    // If the answer to REGISTERED_WITH_GP is NO, go to HELP_REGISTERING_GP
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
      ),
      "REGISTERED_WITH_GP",
      "HELP_REGISTERING_GP",
    ),
    // If the answer to REGISTERED_WITH_GP is YES, go to MEET_HEALTHCARE_TEAM
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("YES")),
      ),
      "REGISTERED_WITH_GP",
      "MEET_HEALTHCARE_TEAM",
    ),
    // Any answer to HELP_REGISTERING_GP, go to MEET_HEALTHCARE_TEAM
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_REGISTERING_GP", answer = StringAnswer("NO")),
      ),
      "HELP_REGISTERING_GP",
      "MEET_HEALTHCARE_TEAM",
    ),
    // If the answer to MEET_HEALTHCARE_TEAM is YES, go to WHAT_HEALTH_NEED
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_REGISTERING_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("MEET_HEALTHCARE_TEAM", answer = StringAnswer("YES")),
      ),
      "MEET_HEALTHCARE_TEAM",
      "WHAT_HEALTH_NEED",
    ),
    // If the answer to MEET_HEALTHCARE_TEAM is NO, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_REGISTERING_GP", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("MEET_HEALTHCARE_TEAM", answer = StringAnswer("NO")),
      ),
      "MEET_HEALTHCARE_TEAM",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to WHAT_HEALTH_NEED, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_REGISTERING_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("MEET_HEALTHCARE_TEAM", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_HEALTH_NEED", answer = ListAnswer(listOf("Physical health", "Mental health"))),
      ),
      "WHAT_HEALTH_NEED",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("REGISTERED_WITH_GP", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_REGISTERING_GP", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("MEET_HEALTHCARE_TEAM", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("WHAT_HEALTH_NEED", answer = ListAnswer(listOf("Physical health", "Mental health"))),
        ResettlementAssessmentRequestQuestionAndAnswer("CASE_NOTE_SUMMARY", answer = StringAnswer("My case note summary.")),
      ),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  @ParameterizedTest
  @MethodSource("test get page from Id - no existing assessment data")
  fun `test get page from Id - no existing assessment`(pageIdInput: String, expectedPage: ResettlementAssessmentResponsePage) {
    val nomsId = "123"
    setUpMocks("123", false)

    val page = resettlementAssessmentService.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.HEALTH,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
    )
    Assertions.assertEquals(expectedPage.id, page.id)
    Assertions.assertEquals(expectedPage.questionsAndAnswers, page.questionsAndAnswers)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "REGISTERED_WITH_GP",
      ResettlementAssessmentResponsePage(
        id = "REGISTERED_WITH_GP",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "REGISTERED_WITH_GP",
              title = "Is the person in prison registered with a GP surgery outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "REGISTERED_WITH_GP",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_REGISTERING_GP",
      ResettlementAssessmentResponsePage(
        id = "HELP_REGISTERING_GP",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "HELP_REGISTERING_GP",
              title = "Does the person in prison want help registering with a  GP surgery?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "HELP_REGISTERING_GP",
          ),
        ),
      ),
    ),
    Arguments.of(
      "MEET_HEALTHCARE_TEAM",
      ResettlementAssessmentResponsePage(
        id = "MEET_HEALTHCARE_TEAM",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "MEET_HEALTHCARE_TEAM",
              title = "Does the person in prison want to meet with a prison healthcare team?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "MEET_HEALTHCARE_TEAM",
          ),
        ),
      ),
    ),
    Arguments.of(
      "WHAT_HEALTH_NEED",
      ResettlementAssessmentResponsePage(
        id = "WHAT_HEALTH_NEED",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "WHAT_HEALTH_NEED",
              title = "What health need is this related to?",
              subTitle = null,
              type = TypeOfQuestion.CHECKBOX,
              options = mutableListOf(
                Option(id = "PHYSICAL_HEALTH", displayText = "Physical health"),
                Option(id = "MENTAL_HEALTH", displayText = "Mental health"),
                Option(id = "PERINATAL_MENTAL_HEALTH", displayText = "Perinatal mental health"),
                Option(id = "NO_ANSWER", displayText = "No answer provided", exclusive = true),
              ),
            ),
            originalPageId = "WHAT_HEALTH_NEED",
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

  private fun setUpMocks(nomsId: String, returnResettlementAssessmentEntity: Boolean, assessment: ResettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentQuestionAndAnswerList(listOf())) {
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val pathwayEntity = PathwayEntity(1, "Health", true, testDate)
    val resettlementAssessmentStatusEntities = listOf(ResettlementAssessmentStatusEntity(3, "Complete", true, testDate), ResettlementAssessmentStatusEntity(4, "Submitted", true, testDate))
    val resettlementAssessmentEntity = if (returnResettlementAssessmentEntity) ResettlementAssessmentEntity(1, prisonerEntity, pathwayEntity, StatusEntity(1, "Not Started", true, testDate), ResettlementAssessmentType.BCST2, assessment, testDate, "", resettlementAssessmentStatusEntities[0], "some text", "USER_1") else null
    Mockito.`when`(pathwayRepository.findById(Pathway.HEALTH.id)).thenReturn(Optional.of(pathwayEntity))
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
