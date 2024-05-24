package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResettlementAssessmentConfig
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
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
      true,
    )
  }

  private fun getTestConfig() = ResettlementAssessmentConfig().assessmentQuestionSets(
    PathMatchingResourcePatternResolver(),
  )

  @ParameterizedTest(name = "{0} - useYaml: {1}")
  @MethodSource("test appliesTo data")
  fun `test appliesTo`(pathway: Pathway, useYaml: Boolean, expectation: Boolean) {
    resettlementAssessmentService = YamlResettlementAssessmentStrategy(
      getTestConfig(),
      resettlementAssessmentRepository,
      prisonerRepository,
      pathwayStatusRepository,
      useYaml,
    )

    Assertions.assertEquals(expectation, resettlementAssessmentService.appliesTo(pathway))
  }

  private fun `test appliesTo data`() = Stream.of(
    Arguments.of(Pathway.ACCOMMODATION, true, true),
    Arguments.of(Pathway.ACCOMMODATION, false, false),
    Arguments.of(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, true, false),
    Arguments.of(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, false, false),
    Arguments.of(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, true, true),
    Arguments.of(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, false, false),
    Arguments.of(Pathway.DRUGS_AND_ALCOHOL, true, false),
    Arguments.of(Pathway.DRUGS_AND_ALCOHOL, false, false),
    Arguments.of(Pathway.EDUCATION_SKILLS_AND_WORK, true, false),
    Arguments.of(Pathway.EDUCATION_SKILLS_AND_WORK, false, false),
    Arguments.of(Pathway.FINANCE_AND_ID, true, true),
    Arguments.of(Pathway.FINANCE_AND_ID, false, false),
    Arguments.of(Pathway.HEALTH, true, true),
    Arguments.of(Pathway.HEALTH, false, false),
  )
}
