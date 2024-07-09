package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3Client
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.DocumentConversionService
import java.io.File
import java.nio.file.Files

@Configuration
class DocumentConversionConfig {
  @Bean
  fun documentConversionService(
    @Value("\${temp.dir.path:-}") tempDirPath: String,
    @Value("\${hmpps.s3.buckets.document-management.bucketName}") bucketName: String,
    s3Client: S3Client,
  ): DocumentConversionService {
    val tempDirFile = if (tempDirPath == "-") {
      val tempDir = Files.createTempDirectory("docs").toFile()
      tempDir.deleteOnExit()
      tempDir
    } else {
      File(tempDirPath)
    }
    return DocumentConversionService(tempDocumentDir = tempDirFile, s3Client = s3Client, bucketName = bucketName)
  }
}
