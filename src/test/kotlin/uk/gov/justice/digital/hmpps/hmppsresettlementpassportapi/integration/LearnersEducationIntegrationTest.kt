package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class LearnersEducationIntegrationTest : IntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-learners-education.sql")
  fun `Get Education Learning from curious-api  happy path `() {
    val nomsId = "A8731DY"
    val expectedOutput = readFile("testdata/expectation/learners-education.json")
    curiousApiMockServer.stubGetLearnerEducationListByNomsId(nomsId, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/learner-education?size=1&page=0")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectHeader().contentType("application/json")
      .expectStatus().isOk
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get Education Learning from curious api  unauthorized`() {
    val nomsId = "abc"

    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/learner-education?size=1&page=0")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get Education Learning from curious api  forbidden`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/learner-education?size=1&page=0")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-learners-education.sql")
  fun `Get Education Learning from curious api  Internal Error`() {
    val nomsId = "A8731DY"

    curiousApiMockServer.stubGetLearnerEducationListByNomsId(nomsId, 500)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/learner-education?size=1&page=0")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-learners-education.sql")
  fun `Get Education Learning from curious api when nomsId not found`() {
    val nomsId = "A8731DY"

    curiousApiMockServer.stubGetLearnerEducationListByNomsId(nomsId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/learner-education?size=1&page=0")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").isEqualTo("Prisoner A8731DY not found in learner education curious api")
  }

  @Test
  fun `Get Education Learning from curious api when nomsId not exists in DB`() {
    val nomsId = "abc"

    curiousApiMockServer.stubGetLearnerEducationListByNomsId(nomsId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/learner-education?size=1&page=0")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").isEqualTo("Prisoner with id abc not found in the DB")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-learners-education.sql")
  fun `Get Education Learning from curious-api  happy path use Cache `() {
    val nomsId = "A8731DY"
    val expectedOutput = readFile("testdata/expectation/learners-education.json")
    curiousApiMockServer.stubGetLearnerEducationListByNomsId(nomsId, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/learner-education?size=1&page=0")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectHeader().contentType("application/json")
      .expectStatus().isOk
      .expectBody().json(expectedOutput)

    curiousApiMockServer.resetAll()

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/learner-education?size=1&page=0")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectHeader().contentType("application/json")
      .expectStatus().isOk
      .expectBody().json(expectedOutput, true)
  }
}
