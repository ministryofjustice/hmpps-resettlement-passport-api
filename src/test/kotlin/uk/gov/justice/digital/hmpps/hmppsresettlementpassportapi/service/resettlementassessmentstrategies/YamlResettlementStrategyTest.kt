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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
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

  @ParameterizedTest(name = "{0} - currentPage: {1}")
  @MethodSource("test getNextPageId data")
  fun `test getNextPageId`(
    questionsAndAnswers: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>,
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
          pathway = Pathway.ACCOMMODATION,
          assessmentType = ResettlementAssessmentType.BCST2,
          currentPage = currentPage,
        )
      }
      return
    }

    val nextPageId = resettlementAssessmentService.getNextPageId(
      assessment = assessment,
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      currentPage = currentPage,
    )

    Assertions.assertEquals(expectedNextPage, nextPageId)
  }

  private fun `test getNextPageId data`() = Stream.of(
    Arguments.of(
      emptyList<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      ResettlementAssessmentRequest(emptyList()),
      Pathway.ACCOMMODATION,
      ResettlementAssessmentType.BCST2,
      null,
      "WHERE_DID_THEY_LIVE",
    ),
    Arguments.of(
      emptyList<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      ResettlementAssessmentRequest(emptyList()),
      ResettlementAssessmentType.BCST2,
      "CHECK_ANSWERS",
      "Cannot get the next question from CHECK_ANSWERS as this is the end of the flow for this pathway.",
    ),
    Arguments.of(
      listOf(
        ResettlementAssessmentRequestQuestionAndAnswer("Sample Question", StringAnswer("Sample Answer")),
      ),
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
    val resettlementAssessmentEntity = if (returnResettlementAssessmentEntity) ResettlementAssessmentEntity(1, prisonerEntity, Pathway.ACCOMMODATION, Status.NOT_STARTED, ResettlementAssessmentType.BCST2, assessment, testDate, "", ResettlementAssessmentStatus.COMPLETE, "some text", "USER_1", submissionDate = null) else null
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
    val assessmentQuestionSet = resettlementAssessmentService.getConfig(Pathway.ACCOMMODATION, ResettlementAssessmentType.BCST2)
    Assertions.assertNotNull(assessmentQuestionSet)
    Assertions.assertTrue(assessmentQuestionSet.pages.isNotEmpty())
  }

  @Test
  fun `test get config for invalid pathway`() {
    val invalidPathway = "INVALID_PATHWAY" // an invalid pathway
    Assertions.assertThrows(IllegalArgumentException::class.java) {
      resettlementAssessmentService.getConfig(Pathway.valueOf(invalidPathway), ResettlementAssessmentType.BCST2)
    }
  }
}
