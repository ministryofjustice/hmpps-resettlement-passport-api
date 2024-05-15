package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class WatchlistIntegrationTest:  IntegrationTestBase() {

 @Test
 @Sql("classpath:testdata/sql/seed-assessment-1.sql")
 fun `Create watchlist - happy path`(){
   val nomsId = "123"
   webTestClient.post()
     .uri("/resettlement-passport/prisoner/$nomsId/watch")
     .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
     .exchange()
     .expectStatus().isOk
 }

  @Test
  fun `Create watchlist - nomsId not found in database`(){
   val nomsId = "G1458GV"
   webTestClient.post()
     .uri("/resettlement-passport/prisoner/$nomsId/watch")
     .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
     .exchange()
     .expectStatus().isNotFound
  }

  @Test
  fun `Create watchlist - Cannot get name from auth token`(){
    val nomsId = "G1458GV"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/watch")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().is4xxClientError
      .expectHeader().contentType("application/json")

  }
}

