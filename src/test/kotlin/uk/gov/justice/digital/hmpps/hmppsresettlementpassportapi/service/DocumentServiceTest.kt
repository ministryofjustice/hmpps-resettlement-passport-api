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
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DocumentCategory
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
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
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
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
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

    val copyRequest = CopyObjectRequest.builder()
      .sourceBucket("document-storage")
      .sourceKey(originalDocumentKey.toString())
      .destinationBucket("document-storage")
      .destinationKey(originalDocumentKey.toString() + "_deleted")
      .build()
    val toDelete = ArrayList<ObjectIdentifier>()
    toDelete.add(
      ObjectIdentifier.builder()
        .key(originalDocumentKey.toString())
        .build(),
    )
    toDelete.add(
      ObjectIdentifier.builder()
        .key(pdfDocumentKey.toString())
        .build(),
    )
    val deleteObjectRequest: DeleteObjectsRequest = DeleteObjectsRequest.builder()
      .bucket("document-storage")
      .delete(
        Delete.builder()
          .objects(toDelete).build(),
      )
      .build()

    whenever(documentsRepository.findFirstByNomsIdAndCategory(prisonerEntity.nomsId, DocumentCategory.LICENCE_CONDITIONS, true)).thenReturn(documentsEntity)
    whenever(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    whenever(documentsRepository.findFirstByNomsIdAndCategory(prisonerEntity.nomsId, DocumentCategory.LICENCE_CONDITIONS, false)).thenReturn(documentsEntity)
    whenever(s3Client.copyObject(copyRequest)).thenReturn(any())
    Mockito.lenient().whenever(s3Client.deleteObjects(deleteObjectRequest)).thenReturn(any())
    val response = documentService.deleteUploadDocumentByNomisId("acb", DocumentCategory.LICENCE_CONDITIONS)
    Assertions.assertTrue(response!!.isDeleted)
    Assertions.assertNotNull(response.deletionDate)
  }
}
