package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DocumentsRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.VirusScanResult.NoVirusFound
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.VirusScanResult.VirusFound
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

private val logger = KotlinLogging.logger {}
private val allowableFileExtensions = setOf("docx", "pdf", "doc")

@Service
class DocumentService(
  private val s3Client: S3Client,
  private val prisonerRepository: PrisonerRepository,
  private val documentsRepository: DocumentsRepository,
  private val virusScanner: VirusScanner,
  private val documentConversionService: DocumentConversionService,
  @Value("\${hmpps.s3.buckets.document-management.bucketName}") private val bucketName: String,
) {

  @Transactional
  fun processDocument(
    nomsId: String,
    document: MultipartFile,
    originalFilename: String?,
    category: DocumentCategory,
  ): Result<DocumentsEntity, VirusFound> = forExistingPrisoner(nomsId) {
    val filename: String =
      originalFilename ?: document.originalFilename ?: throw ValidationException("filename is required")

    val extension = filename.substringAfterLast(".", "")
    if (extension !in allowableFileExtensions) {
      logger.info { "Received unsupported filename $filename" }
      throw ValidationException("Unsupported document format, only .doc or pdf allowed")
    }

    when (val virusScanResult = virusScanner.scan(document.bytes)) {
      NoVirusFound -> Success(convertAndStoreDocument(nomsId, document, category, filename))
      is VirusFound -> {
        logger.info { VirusFoundEvent(nomsId, virusScanResult.foundViruses) }
        Failure(virusScanResult)
      }
    }
  }

  @Transactional
  fun convertAndStoreDocument(
    nomsId: String,
    document: MultipartFile,
    category: DocumentCategory,
    originalFilename: String,
  ): DocumentsEntity {
    // using nomsId to find the prisoner entity
    val prisoner = findPrisonerByNomsId(nomsId)

    val key = UUID.randomUUID()
    var convertedDocumentKey = key
    uploadDocumentToS3(document, bucketName, key)

    if (!originalFilename.endsWith("pdf", true)) {
      convertedDocumentKey = documentConversionService.convert(document)
    }

    val documents = DocumentsEntity(
      prisonerId = prisoner.id!!,
      originalDocumentKey = key,
      pdfDocumentKey = convertedDocumentKey,
      creationDate = LocalDateTime.now(),
      category = category,
      originalDocumentFileName = originalFilename,
    )

    val docEntity = documentsRepository.save(documents)
    logger.info { "Document key ${docEntity.originalDocumentKey} and id is ${docEntity.id}" }
    return docEntity
  }

  fun findPrisonerByNomsId(nomsId: String): PrisonerEntity {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    return prisoner
  }

  fun uploadDocumentToS3(file: MultipartFile, bucketName: String?, key: UUID?) {
    val request = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key.toString())
      .build()

    s3Client.putObject(request, RequestBody.fromInputStream(file.inputStream, file.size))
  }

  fun getDocument(documentKey: String): InputStream {
    val request = GetObjectRequest.builder()
      .bucket(bucketName)
      .key(documentKey)
      .build()

    return s3Client.getObject(request)
  }

  fun getDocumentByNomisIdAndDocumentId(nomsId: String, documentId: Long): InputStream {
    val prisoner = findPrisonerByNomsId(nomsId)
    try {
      val document: DocumentsEntity = (documentsRepository.findByPrisonerIdAndId(prisoner.id!!, documentId) ?: ResourceNotFoundException("Document with id $documentId not found for prisoner with id $nomsId")) as DocumentsEntity

      val documentKey = document.pdfDocumentKey
      if (prisoner.id != document.prisonerId) {
        throw ResourceNotFoundException("Document with id $documentId not found")
      }
      return getDocument(documentKey.toString())
    } catch (ex: Exception) {
      throw ResourceNotFoundException("Document with id $documentId not found")
    }
  }

  fun getLatestDocumentByCategory(nomsId: String, category: DocumentCategory): InputStream {
    val latest = documentsRepository.findFirstByNomsIdAndCategory(nomsId, category, false)
      ?: throw ResourceNotFoundException("No $category documents found for $nomsId")
    return getDocument(latest.pdfDocumentKey.toString())
  }

  private inline fun <reified T : Any?> forExistingPrisoner(nomsId: String, fn: () -> T): T {
    prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in the database.")
    return fn()
  }

  data class VirusFoundEvent(val nomsId: String, val foundViruses: Map<String, Collection<String>>)

  fun listDocuments(nomsId: String, category: DocumentCategory?): Collection<DocumentsEntity> = if (category == null) {
    documentsRepository.findAllByNomsId(nomsId)
  } else {
    documentsRepository.findAllByNomsIdAndCategory(nomsId, category)
  }

  fun listDocuments(prisonerId: Long, startDate: LocalDate, endDate: LocalDate): List<DocumentsEntity> {
    val from = startDate.atStartOfDay()
    val to = endDate.atTime(LocalTime.MAX)
    return documentsRepository.findAllByPrisonerIdAndCreationDateBetween(prisonerId, from, to)
  }

  fun deleteUploadDocumentByNomisId(nomsId: String, category: DocumentCategory) {
    if (!DocumentCategory.entries.contains(category)) {
      throw ValidationException("Given document category ${category.name} does not exists")
    }
    val prisoner = findPrisonerByNomsId(nomsId)
    val documentEntity: DocumentsEntity = documentsRepository.findFirstByNomsIdAndCategory(prisoner.nomsId, category, false) ?: throw ResourceNotFoundException("No document exists for category ${category.name} and prisoner id $nomsId ")

    documentEntity.isDeleted = true
    documentEntity.deletionDate = LocalDateTime.now()
    documentsRepository.save(documentEntity)
  }
}
