package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.DuplicateDataFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.IdApplicationPatch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.IdApplicationPost
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.IdApplicationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.IdTypeRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

@Service
class IdApplicationService(
  private val idApplicationRepository: IdApplicationRepository,
  private val prisonerRepository: PrisonerRepository,
  private val idTypeRepository: IdTypeRepository,
) {

  @Transactional
  suspend fun getIdApplicationByNomsId(nomsId: String): IdApplicationEntity {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    return idApplicationRepository.findByPrisonerAndIsDeleted(prisoner)
      ?: throw ResourceNotFoundException("No active ID application found for prisoner with id $nomsId")
  }

  @Transactional
  suspend fun createIdApplication(idApplicationPost: IdApplicationPost, nomsId: String): IdApplicationEntity {
    val now = LocalDateTime.now()
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    val idTypeEntity = idTypeRepository.findByName(idApplicationPost.idType!!)
      ?: throw ResourceNotFoundException("Id type ${idApplicationPost.idType} not found in database")

    val idApplicationExists = idApplicationRepository.findByPrisonerAndIsDeleted(prisoner)
    if (idApplicationExists != null) {
      throw DuplicateDataFoundException("Id application for prisoner with id $nomsId already exists in database")
    }

    if (idApplicationPost.applicationSubmittedDate != null &&
      idApplicationPost.isPriorityApplication != null &&
      idApplicationPost.costOfApplication != null
    ) {
      val idApplicationEntity = IdApplicationEntity(
        id = null,
        prisoner = prisoner,
        idType = idTypeEntity,
        creationDate = now,
        applicationSubmittedDate = idApplicationPost.applicationSubmittedDate,
        isPriorityApplication = idApplicationPost.isPriorityApplication,
        costOfApplication = idApplicationPost.costOfApplication!!,
        haveGro = idApplicationPost.haveGro,
        isUkNationalBornOverseas = idApplicationPost.isUkNationalBornOverseas,
        countryBornIn = idApplicationPost.countryBornIn,
        caseNumber = idApplicationPost.caseNumber,
        courtDetails = idApplicationPost.courtDetails,
        driversLicenceType = idApplicationPost.driversLicenceType,
        driversLicenceApplicationMadeAt = idApplicationPost.driversLicenceApplicationMadeAt,
      )
      return idApplicationRepository.save(idApplicationEntity)
    }

    throw ValidationException(
      "Request invalid. " +
        "applicationSubmittedDate= ${idApplicationPost.applicationSubmittedDate} " +
        "isPriorityApplication=${idApplicationPost.isPriorityApplication} " +
        "costOfApplication=${idApplicationPost.costOfApplication}",
    )
  }

  @Transactional
  suspend fun updateIdApplication(existingIdApplication: IdApplicationEntity, idApplicationPatchDTO: IdApplicationPatch): IdApplicationEntity {
    val now = LocalDateTime.now()
    existingIdApplication.status = idApplicationPatchDTO.status ?: throw ValidationException("status must be updated")
    existingIdApplication.isAddedToPersonalItems = idApplicationPatchDTO.isAddedToPersonalItems ?: existingIdApplication.isAddedToPersonalItems
    existingIdApplication.refundAmount = idApplicationPatchDTO.refundAmount ?: existingIdApplication.refundAmount
    existingIdApplication.statusUpdateDate = now
    existingIdApplication.dateIdReceived = idApplicationPatchDTO.dateIdReceived ?: existingIdApplication.dateIdReceived
    existingIdApplication.addedToPersonalItemsDate = idApplicationPatchDTO.addedToPersonalItemsDate ?: existingIdApplication.addedToPersonalItemsDate
    return idApplicationRepository.save(existingIdApplication)
  }

  @Transactional
  suspend fun deleteIdApplication(existingIdApplication: IdApplicationEntity) {
    existingIdApplication.isDeleted = true
    existingIdApplication.deletionDate = LocalDateTime.now()
    idApplicationRepository.save(existingIdApplication)
  }
}
