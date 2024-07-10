package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.google.common.io.Resources
import io.mockk.every
import io.mockk.mockkStatic
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.jdbc.Sql
import org.springframework.util.MultiValueMap
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DocumentsRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class DocumentStorageIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var documentsRepository: DocumentsRepository

  @Test
  fun `401 unauthorised`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/documents/$nomsId/upload")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `403 forbidden - no roles`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/documents/$nomsId/upload")
      .bodyValue(generateMultiPartFormRequestWeb())
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `when nomsId not found`() {
    val nomsId = "ABC123"
    webTestClient.post()
      .uri("/resettlement-passport/documents/$nomsId/upload")
      .bodyValue(generateMultiPartFormRequestWeb())
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-3.sql")
  fun `Create uploadDocument and getDocument - happy path`() {
    mockkStatic(UUID::class)
    every { UUID.randomUUID().toString() } returns "123"

    val nomsId = "ABC1234"

    webTestClient.post()
      .uri("/resettlement-passport/documents/$nomsId/upload")
      // .body(BodyInserters.fromMultipartData(generateMultiPartFormRequestWeb()))
      .bodyValue(generateMultiPartFormRequestWeb())
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk

    val expectedDocumentEntity =
      DocumentsEntity(
        id = 1,
        prisoner = PrisonerEntity(
          id = 1,
          nomsId = "ABC1234",
          creationDate = LocalDateTime.parse("2023-08-17T12:21:38.709"),
          crn = "123",
          prisonId = "MDI",
          releaseDate = LocalDate.parse("2030-09-12"),
        ),
        documentKey = "ABC1234_123",
        creationDate = LocalDateTime.now(),
      )

    val documentsList = documentsRepository.findAll()
    assertThat(documentsList[0].documentKey).isEqualTo(expectedDocumentEntity.documentKey)

    webTestClient.get()
      .uri("/resettlement-passport/documents/$nomsId/download/ABC1234_123")
      // .body(BodyInserters.fromMultipartData(generateMultiPartFormRequestWeb()))
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `Get Document 401 unauthorised`() {
    val nomsId = "ABC1234"
    webTestClient.get()
      .uri("/resettlement-passport/documents/$nomsId/download/ABC1234_123")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get Document 403 forbidden - no roles`() {
    val nomsId = "ABC1234"
    webTestClient.get()
      .uri("/resettlement-passport/documents/$nomsId/download/ABC1234_123")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get when nomsId not found`() {
    val nomsId = "ABC123"
    webTestClient.get()
      .uri("/resettlement-passport/documents/$nomsId/download/ABC1234_123")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  private fun generateMultiPartFormRequestWeb(): MultiValueMap<String, HttpEntity<*>> {
    val uploadFile = Resources.getResource("testdata/resettlement-passport-delius-api/appointment.json")
    val multipartBodyBuilder = MultipartBodyBuilder()
    val contentsAsResource: ByteArrayResource = object : ByteArrayResource(uploadFile.readBytes()) {
      override fun getFilename(): String {
        return "a.json"
      }
    }
    multipartBodyBuilder.part("file", contentsAsResource)
    multipartBodyBuilder.part("metadata", 1)
    return multipartBodyBuilder.build()
  }
}
