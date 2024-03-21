package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.SchedulerService
import java.time.LocalDate
import java.time.LocalDateTime

class SchedulerIntegrationTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var schedulerService: SchedulerService

  @Autowired
  protected lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  protected lateinit var popUserOTPRepository: PoPUserOTPRepository

  @AfterEach
  fun afterEach() {
    prisonerSearchApiMockServer.resetMappings()
  }

  @Test
  fun `test reconcile release dates in database - blank`() {
    schedulerService.reconcileReleaseDatesInDatabase()
    Assertions.assertEquals(listOf<PrisonerEntity>(), prisonerRepository.findAll())
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoners-1.sql")
  fun `test reconcile release dates in database - release date updates from prisoner search api`() {
    val expectedPrisonerEntities = getExpectedPrisonEntities()
    expectedPrisonerEntities[0].releaseDate = LocalDate.parse("2023-08-20")
    expectedPrisonerEntities[1].releaseDate = LocalDate.parse("2023-08-20")
    expectedPrisonerEntities[2].releaseDate = LocalDate.parse("2023-08-20")
    expectedPrisonerEntities[3].releaseDate = LocalDate.parse("2023-08-20")

    prisonerSearchApiMockServer.stubGet(
      "/prisoner/G1458GV",
      200,
      "testdata/prisoner-search-api/release-dates-cron/prisoner-details.json",
    )
    prisonerSearchApiMockServer.stubGet(
      "/prisoner/G6628UE",
      200,
      "testdata/prisoner-search-api/release-dates-cron/prisoner-details.json",
    )
    prisonerSearchApiMockServer.stubGet(
      "/prisoner/G6335VX",
      200,
      "testdata/prisoner-search-api/release-dates-cron/prisoner-details.json",
    )
    prisonerSearchApiMockServer.stubGet(
      "/prisoner/G6933GF",
      200,
      "testdata/prisoner-search-api/release-dates-cron/prisoner-details.json",
    )

    schedulerService.reconcileReleaseDatesInDatabase()
    Assertions.assertEquals(expectedPrisonerEntities, prisonerRepository.findAll().sortedBy { it.id })
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoners-1.sql")
  fun `test reconcile release dates in database - no results from prisoner search api`() {
    val expectedPrisonerEntities = getExpectedPrisonEntities()
    schedulerService.reconcileReleaseDatesInDatabase()
    Assertions.assertEquals(expectedPrisonerEntities, prisonerRepository.findAll().sortedBy { it.id })
  }

  private fun getExpectedPrisonEntities() = listOf(
    PrisonerEntity(
      id = 1,
      nomsId = "G1458GV",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN1",
      prisonId = "xyz",
      releaseDate = LocalDate.parse("2025-01-01"),
    ),
    PrisonerEntity(
      id = 2,
      nomsId = "G6628UE",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN2",
      prisonId = "xyz",
      releaseDate = LocalDate.parse("2026-06-30"),
    ),
    PrisonerEntity(
      id = 3,
      nomsId = "G6335VX",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN3",
      prisonId = "xyz",
      releaseDate = LocalDate.parse("2021-05-21"),
    ),
    PrisonerEntity(
      id = 4,
      nomsId = "G6933GF",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN4",
      prisonId = "xyz",
      releaseDate = LocalDate.parse("2027-11-10"),
    ),
    PrisonerEntity(
      id = 5,
      nomsId = "A8339DY",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN5",
      prisonId = "xyz",
      releaseDate = LocalDate.parse("2019-09-15"),
    ),
    PrisonerEntity(
      id = 6,
      nomsId = "A8132DY",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN6",
      prisonId = "xyz",
      releaseDate = LocalDate.parse("2050-02-08"),
    ),
    PrisonerEntity(
      id = 7,
      nomsId = "A8258DY",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN7",
      prisonId = "xyz",
      releaseDate = LocalDate.parse("2090-02-05"),
    ),
    PrisonerEntity(
      id = 8,
      nomsId = "A8257DY",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN8",
      prisonId = "xyz",
      releaseDate = LocalDate.parse("2040-12-12"),
    ),
    PrisonerEntity(
      id = 9,
      nomsId = "A8314DY",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN9",
      prisonId = "xyz",
      releaseDate = LocalDate.parse("2027-11-09"),
    ),
    PrisonerEntity(
      id = 10,
      nomsId = "A8229DY",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN10",
      prisonId = "xyz",
      releaseDate = LocalDate.parse("2010-04-03"),
    ),
  )

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-3.sql")
  fun `test expired otp in database - happy path`() {
    val fakeNow = LocalDateTime.parse("2024-02-19T10:18:22.636066")
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val expectedOTPEntities = getExpectedPoPUserOTPEntities()
    schedulerService.deleteExpiredOTPScheduledTask()
    Assertions.assertEquals(expectedOTPEntities, popUserOTPRepository.findAll().sortedBy { it.id })
    unmockkStatic(LocalDateTime::class)
  }

  private fun getExpectedPoPUserOTPEntities(): List<PoPUserOTPEntity> {
    val prisoner2 = PrisonerEntity(
      id = 2,
      nomsId = "A8314DY",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN2",
      prisonId = "MDI",
      releaseDate = LocalDate.parse("2027-11-09"),
    )
    val prisoner3 = PrisonerEntity(
      id = 3,
      nomsId = "G4161UF",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN3",
      prisonId = "MDI",
      releaseDate = LocalDate.parse("2025-08-04"),
    )

    return listOf(

      PoPUserOTPEntity(
        id = 2,
        prisoner2,
        creationDate = LocalDateTime.parse("2024-02-19T10:18:22"),
        expiryDate = LocalDateTime.parse("2024-02-26T23:59:59"),
        otp = "1Y3456",
        dob = LocalDate.parse("2000-02-01"),
      ),
      PoPUserOTPEntity(
        id = 3,
        prisoner3,
        creationDate = LocalDateTime.parse("2024-02-19T10:18:22"),
        expiryDate = LocalDateTime.parse("2024-02-26T23:59:59"),
        otp = "1Z3456",
        dob = LocalDate.parse("2000-03-01"),
      ),

    )
  }
}
