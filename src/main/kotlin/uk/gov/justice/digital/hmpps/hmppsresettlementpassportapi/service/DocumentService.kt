package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.hibernate.query.sqm.tree.SqmNode.log
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
import java.time.LocalDateTime
import java.util.UUID

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
    category: String,
  ): Result<DocumentsEntity, VirusFound> =
    forExistingPrisoner(nomsId) {
      if (!document.originalFilename?.endsWith("docx")!! &&
        !document.originalFilename?.endsWith("doc")!! &&
        !document.originalFilename?.endsWith(
          "pdf",
        )!!
      ) {
        throw ValidationException("Unsupported document format, only .doc or pdf allowed")
      }

      if (!isValidDocumentCategory(category)) {
        throw ValidationException("Invalid Document Category")
      }
      val categoryValue = DocumentCategory.valueOf(category)

      when (val virusScanResult = virusScanner.scan(document.bytes)) {
        NoVirusFound -> Success(convertAndStoreDocument(nomsId, document, categoryValue))
        is VirusFound -> {
          log.info(VirusFoundEvent(nomsId, virusScanResult.foundViruses).toString())
          Failure(virusScanResult)
        }
      }
    }

  @Transactional
  fun convertAndStoreDocument(nomsId: String, document: MultipartFile, category: DocumentCategory): DocumentsEntity {
    // using nomsId to find the prisoner entity
    val prisoner = findPrisonerByNomsId(nomsId)

    val key = nomsId + "_" + UUID.randomUUID()

    uploadDocumentToS3(document, bucketName, key)
    val convertedDocumentKey = documentConversionService.convert(document)

    val documents = DocumentsEntity(
      prisonerId = prisoner.id!!,
      originalDocumentKey = key,
      htmlDocumentKey = convertedDocumentKey,
      creationDate = LocalDateTime.now(),
      category = category,
      originalDocumentFileName = document.originalFilename!!,

    )

    val docEntity = documentsRepository.save(documents)
    log.info("Document key ${docEntity.originalDocumentKey} and id is ${docEntity.id}")
    // saving the documents entity
    // return documentsRepository.save(documents)
    return docEntity
  }

  fun findPrisonerByNomsId(nomsId: String): PrisonerEntity {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    return prisoner
  }

  fun uploadDocumentToS3(file: MultipartFile, bucketName: String?, key: String?) {
    val request = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .build()

    s3Client.putObject(request, RequestBody.fromInputStream(file.inputStream, file.size))
  }

  fun getDocument(documentKey: String): ByteArray {
    val request = GetObjectRequest.builder()
      .bucket(bucketName)
      .key(documentKey)
      .build()

    return s3Client.getObject(request).readAllBytes()
  }

  fun getDocumentByNomisIdAndDocumentId(nomsId: String, documentId: Long, category: String?): ByteArray {
    val prisoner = findPrisonerByNomsId(nomsId)
    val document: DocumentsEntity
    var documentKey: String? = null
    if (documentId.toInt() != 0) {
      try {
        document = documentsRepository.getReferenceById(documentId)
        documentKey = document.originalDocumentKey
      } catch (ex: Exception) {
        throw ResourceNotFoundException("Document with id $documentId not found")
      }
      if (prisoner.id != document.prisonerId) {
        throw ResourceNotFoundException("Document with id $documentId not found")
      }
    } else if (documentId.toInt() == 0 && category != null) {
      if (!isValidDocumentCategory(category)) {
        throw ValidationException("Invalid Document Category")
      }
      val documentsList =
        prisoner.id?.let {
          documentsRepository.findAllByPrisonerIdAndCategoryOrderByCreationDateDesc(
            it,
            DocumentCategory.valueOf(category),
          )
        }
      if (documentsList != null && documentsList.isEmpty()) {
        throw ResourceNotFoundException("No Document Exists for category $category")
      }
      if (documentsList != null) {
        document = documentsList.first()
        documentKey = document.originalDocumentKey
      }
    }
    if (documentKey != null) {
      return getDocument(documentKey)
    }
    throw ResourceNotFoundException("No Document Found")
  }

  fun getHtmlByNomisIdAndDocumentId(nomsId: String, documentId: Long, category: String?): String {
    val prisoner = findPrisonerByNomsId(nomsId)
    val document: DocumentsEntity
    var key: String? = null
    if (documentId.toInt() > 0) {
      if (category != null && !isValidDocumentCategory(category)) {
        throw ValidationException("Invalid Document Category $category")
      }
      try {
        document = documentsRepository.getReferenceById(documentId)
        key = document.htmlDocumentKey?.toString() ?: throw ResourceNotFoundException("$documentId does not have html available")
      } catch (ex: Exception) {
        throw ResourceNotFoundException("Document with id $documentId not found")
      }
      if (prisoner.id != document.prisonerId) {
        throw ResourceNotFoundException("Document with id $documentId not found")
      }
    } else if (documentId.toInt() == 0 && category != null) {
      if (!isValidDocumentCategory(category)) {
        throw ValidationException("Invalid Document Category $category")
      }
      val documentsList =
        prisoner.id?.let {
          documentsRepository.findAllByPrisonerIdAndCategoryOrderByCreationDateDesc(
            it,
            DocumentCategory.valueOf(category),
          )
        }
      if (documentsList != null && documentsList.isEmpty()) {
        throw ResourceNotFoundException("No Document Exists for category $category and prisoner ${prisoner.nomsId}")
      }
      if (documentsList != null) {
        document = documentsList.first()
        key = document.htmlDocumentKey?.toString() ?: throw ResourceNotFoundException("$documentId does not have html available")
      }
    }
    if (key != null) {
      val bytes = getDocument(key)
      return String(bytes, Charsets.UTF_8)
    }
    throw ResourceNotFoundException("No Document Found for document id $documentId")
  }

  private inline fun <reified T : Any?> forExistingPrisoner(nomsId: String, fn: () -> T): T {
    prisonerRepository.findByNomsId(nomsId)
    return fn()
  }

  data class VirusFoundEvent(val nomsId: String, val foundViruses: Map<String, Collection<String>>)

  fun isValidDocumentCategory(category: String): Boolean {
    try {
      DocumentCategory.valueOf(category)
    } catch (ex: IllegalArgumentException) {
      return false
    }
    return true
  }

  fun getLatestDocumentByNomisId(nomsId: String, category: String?): ByteArray = getDocumentByNomisIdAndDocumentId(nomsId, 0, category)

  fun getLatestHTMLByNomisId(nomsId: String, category: String): String = getHtmlByNomisIdAndDocumentId(nomsId, 0, category)
}
