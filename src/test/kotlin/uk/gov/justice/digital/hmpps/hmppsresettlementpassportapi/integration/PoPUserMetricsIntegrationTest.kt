package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PoPUserMetricsService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PoPUserOTPService
import java.time.LocalDate

class PoPUserMetricsIntegrationTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var popUsermetricsService: PoPUserMetricsService

  @Autowired
  protected lateinit var popUserOTPService: PoPUserOTPService

  @Autowired
  protected lateinit var popUserOTPRepository: PoPUserOTPRepository

  @Autowired
  protected lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  protected lateinit var registry: MeterRegistry

  private val fakeNow = LocalDate.parse("2023-01-01")

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect metrics - happy path `() {
    prisonRegisterApiMockServer.stubPrisonList(200)
    cvlApiMockServer.stubFindLicencesByNomsId("G4161UF", 200)
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
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect metrics - happy path with no licence condition`() {
    prisonRegisterApiMockServer.stubPrisonList(200)
    cvlApiMockServer.stubFindNoLicencesByNomsId("G4161UF", 200)
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
}
