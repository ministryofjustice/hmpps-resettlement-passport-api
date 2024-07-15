package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.google.common.io.Resources
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.jdbc.Sql
import org.springframework.util.MultiValueMap
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DocumentsRepository
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
    val nomsId = "ABC1234"

    webTestClient.post()
      .uri("/resettlement-passport/documents/$nomsId/upload")
      .bodyValue(generateMultiPartFormRequestWeb())
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk

    webTestClient.get()
      .uri("/resettlement-passport/documents/$nomsId/download/1")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk

    webTestClient.get()
      .uri("/resettlement-passport/documents/$nomsId/html/1")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `Get Document 401 unauthorised`() {
    val nomsId = "ABC1234"
    webTestClient.get()
      .uri("/resettlement-passport/documents/$nomsId/download/1")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get Document 403 forbidden - no roles`() {
    val nomsId = "ABC1234"
    webTestClient.get()
      .uri("/resettlement-passport/documents/$nomsId/download/1")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get when nomsId not found`() {
    val nomsId = "ABC123"
    webTestClient.get()
      .uri("/resettlement-passport/documents/$nomsId/download/1")
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
