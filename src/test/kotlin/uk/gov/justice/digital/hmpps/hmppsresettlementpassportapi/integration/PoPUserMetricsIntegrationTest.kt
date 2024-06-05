package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.search.MeterNotFoundException
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PoPUserMetricsService
import java.time.LocalDate

class PoPUserMetricsIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDate.parse("2010-04-03")

  @Autowired
  protected lateinit var popUsermetricsService: PoPUserMetricsService

  @Autowired
  protected lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  protected lateinit var registry: MeterRegistry

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect licence conditions metrics - happy path `() {
    prisonRegisterApiMockServer.stubPrisonList(200)
    cvlApiMockServer.stubFindLicencesByNomsId("G4161UF", 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(101, 200)
    popUserApiMockServer.stubGetPopUserVerifiedList(200)
    popUsermetricsService.recordProbationUsersLicenceConditionMetrics()

    Assertions.assertEquals(
      0.0,
      registry.get("missing_licence_conditions")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Standard Percentage").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_licence_conditions")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Standard Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_licence_conditions")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Others Percentage").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_licence_conditions")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Others Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_licence_conditions")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "No Licence User Count").gauge()
        .value(),
    )

    registry.clear()
    unmockkAll()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect licence conditions metrics - happy path with no licence condition`() {
    prisonRegisterApiMockServer.stubPrisonList(200)
    cvlApiMockServer.stubFindNoLicencesByNomsId("G4161UF", 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(102, 200)
    popUserApiMockServer.stubGetPopUserVerifiedList(200)
    popUsermetricsService.recordProbationUsersLicenceConditionMetrics()
    Assertions.assertEquals(
      1.0,
      registry.get("missing_licence_conditions")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Standard Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      100.0,
      registry.get("missing_licence_conditions")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Standard Percentage").gauge()
        .value(),
    )

    Assertions.assertEquals(
      1.0,
      registry.get("missing_licence_conditions")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Others Count").gauge()
        .value(),
    )
    Assertions.assertEquals(
      100.0,
      registry.get("missing_licence_conditions")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Others Percentage").gauge()
        .value(),
    )

    Assertions.assertEquals(
      1.0,
      registry.get("missing_licence_conditions")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "No Licence User Count").gauge()
        .value(),
    )

    registry.clear()
    unmockkAll()
  }

  @Test
  fun `test collect licence conditions metrics - happy path with no pop user`() {
    registry.clear()
    prisonRegisterApiMockServer.stubPrisonList(200)
    popUserApiMockServer.stubGetPopUserVerifiedEmptyList(200)
    popUsermetricsService.recordProbationUsersLicenceConditionMetrics()
    assertThrows<MeterNotFoundException> { registry.get("missing_licence_conditions").gauges() }
    unmockkAll()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect appointment metrics - happy path `() {
    mockkStatic(LocalDate::class)
    every { LocalDate.now() } returns fakeNow
    prisonRegisterApiMockServer.stubPrisonList(200)
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId("G4161UF", crn)
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 200)
    popUserApiMockServer.stubGetPopUserVerifiedList(200)
    popUsermetricsService.recordProbationUsersAppointmentsMetrics()

    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Date Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Time Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Type Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      45.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Location Count").gauge()
        .value(),
    )
    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Probation Officer Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Email Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "No Appointments User Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Date Percentage").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Time Percentage").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Type Percentage").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Date Score").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Time Score").gauge()
        .value(),
    )
    registry.clear()
    unmockkAll()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect appointment metrics - happy path no appointments`() {
    prisonRegisterApiMockServer.stubPrisonList(200)
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId("G4161UF", crn)
    deliusApiMockServer.stubGetAppointmentsFromCRNNoResults(crn)
    popUserApiMockServer.stubGetPopUserVerifiedList(200)
    popUsermetricsService.recordProbationUsersAppointmentsMetrics()

    Assertions.assertEquals(
      1.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Date Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      1.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Time Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      1.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Type Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      1.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Location Count").gauge()
        .value(),
    )
    Assertions.assertEquals(
      1.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Probation Officer Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      1.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Email Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      1.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "No Appointments User Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      100.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Date Percentage").gauge()
        .value(),
    )

    Assertions.assertEquals(
      100.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Time Percentage").gauge()
        .value(),
    )

    Assertions.assertEquals(
      100.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Type Percentage").gauge()
        .value(),
    )

    Assertions.assertEquals(
      2.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Date Score").gauge()
        .value(),
    )

    Assertions.assertEquals(
      2.0,
      registry.get("missing_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "Time Score").gauge()
        .value(),
    )
    registry.clear()
    unmockkAll()
  }

  @Test
  fun `test collect appointments  metrics - happy path with no pop user`() {
    registry.clear()
    prisonRegisterApiMockServer.stubPrisonList(200)
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId("G4161UF", crn)
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 200)
    popUserApiMockServer.stubGetPopUserVerifiedEmptyList(200)
    popUsermetricsService.recordProbationUsersAppointmentsMetrics()

    assertThrows<MeterNotFoundException> { registry.get("missing_appointments_data").gauges() }
    unmockkAll()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect release day appointment metrics -  happy path no zero appointments and no zero probation appointments `() {
    mockkStatic(LocalDate::class)
    every { LocalDate.now() } returns fakeNow
    prisonRegisterApiMockServer.stubPrisonList(200)
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId("G4161UF", crn)
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 200)
    popUsermetricsService.recordReleaseDayProbationUserAppointmentsMetrics()

    Assertions.assertEquals(
      0.0,
      registry.get("release_day_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "No Appointments Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      0.0,
      registry.get("release_day_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "No Probation Appointments Count").gauge()
        .value(),
    )

    registry.clear()
    unmockkAll()
    unmockkStatic(LocalDate::class)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect release day appointment metrics -  zero probation appointments `() {
    mockkStatic(LocalDate::class)
    every { LocalDate.now() } returns fakeNow
    prisonRegisterApiMockServer.stubPrisonList(200)
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId("G4161UF", crn)
    deliusApiMockServer.stubGetAppointmentsFromCRNNoProbationAppointment(crn, 200)
    popUsermetricsService.recordReleaseDayProbationUserAppointmentsMetrics()

    Assertions.assertEquals(
      0.0,
      registry.get("release_day_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "No Appointments Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      1.0,
      registry.get("release_day_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "No Probation Appointments Count").gauge()
        .value(),
    )

    registry.clear()
    unmockkAll()
    unmockkStatic(LocalDate::class)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-4.sql")
  fun `test collect release day appointment metrics - zero appointments`() {
    mockkStatic(LocalDate::class)
    every { LocalDate.now() } returns fakeNow
    prisonRegisterApiMockServer.stubPrisonList(200)
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId("G4161UF", crn)
    deliusApiMockServer.stubGetAppointmentsFromCRNNoResults(crn)
    popUsermetricsService.recordReleaseDayProbationUserAppointmentsMetrics()

    Assertions.assertEquals(
      1.0,
      registry.get("release_day_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "No Appointments Count").gauge()
        .value(),
    )

    Assertions.assertEquals(
      1.0,
      registry.get("release_day_appointments_data")
        .tags("prison", "Moorland (HMP & YOI)", "metricType", "No Probation Appointments Count").gauge()
        .value(),
    )

    registry.clear()
    unmockkAll()
    unmockkStatic(LocalDate::class)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-5.sql")
  fun `test collect appointments  metrics -  no prisoners `() {
    mockkStatic(LocalDate::class)
    every { LocalDate.now() } returns fakeNow
    registry.clear()

    prisonRegisterApiMockServer.stubPrisonList(200)
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId("G4161UF", crn)
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 200)
    popUsermetricsService.recordReleaseDayProbationUserAppointmentsMetrics()

    assertThrows<MeterNotFoundException> { registry.get("release_day_appointments_data").gauges() }
    unmockkAll()
  }
}
