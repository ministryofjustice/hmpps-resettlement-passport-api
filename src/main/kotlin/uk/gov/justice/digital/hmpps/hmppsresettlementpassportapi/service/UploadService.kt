package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DocumentsRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.io.File
import java.time.LocalDateTime

@Service
class UploadService(
  private val s3Client: S3Client,
  val prisonerRepository: PrisonerRepository,
  val documentsRepository: DocumentsRepository,
  @Value("\${hmpps.s3.buckets.document-management.bucketName}") private val bucketName: String,

  ) {
  @Transactional
  fun documentScanAndStore(nomsId: String, document: MultipartFile): DocumentsEntity {
    // using nomsId to find the prisoner entity
    val prisoner = findPrisonerByNomsId(nomsId)
    val time = System.currentTimeMillis()
    val key = nomsId + "_" + time
    uploadFileToS3(document, bucketName, key)
    val documents = DocumentsEntity(
      id = null,
      prisoner = prisoner,
      documentKey = key,
      creationDate = LocalDateTime.now(),
    )

    // saving the documents entity
    return documentsRepository.save(documents)
  }

  fun findPrisonerByNomsId(nomsId: String): PrisonerEntity {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    return prisoner
  }

  fun uploadFileToS3(file: MultipartFile, bucketName: String?, key: String?) {
    val request = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .build()

    s3Client.putObject(request, RequestBody.fromInputStream(file.inputStream, file.size))
  }
}
