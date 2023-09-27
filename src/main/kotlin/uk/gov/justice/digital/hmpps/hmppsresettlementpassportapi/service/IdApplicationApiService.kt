package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.idapplicationapi.IdApplicationPatchDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.idapplicationapi.IdApplicationPostDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.IdApplicationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.IdTypeRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

@Service
class IdApplicationApiService(
  private val idApplicationRepository: IdApplicationRepository,
  private val prisonerRepository: PrisonerRepository,
  private val idTypeRepository: IdTypeRepository,
) {

  suspend fun getIdApplicationByNomsId(nomsId: String): IdApplicationEntity {
    var prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    return idApplicationRepository.findByPrisonerAndIsDeleted(prisoner)
      ?: throw ResourceNotFoundException("No active ID application found for prisoner with id $nomsId")
  }

  suspend fun createIdApplication(idApplicationPostDTO: IdApplicationPostDTO, nomsId: String): IdApplicationEntity {
    val now = LocalDateTime.now()
    var prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    val idTypeEntity = idTypeRepository.findByName(idApplicationPostDTO.idType!!)
      ?: throw ResourceNotFoundException("Id type ${idApplicationPostDTO.idType} not found in database")

    if (idApplicationPostDTO.applicationSubmittedDate != null &&
      idApplicationPostDTO.isPriorityApplication != null &&
      idApplicationPostDTO.costOfApplication != null
    ) {
      val idApplicationEntity = IdApplicationEntity(
        id = null,
        prisoner = prisoner,
        idType = idTypeEntity,
        creationDate = now,
        applicationSubmittedDate = idApplicationPostDTO.applicationSubmittedDate,
        isPriorityApplication = idApplicationPostDTO.isPriorityApplication,
        costOfApplication = idApplicationPostDTO.costOfApplication!!,
        haveGro = idApplicationPostDTO.haveGro,
        isUkNationalBornOverseas = idApplicationPostDTO.isUkNationalBornOverseas,
        countryBornIn = idApplicationPostDTO.countryBornIn,
        caseNumber = idApplicationPostDTO.caseNumber,
        courtDetails = idApplicationPostDTO.courtDetails,
        driversLicenceType = idApplicationPostDTO.driversLicenceType,
        driversLicenceApplicationMadeAt = idApplicationPostDTO.driversLicenceApplicationMadeAt,
      )
      return idApplicationRepository.save(idApplicationEntity)
    }

    throw ValidationException(
      "Request invalid. " +
        "applicationSubmittedDate= ${idApplicationPostDTO.applicationSubmittedDate} " +
        "isPriorityApplication=${idApplicationPostDTO.isPriorityApplication} " +
        "costOfApplication=${idApplicationPostDTO.costOfApplication}",
    )
  }

  suspend fun updateIdApplication(existingIdApplication: IdApplicationEntity, idApplicationPatchDTO: IdApplicationPatchDTO): IdApplicationEntity {
    val now = LocalDateTime.now()
    existingIdApplication.status = idApplicationPatchDTO.status ?: throw ValidationException("status must be updated")
    existingIdApplication.isAddedToPersonalItems = idApplicationPatchDTO.isAddedToPersonalItems ?: existingIdApplication.isAddedToPersonalItems
    existingIdApplication.refundAmount = idApplicationPatchDTO.refundAmount ?: existingIdApplication.refundAmount
    existingIdApplication.statusUpdateDate = now
    existingIdApplication.dateIdReceived = idApplicationPatchDTO.dateIdReceived ?: existingIdApplication.dateIdReceived
    existingIdApplication.addedToPersonalItemsDate = idApplicationPatchDTO.addedToPersonalItemsDate ?: existingIdApplication.addedToPersonalItemsDate
    return idApplicationRepository.save(existingIdApplication)
  }

  suspend fun deleteIdApplication(existingIdApplication: IdApplicationEntity) {
    existingIdApplication.isDeleted = true
    existingIdApplication.deletionDate = LocalDateTime.now()
    idApplicationRepository.save(existingIdApplication)
  }
}
