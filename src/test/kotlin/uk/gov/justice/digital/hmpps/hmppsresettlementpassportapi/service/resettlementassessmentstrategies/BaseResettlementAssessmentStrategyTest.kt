package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResettlementAssessmentConfig
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDate
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
open class BaseResettlementAssessmentStrategyTest(private val pathway: Pathway) {
  lateinit var resettlementAssessmentStrategy: ResettlementAssessmentStrategy

  @Mock
  lateinit var prisonerRepository: PrisonerRepository

  @Mock
  lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Mock
  lateinit var pathwayStatusRepository: PathwayStatusRepository

  val testDate: LocalDateTime = LocalDateTime.parse("2023-08-16T12:00:00")

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentStrategy = ResettlementAssessmentStrategy(
      getTestConfig(),
      resettlementAssessmentRepository,
      prisonerRepository,
      pathwayStatusRepository,
    )
  }

  private fun getTestConfig() = ResettlementAssessmentConfig().assessmentQuestionSets(
    PathMatchingResourcePatternResolver(),
  )

  fun stubSave() {
    given(resettlementAssessmentRepository.save(any())).willAnswer { mock ->
      mock.arguments[0]
    }
  }

  fun setUpMocks(nomsId: String, returnResettlementAssessmentEntity: Boolean, assessment: ResettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentQuestionAndAnswerList(listOf()), assessmentStatus: ResettlementAssessmentStatus = ResettlementAssessmentStatus.COMPLETE) {
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val resettlementAssessmentEntity = if (returnResettlementAssessmentEntity) ResettlementAssessmentEntity(1, 1, pathway, Status.NOT_STARTED, ResettlementAssessmentType.BCST2, assessment, testDate, "", assessmentStatus, "some text", "USER_1", submissionDate = null, version = 1) else null
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    whenever(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        1,
        pathway,
        ResettlementAssessmentType.BCST2,
        listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
      ),
    ).thenReturn(resettlementAssessmentEntity)
  }
}
