package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.LicenceConditionService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PoPUserMetricsService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

class PoPUserMetricsIntegrationTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var popUsermetricsService: PoPUserMetricsService

  @Autowired
  protected lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Autowired
  protected lateinit var popUserOTPRepository: PoPUserOTPRepository

  @Autowired
  protected lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  protected lateinit var registry: MeterRegistry

  private val fakeNow = LocalDate.parse("2023-01-01")

  @Autowired
  protected lateinit var licenceConditionService: LicenceConditionService

  @Test
  fun `test collect metrics - happy path `() {
    seedPopUserOTP()
    prisonRegisterApiMockServer.stubPrisonList(200)
    cvlApiMockServer.stubFindLicencesByNomsId("G12345", 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(101, 200)
    popUsermetricsService.recordProbationUsersLicenceConditionMetrics()

    Assertions.assertEquals(
      0.0,
      registry.get("missing_licence_conditions_percentage")
        .tags("prison", "Moorland (HMP & YOI)", "licenceType", "Standard").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_licence_conditions_percentage")
        .tags("prison", "Moorland (HMP & YOI)", "licenceType", "Others").gauge()
        .value(),
    )
    unmockkAll()
  }

  @Test
  fun `test collect metrics - happy path with no licence condition`() {
    seedPopUserOTP()
    prisonRegisterApiMockServer.stubPrisonList(200)
    cvlApiMockServer.stubFindNoLicencesByNomsId("G12345", 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(102, 200)
    popUsermetricsService.recordProbationUsersLicenceConditionMetrics()

    Assertions.assertEquals(
      100.0,
      registry.get("missing_licence_conditions_percentage")
        .tags("prison", "Moorland (HMP & YOI)", "licenceType", "Standard").gauge()
        .value(),
    )

    Assertions.assertEquals(
      100.0,
      registry.get("missing_licence_conditions_percentage")
        .tags("prison", "Moorland (HMP & YOI)", "licenceType", "Others").gauge()
        .value(),
    )
    unmockkAll()
  }

  private fun seedPopUserOTP() {
    val uniqueId = AtomicLong()

    val prisoner = prisonerRepository.save(
      PrisonerEntity(
        null,
        "G12345",
        LocalDateTime.now(),
        "${uniqueId.incrementAndGet()}",
        "MDI",
        LocalDate.parse("2022-11-01"),
      ),
    )

    val popUser = popUserOTPRepository.save(
      PoPUserOTPEntity(
        null,
        prisoner,
        LocalDateTime.now(),
        LocalDateTime.now().plusDays(7),
        "123",
        LocalDate.parse("2000-11-30"),
      ),
    )
  }
}
