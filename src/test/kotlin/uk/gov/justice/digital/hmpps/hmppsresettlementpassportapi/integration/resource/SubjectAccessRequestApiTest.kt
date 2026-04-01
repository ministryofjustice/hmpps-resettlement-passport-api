package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarApiDataTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarApiTestBase
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarReportTest
import java.time.LocalDate

class SubjectAccessRequestApiTest {

  /**
   * Check API data returned from SAR endpoint
   */
  @Nested
  @DisplayName("API data test")
  inner class ApiDataTest :
    TestCase(),
    SarApiDataTest {

    @Test
    @Sql("classpath:testdata/sql/seed-sar-data-1.sql")
    override fun `SAR API should return expected data`() {
      super.`SAR API should return expected data`()
    }
  }

  /**
   * Check report content rendered
   */
  @Nested
  @DisplayName("Report test")
  inner class ReportTest :
    TestCase(),
    SarReportTest {

    @Test
    @Sql("classpath:testdata/sql/seed-sar-data-1.sql")
    override fun `SAR report should render as expected`() {
      super.`SAR report should render as expected`()
    }
  }

  abstract class TestCase(
    sarPrisonNumber: String = "G1458GV",
    sarFromDate: LocalDate? = LocalDate.parse("2023-08-16"),
    sarToDate: LocalDate? = null,
  ) : SubjectAccessRequestApiTestCase(
    sarPrisonNumber = sarPrisonNumber,
    sarFromDate = sarFromDate,
    sarToDate = sarToDate,
  ) {
    // test data setup by `@Sql` at test method
    override fun setupTestData() {}
  }
}

abstract class SubjectAccessRequestApiTestCase(
  protected val sarPrisonNumber: String,
  protected val sarFromDate: LocalDate? = null,
  protected val sarToDate: LocalDate? = null,
) : IntegrationTestBase(),
  SarApiTestBase {
  override fun getCrn(): String? = null
  override fun getFromDate(): LocalDate? = sarFromDate
  override fun getToDate(): LocalDate? = sarToDate
  override fun getPrn(): String? = sarPrisonNumber

  override fun getSarHelper(): SarIntegrationTestHelper = sarIntegrationTestHelper
  override fun getWebTestClientInstance(): WebTestClient = webTestClient
}
