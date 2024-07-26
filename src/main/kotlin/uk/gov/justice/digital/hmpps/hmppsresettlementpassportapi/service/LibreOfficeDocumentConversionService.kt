package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Path
import java.util.UUID

private val logger = KotlinLogging.logger {}

interface DocumentConversionService {
  fun convert(multipartFile: MultipartFile): UUID?
}

class LibreOfficeDocumentConversionService(
  private val tempDocumentDir: Path,
  private val s3Client: S3Client,
  private val bucketName: String,
  private val gotenbergWebClient: WebClient,

) : DocumentConversionService {

  override fun convert(multipartFile: MultipartFile): UUID? {
    val response = gotenbergWebClient.post()
      .uri("/forms/libreoffice/convert")
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .header("Service-Name", "Resettlement Passport")
      .body(generateMultiPartFormRequestWeb(multipartFile))
      .retrieve()
      .toEntity(ByteArray::class.java)
      .exponentialBackOffRetry()
      .block()

    val convertedKey = UUID.randomUUID()

    if (response != null) {
      s3Client.putObject(
        { request: PutObjectRequest.Builder ->
          request.bucket(bucketName)
          request.key(convertedKey.toString())
        },
        RequestBody.fromBytes(response.body),
      )
    }

    return convertedKey
  }

  private fun generateMultiPartFormRequestWeb(file: MultipartFile): BodyInserters.MultipartInserter {
    val multipartBodyBuilder = MultipartBodyBuilder()
    multipartBodyBuilder.part("file", file.resource)
    return BodyInserters.fromMultipartData(multipartBodyBuilder.build())
  }

  class StubDocumentConversionService(private val s3Client: S3Client, private val bucketName: String) : DocumentConversionService {
    override fun convert(multipartFile: MultipartFile): UUID? {
      val bucketKey = UUID.randomUUID()

      s3Client.putObject(
        { request: PutObjectRequest.Builder ->
          request.bucket(bucketName)
          request.key(bucketKey.toString())
        },
        RequestBody.fromString(
          """
        PDF CONTENT
          """.trimIndent(),
        ),
      )

      return bucketKey
    }
  }
}
