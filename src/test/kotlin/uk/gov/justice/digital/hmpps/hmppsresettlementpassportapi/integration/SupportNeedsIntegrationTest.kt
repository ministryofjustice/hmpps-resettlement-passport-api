package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway

class SupportNeedsIntegrationTest : IntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-2.sql")
  fun `get support needs summary - happy path`() {
    val expectedOutput = readFile("testdata/expectation/support-needs-summary-1.json")
    val nomsId = "G4161UF"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `get support needs summary - no support needs`() {
    val expectedOutput = readFile("testdata/expectation/support-needs-summary-2.json")
    val nomsId = "G4161UF"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-3.sql")
  fun `get support needs summary - no updates available (only no support needs identified)`() {
    val expectedOutput = readFile("testdata/expectation/support-needs-summary-3.json")
    val nomsId = "G4161UF"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `get support needs summary - no prisoner found`() {
    val nomsId = "G4161UF"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get support needs summary - unauthorised`() {
    val nomsId = "G4161UF"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get support needs summary - forbidden`() {
    val nomsId = "G4161UF"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test get pathway support need summary - happy path`() {
    val expectedOutput = readFile("testdata/expectation/pathway-support-needs-summary-1.json")
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/summary")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `get pathway support needs summary - no prisoner found`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/summary")
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get pathway support needs summary - pathway invalid`() {
    val nomsId = "G4161UF"
    val pathway = "NOT_A_PATHWAY"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/summary")
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `get pathway support needs summary - unauthorised`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/summary")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get pathway support needs summary - forbidden`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/summary")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test get pathway support need updates - happy path with defaults`() {
    val expectedOutput = readFile("testdata/expectation/pathway-support-needs-updates-1.json")
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test get pathway support need updates - happy path with query params`() {
    val expectedOutput = readFile("testdata/expectation/pathway-support-needs-updates-2.json")
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates?page=0&size=5&sort=createdDate,ASC&filterByPrisonerSupportNeedId=1")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `get pathway support needs updates - no prisoner found`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates")
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get pathway support needs updates - pathway invalid`() {
    val nomsId = "G4161UF"
    val pathway = "NOT_A_PATHWAY"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates")
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `get pathway support needs updates - unauthorised`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get pathway support needs updates - forbidden`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test get pathway support needs - happy path`() {
    val expectedOutput = readFile("testdata/expectation/pathway-support-needs-1.json")
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `get pathway support needs - no prisoner found`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway")
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get pathway support needs - pathway invalid`() {
    val nomsId = "G4161UF"
    val pathway = "NOT_A_PATHWAY"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway")
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `get pathway support needs - unauthorised`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get pathway support needs - forbidden`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test get a support need - happy path`() {
    val expectedOutput = readFile("testdata/expectation/get-a-support-need-1.json")
    val nomsId = "G4161UF"
    val prisonerSupportNeedId = 1
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/prisoner-need/$prisonerSupportNeedId")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `get a support need - no prisoner found`() {
    val nomsId = "G4161UF"
    val prisonerSupportNeedId = 1
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/prisoner-need/$prisonerSupportNeedId")
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get a support need - unauthorised`() {
    val nomsId = "G4161UF"
    val prisonerSupportNeedId = 1
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/prisoner-need/$prisonerSupportNeedId")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get a support need - forbidden`() {
    val nomsId = "G4161UF"
    val prisonerSupportNeedId = 1
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/prisoner-need/$prisonerSupportNeedId")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }
}
