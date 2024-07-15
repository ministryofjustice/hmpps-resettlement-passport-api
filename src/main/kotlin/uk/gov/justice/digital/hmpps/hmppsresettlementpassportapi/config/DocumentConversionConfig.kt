package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3Client
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.DocumentConversionService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.LibreOfficeDocumentConversionService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.StubDocumentConversionService
import java.nio.file.Paths
import kotlin.io.path.createTempDirectory
private val logger = KotlinLogging.logger {}

@Configuration
class DocumentConversionConfig {
  @Bean
  fun documentConversionService(
    @Value("\${doc.conversion.use.stub:false}") useStub: Boolean,
    @Value("\${doc.conversion.temp.dir.path:-}") tempDirPath: String,
    @Value("\${hmpps.s3.buckets.document-management.bucketName}") bucketName: String,
    s3Client: S3Client,
  ): DocumentConversionService {
    if (useStub) {
      logger.warn { "Using stub document conversion service" }
      return StubDocumentConversionService(s3Client, bucketName)
    }

    val tempDirFile = if (tempDirPath == "-") {
      createTempDirectory("doc-converter")
    } else {
      Paths.get(tempDirPath)
    }
    return LibreOfficeDocumentConversionService(tempDocumentDir = tempDirFile, s3Client = s3Client, bucketName = bucketName)
  }
}
