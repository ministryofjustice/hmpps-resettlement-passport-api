package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DocumentsRepository
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.time.measureTime

class DocumentConversionService(
  private val tempDocumentDir: File,
  private val s3Client: S3Client,
  private val documentsRepository: DocumentsRepository,
) {

  fun convert(multipartFile: MultipartFile, originalBucketKey: String) {
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
      println(process.exitValue())
      println(process.inputStream.readAllBytes().toString(Charsets.UTF_8))
    }
    println(elapsed)

    val response = s3Client.putObject(
      { request: PutObjectRequest.Builder ->
        request.key(originalBucketKey + "html")
      },
      Path.of(tempFile.absolutePath),
    )

    tempFile.delete()
  }
}
