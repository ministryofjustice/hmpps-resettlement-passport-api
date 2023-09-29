package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.DuplicateDataFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.bankapplicatonapi.BankApplicationDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.bankapplicatonapi.BankApplicationLogDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.bankapplicatonapi.BankApplicationResponseDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationStatusLogEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.BankApplicationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.BankApplicationStatusLogRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

@Service
class BankApplicationApiService(
  private val prisonerRepository: PrisonerRepository,
  private val bankApplicationRepository: BankApplicationRepository,
  private val bankApplicationStatusLogRepository: BankApplicationStatusLogRepository,
) {

  @Transactional
  suspend fun getBankApplicationById(id: Long) = bankApplicationRepository.findById(id)
    ?: throw ResourceNotFoundException("Bank application with id $id not found in database")

  @Transactional
  suspend fun getBankApplicationByNomsId(nomsId: String): BankApplicationResponseDTO? {
    var prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    var bankApplication = bankApplicationRepository.findByPrisonerAndIsDeleted(prisoner)
      ?: throw ResourceNotFoundException(" no none deleted bank applications for prisoner: ${prisoner.nomsId} found in database")
    bankApplication.logs = emptySet()
    val logs = bankApplicationStatusLogRepository.findByBankApplication(bankApplication)
    return BankApplicationResponseDTO(
      id = bankApplication.id!!,
      prisoner = prisoner,
      logs = if (logs.isNullOrEmpty()) emptyList() else logs.map { BankApplicationLogDTO(it.id!!, it.statusChangedTo, it.changedAtDate) },
      currentStatus = bankApplication.status,
      bankName = bankApplication.bankName,
      applicationSubmittedDate = bankApplication.applicationSubmittedDate,
      bankResponseDate = bankApplication.bankResponseDate,
      addedToPersonalItemsDate = bankApplication.addedToPersonalItemsDate,
      isAddedToPersonalItems = bankApplication.isAddedToPersonalItems,
    )
  }

  @Transactional
  suspend fun deleteBankApplication(bankApplication: BankApplicationEntity) {
    bankApplication.isDeleted = true
    bankApplication.deletionDate = LocalDateTime.now()
    bankApplicationRepository.save(bankApplication)
  }

  @Transactional
  suspend fun createBankApplication(bankApplicationDTO: BankApplicationDTO, nomsId: String, notUnitTest: Boolean = true): BankApplicationResponseDTO {
    val now = LocalDateTime.now()
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val statusText = "Pending"
    val bankApplicationExists = bankApplicationRepository.findByPrisonerAndIsDeleted(prisoner)
    if (notUnitTest && bankApplicationExists != null) {
      throw DuplicateDataFoundException("Bank application for prisoner with id $nomsId already exists in database")
    }

    val bankApplication = BankApplicationEntity(
      null,
      prisoner,
      emptySet(),
      bankName = bankApplicationDTO.bankName ?: throw ValidationException("Bank name cannot be null"),
      creationDate = now,
      applicationSubmittedDate = bankApplicationDTO.applicationSubmittedDate!!,
      status = statusText,
    )
    val newStatus = BankApplicationStatusLogEntity(null, bankApplication, statusChangedTo = statusText, now)
    bankApplicationStatusLogRepository.save(newStatus)
    return getBankApplicationByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Bank application for prisoner with id $nomsId not found in database ")
  }

  @Transactional
  suspend fun updateBankApplication(existingBankApplication: BankApplicationEntity, bankApplicationDTO: BankApplicationDTO) {
    val logs = bankApplicationStatusLogRepository.findByBankApplication(existingBankApplication)
      ?: throw ResourceNotFoundException("Bank application for prisoner with id ${existingBankApplication.prisoner.nomsId} not found in database ")

    logs[0].bankApplication?.bankResponseDate = bankApplicationDTO.bankResponseDate ?: logs[0].bankApplication?.bankResponseDate
    logs[0].bankApplication?.status = bankApplicationDTO.status ?: logs[0].bankApplication?.status!!
    logs[0].bankApplication?.isAddedToPersonalItems = bankApplicationDTO.isAddedToPersonalItems ?: logs[0].bankApplication?.isAddedToPersonalItems
    logs[0].bankApplication?.addedToPersonalItemsDate = bankApplicationDTO.addedToPersonalItemsDate ?: logs[0].bankApplication?.addedToPersonalItemsDate
    bankApplicationStatusLogRepository.saveAll(logs)
    if (bankApplicationDTO.status != null) {
      val newStatus = BankApplicationStatusLogEntity(null, logs[0].bankApplication, statusChangedTo = bankApplicationDTO.status, LocalDateTime.now())
      logs.plus(newStatus)
      bankApplicationStatusLogRepository.save(newStatus)
    }
  }
}
