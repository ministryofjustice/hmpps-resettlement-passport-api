package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import dev.forkhandles.result4k.valueOrNull
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.http.AbortableInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DocumentsRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class DocumentServiceTest {
  private lateinit var documentService: DocumentService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var documentsRepository: DocumentsRepository

  @Mock
  private lateinit var virusScanner: VirusScanner

  @Mock
  private lateinit var s3Client: S3Client

  @Mock
  private lateinit var s3Object: GetObjectRequest

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    documentService = DocumentService(s3Client, prisonerRepository, documentsRepository, virusScanner, "document-storage")
  }

  @Test
  fun `test scanAndStoreDocument - returns document`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val documentsEntity = DocumentsEntity(1, prisonerEntity, "", fakeNow)
    val file = MockMultipartFile(
      "file",
      "hello.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "Hello, World!".toByteArray(),
    )

    Mockito.`when`(virusScanner.scan(file.bytes)).thenReturn(VirusScanResult.NoVirusFound)
    Mockito.`when`(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    Mockito.`when`(documentsRepository.save(any())).thenReturn(documentsEntity)
    val response = documentService.processDocument("acb", file)
    Assertions.assertEquals(documentsEntity, response.valueOrNull())
  }

  @Test
  fun `test getDocumentByNomisIdAndDocumentId - returns document`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val documentsEntity = DocumentsEntity(1, prisonerEntity, "acb_123455", fakeNow)
    val file = MockMultipartFile(
      "file",
      "hello.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "Hello, World!".toByteArray(),
    )
    val res = ResponseInputStream(
      GetObjectResponse.builder().build(),
      AbortableInputStream.create(file.inputStream),
    )

    val request = GetObjectRequest.builder()
      .bucket("document-storage")
      .key("acb_123455")
      .build()

    Mockito.`when`(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    Mockito.`when`(documentsRepository.findByPrisonerAndDocumentKey(prisonerEntity, "acb_123455")).thenReturn(documentsEntity)
    Mockito.`when`(s3Client.getObject(request)).thenReturn(res)
    val response = documentService.getDocumentByNomisIdAndDocumentId("acb", "acb_123455")
    Assertions.assertEquals(file.size.toInt(), response.size)
  }
}
