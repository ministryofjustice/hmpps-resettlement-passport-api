package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Counter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.apache.commons.codec.Resources
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ClientTimeoutException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.io.path.deleteExisting
import kotlin.io.path.name
import kotlin.io.path.pathString


private val logger = KotlinLogging.logger {}

interface DocumentConversionService {
   fun convert(multipartFile: MultipartFile): UUID?
}

class LibreOfficeDocumentConversionService(
  private val tempDocumentDir: Path,
  private val s3Client: S3Client,
  private val bucketName: String,
  private val gotenbergWebClient: WebClient

) : DocumentConversionService {

  override fun convert(multipartFile: MultipartFile): UUID? {
    //val gotenbergWebClient = WebClient.builder().baseUrl("http://localhost:9091").build()
    val tempFileId = UUID.randomUUID().toString()
    val tempFile = tempDocumentDir.resolve(tempFileId)
    multipartFile.transferTo(tempFile)
    log.info("Temp file size ${tempFile.toFile().absolutePath}")
    log.info("Temp filename ${tempFile.name}")

    val uploadFile = multipartFile.resource.contentAsByteArray
    log.info("uploadFile size ${uploadFile.size}")
    val responseType = object : ParameterizedTypeReference<DocumentUploadResponse>() {}
    val fileMap: MultiValueMap<String, String> = LinkedMultiValueMap()
    val contentDisposition = ContentDisposition
      .builder("form-data")
      .name("files")
      .filename(tempFile.name)
      .build()
    fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
    val fileEntity: HttpEntity<ByteArray> = HttpEntity(uploadFile, fileMap)
    val contentType = MediaType.MULTIPART_FORM_DATA.toString()
    log.info("Media Type $contentType")
   val response = gotenbergWebClient.post()
      .uri("/forms/libreoffice/convert")
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .header("Service-Name", "Resettlement Passport")
      .body(generateMultiPartFormRequestWeb(multipartFile))
      //.bodyValue(multipartFile)
      .retrieve()
      .toEntity(ByteArray::class.java)
      .exponentialBackOffRetry()
      .block()

    if (response != null) {
      log.info("Response status ${response.body.size}")
    }

    val convertedKey = UUID.randomUUID()
    val convertedPath = Paths.get(tempFile.pathString + ".pdf")
   /* if (response != null) {
        Files.write(convertedPath, response.data)
    }

    s3Client.putObject(
      { request: PutObjectRequest.Builder ->
        request.bucket(bucketName)
        request.key(convertedKey.toString())
      },
      convertedPath,
    )

    */

    tempFile.cleanupQuietly()
    convertedPath.cleanupQuietly()
    return convertedKey

}



private fun Path.cleanupQuietly() {
  try {
    this.deleteExisting()
  } catch (e: Exception) {
    logger.warn(e) { "Failed to cleanup ${this.pathString}" }
  }
}

  private fun generateMultiPartFormRequestWeb(file: MultipartFile ): BodyInserters.MultipartInserter {
    val multipartBodyBuilder = MultipartBodyBuilder()
    multipartBodyBuilder.part("file", file)
    return BodyInserters.fromMultipartData(multipartBodyBuilder.build())
  }



class StubDocumentConversionService(private val s3Client: S3Client, private val bucketName: String) : DocumentConversionService {
  override  fun convert(multipartFile: MultipartFile): UUID? {
    val bucketKey = UUID.randomUUID()

    s3Client.putObject(
      { request: PutObjectRequest.Builder ->
        request.bucket(bucketName)
        request.key(bucketKey.toString())
      },
      RequestBody.fromString(
        """
        <html>
          <body>
            <p>Created by stub conversion service</p>
          </body>
        </html>
        """.trimIndent(),
      ),
    )

    return bucketKey
  }


}

  data class DocumentUploadResponse(
    val data: ByteArray,
  )

  /*private fun handleTimeoutException(exception: Throwable?, endPoint: String) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()
        throw ClientTimeoutException(
          "Document Management client - $endPoint endpoint",
          "No response within 3000 seconds",
        )
      }
    }
  }*/


  /*fun convertOfficeFile(officeFile: GotenbergFile): GotenbergFile {
    try {
      val serverUrl: String = baseUrl + "/convert/office"
      val post: HttpPost = HttpPost(serverUrl)
      val builder: MultipartEntityBuilder = MultipartEntityBuilder.create()
      builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
      builder.addBinaryBody("file", officeFile.data, ContentType.APPLICATION_OCTET_STREAM, officeFile.name)

      val entity: HttpEntity<*> = builder.build()
      post.setEntity(entity)

      val client: CloseableHttpClient = HttpClients.createDefault()
      val response: HttpResponse = client.execute(post)

      val out = ByteArrayOutputStream()
      response.getEntity().writeTo(out)
      return GotenbergFile(out.toByteArray(), "out.pdf")
    } catch (e: java.lang.Exception) {
      throw RuntimeException(e)
    }
  }*/
}
