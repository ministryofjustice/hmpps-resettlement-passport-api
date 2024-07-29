package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.google.common.io.Resources
import dev.forkhandles.result4k.get
import io.mockk.every
import io.mockk.mockkStatic
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DocumentResponse
import java.time.LocalDateTime

class DocumentStorageIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2024-07-26T12:00:01")

  @Test
  fun `401 unauthorised`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/upload")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `403 forbidden - no roles`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/upload")
      .body(generateMultiPartFormRequestWeb("testdata/PD1_example.docx", "LICENCE_CONDITIONS"))
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `when nomsId not found`() {
    val nomsId = "ABC123"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/upload")
      .body(generateMultiPartFormRequestWeb("testdata/PD1_example.docx", "LICENCE_CONDITIONS"))
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `Create uploadDocument and getDocument - happy path`() {
    val nomsId = "ABC1234"

    uploadDocument(nomsId)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/1/download")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/pdf")
  }

  @Test
  fun `Get Document 401 unauthorised`() {
    val nomsId = "ABC1234"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/1/download")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get Document 403 forbidden - no roles`() {
    val nomsId = "ABC1234"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/1/download")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get when nomsId not found`() {
    val nomsId = "ABC123"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/1/download")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  private fun generateMultiPartFormRequestWeb(fileName: String, category: String): BodyInserters.MultipartInserter {
    val uploadFile = Resources.getResource(fileName)
    val multipartBodyBuilder = MultipartBodyBuilder()
    val contentsAsResource: ByteArrayResource = object : ByteArrayResource(uploadFile.readBytes()) {
      override fun getFilename(): String = fileName.split("/").last()
    }
    multipartBodyBuilder.part("file", contentsAsResource)
    multipartBodyBuilder.part("metadata", 1)
    multipartBodyBuilder.part("category", category)
    return BodyInserters.fromMultipartData(multipartBodyBuilder.build())
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `Create uploadDocument with document not supported`() {
    val nomsId = "ABC1234"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/upload")
      .bodyValue(generateMultiPartFormRequestWeb("testdata/pop-user-api/pop-user-details.json", "LICENCE_CONDITIONS"))
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `Create uploadDocument with document size exceeded`() {
    val nomsId = "ABC1234"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/upload")
      .bodyValue(generateMultiPartFormRequestWeb("testdata/PD1_example_oversized.docx", "LICENCE_CONDITIONS"))
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `Create uploadDocument with invalid document category`() {
    val nomsId = "ABC1234"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/upload")
      .bodyValue(generateMultiPartFormRequestWeb("testdata/PD1_example.docx", "NOT_EXISTS_CATEGORY"))
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `Get Document when category and documentId given`() {
    val nomsId = "ABC1234"

    uploadDocument(nomsId)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/1/download?category=LICENCE_CONDITIONS")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(200)
      .expectHeader().contentType("application/pdf")
      .expectBody()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `Get Document when Invalid category given`() {
    val nomsId = "ABC1234"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/latest/download?category=LICENCE_CONDITIONS_INVALID")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `Get Latest Document when only category  given`() {
    val nomsId = "ABC1234"

    uploadDocument(nomsId)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/latest/download?category=LICENCE_CONDITIONS")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(200)
      .expectHeader().contentType("application/pdf")
      .expectBody()
  }

  private fun uploadDocument(nomsId: String, filename: String = "testdata/PD1_example.docx") {
    val response = webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/upload")
      .body(generateMultiPartFormRequestWeb(filename, "LICENCE_CONDITIONS"))
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `Get document list`() {
    val nomsId = "ABC1234"
    uploadDocument(nomsId, filename = "testdata/PD1_example.docx")
    uploadDocument(nomsId, filename = "testdata/example-doc.pdf")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents?category=LICENCE_CONDITIONS")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(
        """
        [
          {"id": 2, "fileName": "example-doc.pdf"},
          {"id": 1, "fileName": "PD1_example.docx"},
        ]     
        """.trimIndent(),
      )
  }

  @Test
  fun `get document list gives empty list when no documents`() {
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/0/documents?category=LICENCE_CONDITIONS")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectHeader().contentType("application/json")
      .expectBody()
      .json("[]")
  }

  @Test
  fun `get document list gives 403 when unauthorized`() {
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/0/documents?category=LICENCE_CONDITIONS")
      .headers(setAuthorisation(roles = listOf("SOME_ROLE")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `get document list gives 401 when unauthenticated`() {
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/0/documents?category=LICENCE_CONDITIONS")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `404 on download document when document does not exist`() {
    val nomsId = "ABC1234"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/1/download")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `404 on download document when document does not exist not belong to prisoner`() {
    val nomsId = "ABC1234"
    uploadDocument("DEF4567")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/1/download")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  @Sql("classpath:testdata/sql/seed-document-upload.sql")
  fun `Create uploadDocument pdf and getDocument pdf without conversion - happy path`() {
    val nomsId = "ABC1234"
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val response = webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/upload")
      .body(generateMultiPartFormRequestWeb("testdata/example-doc.pdf", "LICENCE_CONDITIONS"))
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody(DocumentResponse::class.java)
      .value { document -> assertThat(document.value.originalDocumentKey).isEqualTo(document.value.pdfDocumentKey) }

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/documents/1/download")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/pdf")
      .expectBody()
  }
}
