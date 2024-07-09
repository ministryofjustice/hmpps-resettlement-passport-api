package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import dev.forkhandles.result4k.valueOrNull
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isA
import org.mockito.kotlin.whenever
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
import java.util.UUID

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
  private lateinit var documentConversionService: LibreOfficeDocumentConversionService
  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    documentService = DocumentService(s3Client, prisonerRepository, documentsRepository, virusScanner, documentConversionService, "document-storage")
  }

  @Test
  fun `test scanAndStoreDocument - returns document`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val htmlDocumentKey = UUID.randomUUID()
    val documentsEntity = DocumentsEntity(1, 1, "", htmlDocumentKey, fakeNow)
    val file = MockMultipartFile(
      "file",
      "hello.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "Hello, World!".toByteArray(),
    )

    whenever(virusScanner.scan(file.bytes)).thenReturn(VirusScanResult.NoVirusFound)
    whenever(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    whenever(documentsRepository.save(any())).thenReturn(documentsEntity)
    whenever(documentConversionService.convert(eq(file), isA<String>())).thenReturn(htmlDocumentKey)
    val response = documentService.processDocument("acb", file)
    Assertions.assertEquals(documentsEntity, response.valueOrNull())
  }

  @Test
  fun `test getDocumentByNomisIdAndDocumentId - returns document`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val documentsEntity = DocumentsEntity(1, 1, "acb_123455", UUID.randomUUID(), fakeNow)
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

    whenever(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    whenever(documentsRepository.findByPrisonerIdAndOriginalDocumentKey(1, "acb_123455")).thenReturn(documentsEntity)
    whenever(s3Client.getObject(request)).thenReturn(res)
    val response = documentService.getDocumentByNomisIdAndDocumentId("acb", "acb_123455")
    Assertions.assertEquals(file.size.toInt(), response.size)
  }
}
