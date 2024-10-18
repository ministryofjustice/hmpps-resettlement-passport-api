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
    )
    val prisoner3 = PrisonerEntity(
      id = 3,
      nomsId = "G4161UF",
      creationDate = LocalDateTime.parse("2023-05-17T12:21:44"),
      crn = "CRN3",
      prisonId = "MDI",
    )

    return listOf(

      PoPUserOTPEntity(
        id = 2,
        prisoner2.id(),
        creationDate = LocalDateTime.parse("2024-02-19T10:18:22"),
        expiryDate = LocalDateTime.parse("2024-02-26T23:59:59"),
        otp = "1Y3456",
        dob = LocalDate.parse("2000-02-01"),
      ),
      PoPUserOTPEntity(
        id = 3,
        prisoner3.id(),
        creationDate = LocalDateTime.parse("2024-02-19T10:18:22"),
        expiryDate = LocalDateTime.parse("2024-02-26T23:59:59"),
        otp = "1Z3456",
        dob = LocalDate.parse("2000-03-01"),
      ),

    )
  }
}
