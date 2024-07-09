package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

class DocumentConversionService(
  private val tempDocumentDir: File,
  private val s3Client: S3Client,
  private val bucketName: String,
) {

  fun convert(multipartFile: MultipartFile, originalBucketKey: String): UUID {
    val tempFile = tempDocumentDir.resolve(originalBucketKey)
    tempFile.outputStream().use { outputStream ->
      multipartFile.inputStream.use { inputStream ->
        inputStream.copyTo(outputStream)
      }
    }
    val elapsed = measureTime {
      val process = Runtime.getRuntime().exec(
        arrayOf(
          "soffice",
          "--headless",
          "--convert-to",
          "html",
          "--outdir",
          tempDocumentDir.absolutePath,
          tempFile.absolutePath,
        ),
      )
      process.waitFor(1, TimeUnit.MINUTES)

      logger.info { "Converted document using libre office exit code: ${process.exitValue()}, stdout: ${process.inputStream.readAllBytes().toString(Charsets.UTF_8)}" }
    }
    logger.info { "document was processed in $elapsed" }

    val convertedKey = UUID.randomUUID()
    s3Client.putObject(
      { request: PutObjectRequest.Builder ->
        request.bucket(bucketName)
        request.key(convertedKey.toString())
      },
      Path.of(tempFile.absolutePath),
    )

    tempFile.delete()
    return convertedKey
  }
}
