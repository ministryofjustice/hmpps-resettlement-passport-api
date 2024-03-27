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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class EducationSkillsAndWorkResettlementStrategyTest {
  private lateinit var resettlementAssessmentService: EducationSkillsAndWorkResettlementAssessmentStrategy

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var pathwayRepository: PathwayRepository

  @Mock
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Mock
  private lateinit var statusRepository: StatusRepository

  @Mock
  private lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Mock
  private lateinit var resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentService = EducationSkillsAndWorkResettlementAssessmentStrategy(
      resettlementAssessmentRepository,
      prisonerRepository,
      statusRepository,
      pathwayRepository,
      pathwayStatusRepository,
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
      pathway = Pathway.EDUCATION_SKILLS_AND_WORK,
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
      "JOB_BEFORE_CUSTODY",
    ),
    // If the answer to JOB_BEFORE_CUSTODY is YES, go to TYPE_OF_EMPLOYMENT_CONTRACT
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("YES")),
      ),
      "JOB_BEFORE_CUSTODY",
      "TYPE_OF_EMPLOYMENT_CONTRACT",
    ),
    // If the answer to JOB_BEFORE_CUSTODY is NO, go to HAVE_A_JOB_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
      ),
      "JOB_BEFORE_CUSTODY",
      "HAVE_A_JOB_AFTER_RELEASE",
    ),
    // If the answer to JOB_BEFORE_CUSTODY is NO_ANSWER, go to HAVE_A_JOB_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO_ANSWER")),
      ),
      "JOB_BEFORE_CUSTODY",
      "HAVE_A_JOB_AFTER_RELEASE",
    ),
    // Any answer to TYPE_OF_EMPLOYMENT_CONTRACT, go to RETURN_TO_JOB_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO_ANSWER")),
        ResettlementAssessmentRequestQuestionAndAnswer("TYPE_OF_EMPLOYMENT_CONTRACT", answer = ListAnswer(listOf("FULL_TIME_CONTRACT", "PERMANENT_CONTRACT"))),
      ),
      "TYPE_OF_EMPLOYMENT_CONTRACT",
      "RETURN_TO_JOB_AFTER_RELEASE",
    ),
    // If the answer to RETURN_TO_JOB_AFTER_RELEASE is YES, go to HELP_CONTACTING_EMPLOYER
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("TYPE_OF_EMPLOYMENT_CONTRACT", answer = ListAnswer(listOf("FULL_TIME_CONTRACT", "PERMANENT_CONTRACT"))),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
      ),
      "RETURN_TO_JOB_AFTER_RELEASE",
      "HELP_CONTACTING_EMPLOYER",
    ),
    // If the answer to RETURN_TO_JOB_AFTER_RELEASE is NO, go to HAVE_A_JOB_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("TYPE_OF_EMPLOYMENT_CONTRACT", answer = ListAnswer(listOf("FULL_TIME_CONTRACT", "PERMANENT_CONTRACT"))),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_JOB_AFTER_RELEASE", answer = StringAnswer("NO")),
      ),
      "RETURN_TO_JOB_AFTER_RELEASE",
      "HAVE_A_JOB_AFTER_RELEASE",
    ),
    // If the answer to RETURN_TO_JOB_AFTER_RELEASE is NO_ANSWER, go to HAVE_A_JOB_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("TYPE_OF_EMPLOYMENT_CONTRACT", answer = ListAnswer(listOf("FULL_TIME_CONTRACT", "PERMANENT_CONTRACT"))),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_JOB_AFTER_RELEASE", answer = StringAnswer("NO_ANSWER")),
      ),
      "RETURN_TO_JOB_AFTER_RELEASE",
      "HAVE_A_JOB_AFTER_RELEASE",
    ),
    // If the answer to HAVE_A_JOB_AFTER_RELEASE is YES, go to HELP_CONTACTING_EMPLOYER
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
      ),
      "HAVE_A_JOB_AFTER_RELEASE",
      "HELP_CONTACTING_EMPLOYER",
    ),
    // If the answer to HAVE_A_JOB_AFTER_RELEASE is NO, go to SUPPORT_TO_FIND_JOB
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("NO")),
      ),
      "HAVE_A_JOB_AFTER_RELEASE",
      "SUPPORT_TO_FIND_JOB",
    ),
    // If the answer to HAVE_A_JOB_AFTER_RELEASE is NO_ANSWER, go to SUPPORT_TO_FIND_JOB
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("NO_ANSWER")),
      ),
      "HAVE_A_JOB_AFTER_RELEASE",
      "SUPPORT_TO_FIND_JOB",
    ),
    // If the answer to HELP_CONTACTING_EMPLOYER is YES, go to EMPLOYMENT_DETAILS_BEFORE_CUSTODY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
      ),
      "HELP_CONTACTING_EMPLOYER",
      "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
    ),
    // If the answer to HELP_CONTACTING_EMPLOYER is NO, go to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("NO")),
      ),
      "HELP_CONTACTING_EMPLOYER",
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
    ),
    // If the answer to HELP_CONTACTING_EMPLOYER is NO_ANSWER, go to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("NO_ANSWER")),
      ),
      "HELP_CONTACTING_EMPLOYER",
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
    ),
    // Any answer to SUPPORT_TO_FIND_JOB, go to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_TO_FIND_JOB", answer = StringAnswer("NO")),
      ),
      "SUPPORT_TO_FIND_JOB",
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
    ),
    // Any answer to EMPLOYMENT_DETAILS_BEFORE_CUSTODY, go to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
      ),
      "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
    ),
    // If the answer to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY is YES, go to RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
      ),
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
      "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
    ),
    // If the answer to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY is NO, go to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("NO")),
      ),
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
    ),
    // If the answer to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY is NO_ANSWER, go to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("NO_ANSWER")),
      ),
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
    ),
    // If the answer to RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE is NO, go to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("NO")),
      ),
      "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
    ),
    // If the answer to RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE is YES, go to HELP_CONTACTING_EDUCATION_PROVIDER
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
      ),
      "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "HELP_CONTACTING_EDUCATION_PROVIDER",
    ),
    // If the answer to RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE is NO_ANSWER, go to HELP_CONTACTING_EDUCATION_PROVIDER
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("NO_ANSWER")),
      ),
      "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "HELP_CONTACTING_EDUCATION_PROVIDER",
    ),
    // If the answer to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE is YES, go to BURSARIES_AND_GRANTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
      ),
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "BURSARIES_AND_GRANTS",
    ),
    // If the answer to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE is NO, go to SUPPORT_NEEDS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("NO")),
      ),
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "ASSESSMENT_SUMMARY",
    ),
    // If the answer to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE is NO_ANSWER, go to SUPPORT_NEEDS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("NO_ANSWER")),
      ),
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "ASSESSMENT_SUMMARY",
    ),
    // If the answer to HELP_CONTACTING_EDUCATION_PROVIDER is YES, go to TRAINING_PROVIDER_DETAILS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("YES")),
      ),
      "HELP_CONTACTING_EDUCATION_PROVIDER",
      "TRAINING_PROVIDER_DETAILS",
    ),
    // If the answer to HELP_CONTACTING_EDUCATION_PROVIDER is NO, go to BURSARIES_AND_GRANTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("NO")),
      ),
      "HELP_CONTACTING_EDUCATION_PROVIDER",
      "BURSARIES_AND_GRANTS",
    ),
    // If the answer to HELP_CONTACTING_EDUCATION_PROVIDER is NO_ANSWER, go to BURSARIES_AND_GRANTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("NO_ANSWER")),
      ),
      "HELP_CONTACTING_EDUCATION_PROVIDER",
      "BURSARIES_AND_GRANTS",
    ),
    // Any answer to TRAINING_PROVIDER_DETAILS, go to BURSARIES_AND_GRANTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_TRAINING_PROVIDER", answer = StringAnswer("College")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_TRAINING_PROVIDER", answer = MapAnswer(listOf(mapOf("address_line1" to "456 The Street")))),
      ),
      "TRAINING_PROVIDER_DETAILS",
      "BURSARIES_AND_GRANTS",
    ),
    // Any answer to BURSARIES_AND_GRANTS, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_TRAINING_PROVIDER", answer = StringAnswer("College")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_TRAINING_PROVIDER", answer = MapAnswer(listOf(mapOf("address_line1" to "456 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("BURSARIES_AND_GRANTS", answer = StringAnswer("YES")),
      ),
      "BURSARIES_AND_GRANTS",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_TRAINING_PROVIDER", answer = StringAnswer("College")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_TRAINING_PROVIDER", answer = MapAnswer(listOf(mapOf("address_line1" to "456 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("BURSARIES_AND_GRANTS", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_NEEDS", answer = StringAnswer("SUPPORT_REQUIRED")),
        ResettlementAssessmentRequestQuestionAndAnswer("CASE_NOTE_SUMMARY", answer = StringAnswer("Some text here...")),
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
      pathway = Pathway.EDUCATION_SKILLS_AND_WORK,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "JOB_BEFORE_CUSTODY",
      ResettlementAssessmentResponsePage(
        id = "JOB_BEFORE_CUSTODY",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "JOB_BEFORE_CUSTODY",
              title = "Did the person in prison have a job before custody?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "JOB_BEFORE_CUSTODY",
          ),
        ),
      ),
    ),
    Arguments.of(
      "TYPE_OF_EMPLOYMENT_CONTRACT",
      ResettlementAssessmentResponsePage(
        id = "TYPE_OF_EMPLOYMENT_CONTRACT",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "TYPE_OF_EMPLOYMENT_CONTRACT",
              title = "Type of employment contract",
              type = TypeOfQuestion.CHECKBOX,
              options = mutableListOf(
                Option(id = "FULL_TIME_CONTRACT", displayText = "Full-time contract"),
                Option(id = "PART_TIME_CONTRACT", displayText = "Part-time contract"),
                Option(id = "PERMANENT_CONTRACT", displayText = "Permanent contract"),
                Option(id = "TEMPORARY_CONTRACT", displayText = "Temporary contract"),
                Option(id = "FIXED_TERM_CONTRACT", displayText = "Fixed-term contract"),
                Option(id = "ZERO_HOURS_CONTRACT", displayText = "Zero hours contract"),
                Option(id = "NO_ANSWER", displayText = "No answer provided", exclusive = true),
              ),
            ),
            originalPageId = "TYPE_OF_EMPLOYMENT_CONTRACT",
          ),
        ),
      ),
    ),
    Arguments.of(
      "RETURN_TO_JOB_AFTER_RELEASE",
      ResettlementAssessmentResponsePage(
        id = "RETURN_TO_JOB_AFTER_RELEASE",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "RETURN_TO_JOB_AFTER_RELEASE",
              title = "Can the person in prison return to this job after release?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "RETURN_TO_JOB_AFTER_RELEASE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HAVE_A_JOB_AFTER_RELEASE",
      ResettlementAssessmentResponsePage(
        id = "HAVE_A_JOB_AFTER_RELEASE",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "HAVE_A_JOB_AFTER_RELEASE",
              title = "Does the person in prison have a job when they are released?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "HAVE_A_JOB_AFTER_RELEASE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_CONTACTING_EMPLOYER",
      ResettlementAssessmentResponsePage(
        id = "HELP_CONTACTING_EMPLOYER",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "HELP_CONTACTING_EMPLOYER",
              title = "Does the person in prison need help contacting the employer?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "HELP_CONTACTING_EMPLOYER",
          ),
        ),
      ),
    ),
    Arguments.of(
      "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
      ResettlementAssessmentResponsePage(
        id = "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
        title = "Employment before custody",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "EMPLOYMENT_TITLE_BEFORE_CUSTODY",
              title = "Job title",
              type = TypeOfQuestion.SHORT_TEXT,
            ),
            originalPageId = "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
          ),
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "NAME_OF_EMPLOYER",
              title = "Employer",
              type = TypeOfQuestion.SHORT_TEXT,
            ),
            originalPageId = "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
          ),
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "ADDRESS_OF_EMPLOYER",
              title = "Employer address",
              type = TypeOfQuestion.ADDRESS,
            ),
            originalPageId = "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
          ),
        ),
      ),
    ),
    Arguments.of(
      "SUPPORT_TO_FIND_JOB",
      ResettlementAssessmentResponsePage(
        id = "SUPPORT_TO_FIND_JOB",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "SUPPORT_TO_FIND_JOB",
              title = "Does the person in prison want support to find a job when they are released?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "SUPPORT_TO_FIND_JOB",
          ),
        ),
      ),
    ),
    Arguments.of(
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
      ResettlementAssessmentResponsePage(
        id = "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
              title = "Was the person in prison in education or training before custody?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
          ),
        ),
      ),
    ),
    Arguments.of(
      "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      ResettlementAssessmentResponsePage(
        id = "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
              title = "Can the person in prison return to this education or training after release?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      ResettlementAssessmentResponsePage(
        id = "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
              title = "Does the person in prison want to start education or training after release?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_CONTACTING_EDUCATION_PROVIDER",
      ResettlementAssessmentResponsePage(
        id = "HELP_CONTACTING_EDUCATION_PROVIDER",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "HELP_CONTACTING_EDUCATION_PROVIDER",
              title = "Does the person in prison want help contacting an education or training provider?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "HELP_CONTACTING_EDUCATION_PROVIDER",
          ),
        ),
      ),
    ),
    Arguments.of(
      "TRAINING_PROVIDER_DETAILS",
      ResettlementAssessmentResponsePage(
        id = "TRAINING_PROVIDER_DETAILS",
        title = "Education or training before custody",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "NAME_OF_TRAINING_PROVIDER",
              title = "Education or training provider",
              type = TypeOfQuestion.SHORT_TEXT,
            ),
            originalPageId = "TRAINING_PROVIDER_DETAILS",
          ),
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "ADDRESS_OF_TRAINING_PROVIDER",
              title = "Education or training provider address",
              type = TypeOfQuestion.ADDRESS,
            ),
            originalPageId = "TRAINING_PROVIDER_DETAILS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "BURSARIES_AND_GRANTS",
      ResettlementAssessmentResponsePage(
        id = "BURSARIES_AND_GRANTS",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "BURSARIES_AND_GRANTS",
              title = "Does the person in prison want to find out about bursaries and grants for courses or training?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "BURSARIES_AND_GRANTS",
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

  private fun setUpMocks(nomsId: String, returnResettlementAssessmentEntity: Boolean, assessment: ResettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentQuestionAndAnswerList(mutableListOf())) {
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val pathwayEntity = PathwayEntity(5, "Education, skills and work", true, testDate)
    val resettlementAssessmentStatusEntities = listOf(ResettlementAssessmentStatusEntity(3, "Complete", true, testDate), ResettlementAssessmentStatusEntity(4, "Submitted", true, testDate))
    val resettlementAssessmentEntity = if (returnResettlementAssessmentEntity) ResettlementAssessmentEntity(1, prisonerEntity, pathwayEntity, StatusEntity(1, "Not Started", true, testDate), ResettlementAssessmentType.BCST2, assessment, testDate, "", resettlementAssessmentStatusEntities[0], "some text", "USER_1") else null
    Mockito.`when`(pathwayRepository.findById(Pathway.EDUCATION_SKILLS_AND_WORK.id)).thenReturn(Optional.of(pathwayEntity))
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
