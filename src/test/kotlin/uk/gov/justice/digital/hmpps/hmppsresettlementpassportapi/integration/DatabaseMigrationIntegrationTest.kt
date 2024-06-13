package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.LicenceConditionsChangeAuditRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import javax.sql.DataSource

class DatabaseMigrationIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var dataSource: DataSource

  @Autowired
  lateinit var licenceConditionChangeAuditRepository: LicenceConditionsChangeAuditRepository

  @Autowired
  lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Test
  fun `test database migration - apply license conditions change 1_47`() {
    // Drop database
    val dropDatabasePopulator = ResourceDatabasePopulator(ClassPathResource("testdata/sql/drop-database.sql"))
    dropDatabasePopulator.execute(dataSource)

    // Re-run flyway script up to version 1.37
    Flyway.configure().dataSource(dataSource).target("1.46").load().migrate()

    // Seed database with old data
    val seedDatabasePopulator = ResourceDatabasePopulator(ClassPathResource("testdata/sql/test-database-v1-46.sql"))
    seedDatabasePopulator.execute(dataSource)

    // Re-run flyway script up to latest version
    Flyway.configure().dataSource(dataSource).load().migrate()

    licenceConditionChangeAuditRepository.findAll().forEach { entry ->
      assertThat(entry.seen).isEqualTo(false)
      assertThat(entry.version).isNotNull()
    }
  }
}
