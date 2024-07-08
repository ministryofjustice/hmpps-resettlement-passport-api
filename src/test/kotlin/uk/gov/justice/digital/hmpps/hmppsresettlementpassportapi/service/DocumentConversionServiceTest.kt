package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.google.common.io.Resources
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DocumentsRepository
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer

class DocumentConversionServiceTest {
  @Test
  fun `converts a document`() {
    val tempDir = Files.createTempDirectory("temp").toFile()
    tempDir.deleteOnExit()
    val s3Client = mockk<S3Client>()
    val documentConversionService = DocumentConversionService(
      tempDir,
      s3Client,
      mockk<DocumentsRepository>(),
    )

    val fileData = Resources.getResource("testdata/PD1_example.docx").openStream()
    every { s3Client.putObject(any<Consumer<PutObjectRequest.Builder>>(), any<Path>()) }
      .returns(PutObjectResponse.builder().build())

    documentConversionService.convert(MockMultipartFile("file", fileData), "key")
  }
}
