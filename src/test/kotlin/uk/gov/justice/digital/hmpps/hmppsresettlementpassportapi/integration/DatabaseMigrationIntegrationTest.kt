package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import jakarta.persistence.EntityManager
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.BankApplicationRepository
import java.sql.Date
import java.time.LocalDate
import javax.sql.DataSource

class DatabaseMigrationIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var dataSource: DataSource

  @Autowired
  lateinit var entityManager: EntityManager

  @Test
  fun `test database migration - update timestamps in bank_application to dates`() {
    // Drop database
    val dropDatabasePopulator = ResourceDatabasePopulator(ClassPathResource("testdata/sql/drop-database.sql"))
    dropDatabasePopulator.execute(dataSource)

    // Re-run flyway script up to version 1.40
    Flyway.configure().dataSource(dataSource).target("1.40").load().migrate()

    // Seed database with old data
    val seedDatabasePopulator = ResourceDatabasePopulator(ClassPathResource("testdata/sql/test-database-v1-40.sql"))
    seedDatabasePopulator.execute(dataSource)

    // Re-run flyway script up to version 1.41
    Flyway.configure().dataSource(dataSource).target("1.41").load().migrate()

    // Assert results
    val actualBankApplications = entityManager.createNativeQuery("select id, application_submitted_date from bank_application").resultList
      .map { it as Array<*> }.map { PartialBankApplication(it[0] as Int, (it[1] as Date).toLocalDate()) }
    val expectedBankApplications = listOf<PartialBankApplication>()
    Assertions.assertEquals(expectedBankApplications, actualBankApplications)
  }

  data class PartialBankApplication(
    val id: Int,
    val applicationSubmittedDate: LocalDate,
  )
}
