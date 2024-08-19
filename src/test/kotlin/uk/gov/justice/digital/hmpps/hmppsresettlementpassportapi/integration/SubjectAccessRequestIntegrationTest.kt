package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import net.javacrumbs.jsonunit.assertj.assertThatJson
import net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDate

class SubjectAccessRequestIntegrationTest : IntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-sar-data-1.sql")
  fun `SAR without Dates`() {
    val nomsId = "G1458GV"

    val response = webTestClient.get()
      .uri("/subject-access-request?prn=$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    assertThatJson(response)
      .`when`(IGNORING_ARRAY_ORDER)
      .isEqualTo(readFile("testdata/expectation/sar-without-dates.json"))
  }

  @Test
  @Sql("classpath:testdata/sql/seed-sar-data-1.sql")
  fun `SAR with Dates`() {
    val nomsId = "G1458GV"
    val fromYesterday = LocalDate.parse("2023-08-17")
    val fromLastYear = LocalDate.parse("2022-08-17")

    val responseContentYesterday = getSarData(nomsId, fromYesterday)

    assertThatJson(responseContentYesterday)
      .`when`(IGNORING_ARRAY_ORDER)
      .isEqualTo(readFile("testdata/expectation/sar-with-dates-1.json"))

    val responseContent = getSarData(nomsId, fromLastYear)
    assertThatJson(responseContent)
      .`when`(IGNORING_ARRAY_ORDER)
      .isEqualTo(readFile("testdata/expectation/sar-with-dates-2.json"))
  }

  private fun getSarData(nomsId: String, fromYesterday: LocalDate?): String = webTestClient.get()
    .uri("/subject-access-request?prn=$nomsId&fromDate=$fromYesterday")
    .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
    .exchange()
    .expectStatus().isOk
    .expectHeader().contentType("application/json")
    .expectBody(String::class.java)
    .returnResult().responseBody!!

  @Test
  fun `SAR request when nomsId not found`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/subject-access-request?prn=$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus().isEqualTo(204)
  }
}
