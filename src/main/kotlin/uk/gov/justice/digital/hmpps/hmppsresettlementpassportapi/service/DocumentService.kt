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
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

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
    category: DocumentCategory,
  ): Result<DocumentsEntity, VirusFound> =
    forExistingPrisoner(nomsId) {
      if (!document.originalFilename?.endsWith("docx")!! &&
        !document.originalFilename?.endsWith("doc")!! &&
        !document.originalFilename?.endsWith(
          "pdf",
        )!!
      ) {
        logger.info { "Received unsupported filename ${document.originalFilename}" }
        throw ValidationException("Unsupported document format, only .doc or pdf allowed")
      }

      when (val virusScanResult = virusScanner.scan(document.bytes)) {
        NoVirusFound -> Success(convertAndStoreDocument(nomsId, document, category))
        is VirusFound -> {
          logger.info { VirusFoundEvent(nomsId, virusScanResult.foundViruses) }
          Failure(virusScanResult)
        }
      }
    }

  @Transactional
  fun convertAndStoreDocument(nomsId: String, document: MultipartFile, category: DocumentCategory): DocumentsEntity {
    // using nomsId to find the prisoner entity
    val prisoner = findPrisonerByNomsId(nomsId)

    val key = UUID.randomUUID()
    var convertedDocumentKey = key
    uploadDocumentToS3(document, bucketName, key)
    if (!document.originalFilename?.endsWith("pdf", true)!!) {
      convertedDocumentKey = documentConversionService.convert(document)
    }

    val documents = DocumentsEntity(
      prisonerId = prisoner.id!!,
      originalDocumentKey = key,
      pdfDocumentKey = convertedDocumentKey,
      creationDate = LocalDateTime.now(),
      category = category,
      originalDocumentFileName = document.originalFilename!!,

    )

    val docEntity = documentsRepository.save(documents)
    logger.info { "Document key ${docEntity.originalDocumentKey} and id is ${docEntity.id}" }
    // saving the documents entity
    // return documentsRepository.save(documents)
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
      val document = documentsRepository.getReferenceById(documentId)
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
    val latest = documentsRepository.findFirstByNomsIdAndCategory(nomsId, category) ?: throw ResourceNotFoundException("No $category documents found for $nomsId")
    return getDocument(latest.pdfDocumentKey.toString())
  }

  private inline fun <reified T : Any?> forExistingPrisoner(nomsId: String, fn: () -> T): T {
    prisonerRepository.findByNomsId(nomsId)
    return fn()
  }

  data class VirusFoundEvent(val nomsId: String, val foundViruses: Map<String, Collection<String>>)

  fun listDocuments(nomsId: String, category: DocumentCategory): Collection<DocumentsEntity> =
    documentsRepository.findAllByNomsIdAndCategory(nomsId, category)
}
