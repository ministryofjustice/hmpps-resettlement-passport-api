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
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.http.AbortableInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DocumentsRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime
import java.util.*

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
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val pdfDocumentKey = UUID.randomUUID()
    val originalDocumentKey = UUID.randomUUID()
    val documentsEntity = DocumentsEntity(1, 1, originalDocumentKey, pdfDocumentKey, fakeNow, DocumentCategory.LICENCE_CONDITIONS, "Filename.doc")
    val file = MockMultipartFile(
      "file",
      "hello.doc",
      MediaType.TEXT_PLAIN_VALUE,
      "Hello, World!".toByteArray(),
    )

    whenever(virusScanner.scan(file.bytes)).thenReturn(VirusScanResult.NoVirusFound)
    whenever(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    whenever(documentsRepository.save(any())).thenReturn(documentsEntity)
    whenever(documentConversionService.convert(eq(file))).thenReturn(pdfDocumentKey)
    val response = documentService.processDocument("acb", file, null, DocumentCategory.LICENCE_CONDITIONS)
    Assertions.assertEquals(documentsEntity, response.valueOrNull())
  }

  @Test
  fun `test getLatestDocumentByNomisId - returns document`() {
    val pdfDocumentKey = UUID.randomUUID()
    val originalDocumentKey = UUID.randomUUID()
    val documentsEntity = DocumentsEntity(1, 1, originalDocumentKey, pdfDocumentKey, fakeNow, DocumentCategory.LICENCE_CONDITIONS, "Filename.doc")

    val file = MockMultipartFile(
      "file",
      "hello.doc",
      MediaType.TEXT_PLAIN_VALUE,
      "Hello, World!".toByteArray(),
    )
    val res = ResponseInputStream(
      GetObjectResponse.builder().build(),
      AbortableInputStream.create(file.inputStream),
    )

    val request = GetObjectRequest.builder()
      .bucket("document-storage")
      .key(pdfDocumentKey.toString())
      .build()

    whenever(documentsRepository.findFirstByNomsIdAndCategory("acb", DocumentCategory.LICENCE_CONDITIONS, false)).thenReturn(documentsEntity)
    whenever(s3Client.getObject(request)).thenReturn(res)
    val response = documentService.getLatestDocumentByCategory("acb", DocumentCategory.LICENCE_CONDITIONS)
    Assertions.assertEquals(file.size.toInt(), response.readAllBytes().size)
  }

  @Test
  fun `test getDocumentByNomisIdAndDocumentId - returns document`() {
    val pdfDocumentKey = UUID.randomUUID()
    val originalDocumentKey = UUID.randomUUID()
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val documentsEntity = DocumentsEntity(1, 1, originalDocumentKey, pdfDocumentKey, fakeNow, DocumentCategory.LICENCE_CONDITIONS, "Filename.doc")
    val file = MockMultipartFile(
      "file",
      "hello.doc",
      MediaType.TEXT_PLAIN_VALUE,
      "Hello, World!".toByteArray(),
    )
    val res = ResponseInputStream(
      GetObjectResponse.builder().build(),
      AbortableInputStream.create(file.inputStream),
    )

    val request = GetObjectRequest.builder()
      .bucket("document-storage")
      .key(pdfDocumentKey.toString())
      .build()

    whenever(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    whenever(documentsRepository.findByPrisonerIdAndId(prisonerEntity.id!!, 1)).thenReturn(documentsEntity)
    whenever(s3Client.getObject(request)).thenReturn(res)
    val response = documentService.getDocumentByNomisIdAndDocumentId("acb", 1)
    Assertions.assertEquals(file.size.toInt(), response.readAllBytes().size)
  }

  @Test
  fun `test deleteDocumentByNomisId - returns document`() {
    val pdfDocumentKey = UUID.randomUUID()
    val originalDocumentKey = UUID.randomUUID()
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val documentsEntity = DocumentsEntity(1, 1, originalDocumentKey, pdfDocumentKey, fakeNow, DocumentCategory.LICENCE_CONDITIONS, "Filename.doc")
    val list = mutableListOf<DocumentsEntity>()
    list.add(documentsEntity)
    val file = MockMultipartFile(
      "file",
      "hello.doc",
      MediaType.TEXT_PLAIN_VALUE,
      "Hello, World!".toByteArray(),
    )
    val res = ResponseInputStream(
      GetObjectResponse.builder().build(),
      AbortableInputStream.create(file.inputStream),
    )

    val request = GetObjectRequest.builder()
      .bucket("document-storage")
      .key(pdfDocumentKey.toString())
      .build()

    whenever(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    whenever(documentsRepository.findFirstByNomsIdAndCategory(prisonerEntity.nomsId, DocumentCategory.LICENCE_CONDITIONS, false)).thenReturn(documentsEntity)
    val response = documentService.deleteUploadDocumentByNomisId("acb", DocumentCategory.LICENCE_CONDITIONS)
    documentsEntity.isDeleted = true
    documentsEntity.deletionDate = fakeNow
    Mockito.verify(documentsRepository).save(documentsEntity)
  }

  @Test
  fun `test getDocuments should return data from repository`() {
    val currentDate = LocalDateTime.now()
    val originalDocumentKey = UUID.randomUUID()
    val pdfDocumentKey = UUID.randomUUID()
    val data = listOf(
      DocumentsEntity(
        id = null,
        prisonerId = 1,
        originalDocumentKey = originalDocumentKey,
        pdfDocumentKey = pdfDocumentKey,
        creationDate = currentDate,
        category = DocumentCategory.LICENCE_CONDITIONS,
        originalDocumentFileName = "license2.pdf",
        isDeleted = false,
        deletionDate = null,
      ),
    )

    val expected = listOf(DocumentService.DocumentsSarContent(
      originalDocumentKey = originalDocumentKey,
      pdfDocumentKey = pdfDocumentKey,
      creationDate = currentDate,
      category = DocumentCategory.LICENCE_CONDITIONS,
      originalDocumentFileName = "license2.pdf",
    ))

    Mockito.`when`(documentsRepository.findAllByPrisonerIdAndCreationDateBetween(any(), any(), any())).thenReturn(data)

    val response = documentService.getDocuments(1, LocalDateTime.now(), LocalDateTime.now())
    Assertions.assertEquals(expected, response)
  }
}
