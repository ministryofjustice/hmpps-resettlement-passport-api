package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResettlementAssessmentConfig
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
open class BaseYamlResettlementStrategyTest {
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
}
