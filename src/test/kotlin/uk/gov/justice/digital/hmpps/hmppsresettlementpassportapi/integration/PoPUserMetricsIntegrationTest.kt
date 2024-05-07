package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.search.MeterNotFoundException
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PoPUserMetricsService

class PoPUserMetricsIntegrationTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var popUsermetricsService: PoPUserMetricsService

  @Autowired
  protected lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  protected lateinit var registry: MeterRegistry

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect metrics - happy path `() {
    prisonRegisterApiMockServer.stubPrisonList(200)
    cvlApiMockServer.stubFindLicencesByNomsId("G4161UF", 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(101, 200)
    popUserApiMockServer.stubGetPopUserVerifiedList(200)
    popUsermetricsService.recordProbationUsersLicenceConditionMetrics()

    Assertions.assertEquals(
      50.0,
      registry.get("missing_licence_conditions_percentage")
        .tags("prison", "Moorland (HMP & YOI)", "licenceType", "Standard").gauge()
        .value(),
    )

    Assertions.assertEquals(
      50.0,
      registry.get("missing_licence_conditions_percentage")
        .tags("prison", "Moorland (HMP & YOI)", "licenceType", "Others").gauge()
        .value(),
    )
    registry.clear()
    unmockkAll()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect metrics - happy path with no licence condition`() {
    prisonRegisterApiMockServer.stubPrisonList(200)
    cvlApiMockServer.stubFindNoLicencesByNomsId("G4161UF", 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(102, 200)
    popUserApiMockServer.stubGetPopUserVerifiedList(200)
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
    registry.clear()
    unmockkAll()
  }

  @Test
  fun `test collect metrics - happy path with no pop user`() {
    prisonRegisterApiMockServer.stubPrisonList(200)
    popUserApiMockServer.stubGetPopUserVerifiedEmptyList(200)
    popUsermetricsService.recordProbationUsersLicenceConditionMetrics()
    assertThrows<MeterNotFoundException> { registry.get("missing_licence_conditions_percentage").gauges() }
    unmockkAll()
  }
}
