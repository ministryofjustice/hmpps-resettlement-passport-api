package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.google.common.io.Resources
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer

@Disabled("Just used to quickly very libreoffice is working locally")
class LibreOfficeDocumentConversionServiceTest {
  @Test
  fun `converts a document`() {
    val tempDir = Files.createTempDirectory("temp").toFile()
    tempDir.deleteOnExit()
    val s3Client = mockk<S3Client>()
    val documentConversionService = LibreOfficeDocumentConversionService(
      tempDir.toPath(),
      s3Client,
      "bucket",
    )

    val fileData = Resources.getResource("testdata/PD1_example.docx").openStream()
    every { s3Client.putObject(any<Consumer<PutObjectRequest.Builder>>(), any<Path>()) }
      .returns(PutObjectResponse.builder().build())

    documentConversionService.convert(MockMultipartFile("file", fileData), "key")
  }
}
