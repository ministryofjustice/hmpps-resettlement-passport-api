package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import java.io.File


class PrisonersIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Get All Prisoners happy path`() {
    val expectedOutput = File("src/test/resources/testdata/prisoners/prisoners.json").inputStream().readBytes().toString(Charsets.UTF_8)
    val prisonId ="MDI"
    offenderSearchApiMockServer.stubGetPrisonersList(prisonId, 500,0, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=0&size=10&sort=releaseDate,DESC")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get All Prisoners sort by releaseDate ascending happy path`() {
    val expectedOutput = File("src/test/resources/testdata/prisoners/prisoners-ascending.json").inputStream().readBytes().toString(Charsets.UTF_8)
    val prisonId ="MDI"
    offenderSearchApiMockServer.stubGetPrisonersList(prisonId, 500,0,200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=0&size=10&sort=releaseDate,ASC")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get All Prisoners unauthorized`() {
    val prisonId ="MDI"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get All Prisoners  Internal Error`() {
    val prisonId ="MDI"
    offenderSearchApiMockServer.stubGetPrisonersList(prisonId, 500,0,500)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get All Prisoners  negative Page number`() {
    val prisonId ="MDI"
    offenderSearchApiMockServer.stubGetPrisonersList(prisonId,500,0,404)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=-1&size=10&sort=releaseDate,ASC")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("No Data found")
  }

  @Test
  fun `Get All Prisoners  negative Page size`() {
    val prisonId ="MDI"
    offenderSearchApiMockServer.stubGetPrisonersList(prisonId,500,0,404)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=1&size=-1&sort=releaseDate,ASC")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("No Data found")
  }

  @Test
  fun `Get All Prisoners with no page and no size as Internal Error`() {
    val prisonId ="MDI"
    offenderSearchApiMockServer.stubGetPrisonersList(prisonId,500,0,500)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?sort=releaseDate,ASC")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get All Prisoners with no sort as Internal Error`() {
    val prisonId ="MDI"
    offenderSearchApiMockServer.stubGetPrisonersList(prisonId,500,0,500)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=1&sie=10")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get All Prisoners with sort invalid`() {
    val expectedOutput = File("src/test/resources/testdata/prisoners/prisoners.json").inputStream().readBytes().toString(Charsets.UTF_8)
    val prisonId ="MDI"
    offenderSearchApiMockServer.stubGetPrisonersList(prisonId, 500,0, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=0&size=10&sort=xxxxx")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("No Data found")
  }

}
