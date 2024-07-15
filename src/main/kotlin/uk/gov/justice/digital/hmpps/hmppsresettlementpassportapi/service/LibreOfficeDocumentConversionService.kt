package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteExisting
import kotlin.io.path.pathString
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

interface DocumentConversionService {
  fun convert(multipartFile: MultipartFile, originalBucketKey: String): UUID?
}

class LibreOfficeDocumentConversionService(
  private val tempDocumentDir: Path,
  private val s3Client: S3Client,
  private val bucketName: String,
) : DocumentConversionService {

  override fun convert(multipartFile: MultipartFile, originalBucketKey: String): UUID? {
    val tempFile = tempDocumentDir.resolve(originalBucketKey)
    multipartFile.transferTo(tempFile)
    val (exitCode, elapsed) = measureTimedValue {
      val process = Runtime.getRuntime().exec(
        arrayOf(
          "soffice",
          "--headless",
          "--convert-to",
          "html",
          "--outdir",
          tempDocumentDir.pathString,
          tempFile.pathString,
        ),
      )
      process.waitFor(1, TimeUnit.MINUTES)
      logger.info { "Libre office stdout: ${process.inputStream.readAllBytes().toString(Charsets.UTF_8)}" }
      process.exitValue()
    }
    logger.info { "Converted document using libre office exit code: $exitCode" }
    logger.info { "document was processed in $elapsed" }

    if (exitCode != 0) {
      logger.warn { "Failed to convert document" }
      return null
    }

    val convertedKey = UUID.randomUUID()
    val convertedPath = Paths.get(tempFile.pathString + ".html")
    s3Client.putObject(
      { request: PutObjectRequest.Builder ->
        request.bucket(bucketName)
        request.key(convertedKey.toString())
      },
      convertedPath,
    )

    tempFile.cleanupQuietly()
    convertedPath.cleanupQuietly()
    return convertedKey
  }
}

private fun Path.cleanupQuietly() {
  try {
    this.deleteExisting()
  } catch (e: Exception) {
    logger.warn(e) { "Failed to cleanup ${this.pathString}" }
  }
}

class StubDocumentConversionService(private val s3Client: S3Client, private val bucketName: String) : DocumentConversionService {
  override fun convert(multipartFile: MultipartFile, originalBucketKey: String): UUID? {
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
