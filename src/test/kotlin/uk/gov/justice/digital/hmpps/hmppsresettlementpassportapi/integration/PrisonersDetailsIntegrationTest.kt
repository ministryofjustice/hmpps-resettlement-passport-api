package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.google.common.io.Resources
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CvlApiMockServer
import java.time.LocalDate
import java.time.Period
import java.util.*

class PrisonersDetailsIntegrationTest : IntegrationTestBase() {
  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get Prisoner Details happy path`() {
    var expectedOutput = readFile("testdata/expectation/prisoner-details.json")
    val dob = LocalDate.of(1982, 10, 24)
    val age = Period.between(dob, LocalDate.now()).years
    expectedOutput = expectedOutput.replace("REPLACE_WITH_AGE", "$age")
    val nomsId = "123"
    offenderSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    communityApiMockServer.stubGetCrnFromNomsId(nomsId, "abc")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get Prisoner Details unauthorized`() {
    val nomsId = "G4274GN"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get Prisoner Details when nomisId not found`() {
    val nomsId = "abc"

    offenderSearchApiMockServer.stubGetPrisonerDetails(nomsId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  @Test
  fun `Get image for Prisoner happy path`() {
    val nomsId = "abc"
    val imageId = "1313058"
    val expectedOutput = Base64.getDecoder().decode(CvlApiMockServer.TEST_IMAGE_BASE64)

    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerFacialImage(imageId, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/image/$imageId")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("image/jpeg")
      .expectBody<ByteArray>().consumeWith {
        Assertions.assertArrayEquals(expectedOutput, it.responseBody)
      }
  }

  @Test
  fun `Get Prisoner image not found`() {
    val nomsId = "abc"
    val imageId = "1313058"
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerFacialImage(imageId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/image/$imageId")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - Image not found")
      .jsonPath("developerMessage").isEqualTo("Image not found")
      .jsonPath("moreInfo").isEmpty
  }
}
