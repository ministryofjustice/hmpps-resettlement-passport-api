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
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class IdApplicationService(
  private val idApplicationRepository: IdApplicationRepository,
  private val prisonerRepository: PrisonerRepository,
  private val idTypeRepository: IdTypeRepository,
) {

  @Transactional
  fun getIdApplicationByNomsId(nomsId: String): IdApplicationEntity? {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val idApplicationEntityList = idApplicationRepository.findByPrisonerIdAndIsDeleted(prisoner.id())
    if (idApplicationEntityList.isEmpty()) {
      throw ResourceNotFoundException("No active ID application found for prisoner with id $nomsId")
    } else {
      return idApplicationEntityList[0]
    }
  }

  @Transactional
  fun getIdApplicationByNomsIdAndCreationDate(
    nomsId: String,
    fromDate: LocalDate,
    toDate: LocalDate,
  ): IdApplicationEntity? {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val idApplicationEntityList = idApplicationRepository.findByPrisonerIdAndIsDeletedAndCreationDateBetween(
      prisoner.id(),
      fromDate = fromDate.atStartOfDay(),
      toDate = toDate.atStartOfDay(),
    )
    if (idApplicationEntityList.isEmpty()) {
      throw ResourceNotFoundException("No active ID application found for prisoner with id $nomsId")
    } else {
      return idApplicationEntityList[0]
    }
  }

  @Transactional
  fun createIdApplication(idApplicationPost: IdApplicationPost, nomsId: String): IdApplicationEntity {
    val now = LocalDateTime.now()
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    val idTypeEntity = idTypeRepository.findByName(idApplicationPost.idType!!)
      ?: throw ResourceNotFoundException("Id type ${idApplicationPost.idType} not found in database")

    val idApplicationExists = idApplicationRepository.findByPrisonerIdAndIdTypeAndIsDeleted(prisoner.id(), idTypeEntity, false)
    if (idApplicationExists != null) {
      throw DuplicateDataFoundException("Id application for prisoner with id $nomsId and id application type ${idApplicationPost.idType} already exists in database")
    }

    if (idApplicationPost.applicationSubmittedDate != null &&
      idApplicationPost.isPriorityApplication != null &&
      idApplicationPost.costOfApplication != null
    ) {
      val idApplicationEntity = IdApplicationEntity(
        id = null,
        prisonerId = prisoner.id(),
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
  fun updateIdApplication(existingIdApplication: IdApplicationEntity, idApplicationPatchDTO: IdApplicationPatch): IdApplicationEntity {
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
  fun deleteIdApplication(existingIdApplication: IdApplicationEntity) {
    existingIdApplication.isDeleted = true
    existingIdApplication.deletionDate = LocalDateTime.now()
    idApplicationRepository.save(existingIdApplication)
  }

  @Transactional
  fun getIdApplicationByNomsIdAndIdApplicationID(nomsId: String, idApplicationId: Long): IdApplicationEntity? {
    prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    return idApplicationRepository.findByIdAndIsDeleted(idApplicationId, false)
      ?: throw ResourceNotFoundException("No active ID application found for prisoner with id $nomsId and Application Id $idApplicationId")
  }

  @Transactional
  fun getAllIdApplicationsByNomsId(nomsId: String): List<IdApplicationEntity?> {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    return idApplicationRepository.findByPrisonerIdAndIsDeleted(prisoner.id(), false)
  }
}
