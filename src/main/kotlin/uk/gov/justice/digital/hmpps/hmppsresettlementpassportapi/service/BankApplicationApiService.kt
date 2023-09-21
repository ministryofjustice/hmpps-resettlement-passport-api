package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoner
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.bankapplicatonapi.BankApplicationDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationStatusLogEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.BankApplicationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

@Service
class BankApplicationApiService(
  private val prisonerRepository: PrisonerRepository,
  private val bankApplicationRepository: BankApplicationRepository
) {

  suspend fun getBankApplicationById(id: Long) = bankApplicationRepository.findById(id)
    ?: throw ResourceNotFoundException("Bank application with id $id not found in database")

  suspend fun getBankApplicationByPrisoner(prisoner: PrisonerEntity) = bankApplicationRepository.findByPrisonerAndIsDeleted(prisoner)
    ?: throw ResourceNotFoundException(" no none deleted bank applications for prisoner: ${prisoner.nomsId} found in database")

  suspend fun deleteBankApplication(bankApplication: BankApplicationEntity){
    bankApplication.isDeleted = true
    bankApplication.deletionDate = LocalDateTime.now()
    bankApplicationRepository.save(bankApplication)
  }

  suspend fun createBankApplication(bankApplicationDTO: BankApplicationDTO, prisoner: PrisonerEntity){
    val now = LocalDateTime.now()
    val statusText = "Pending"
    val newStatus = BankApplicationStatusLogEntity(null,null, statusChangedTo = statusText, now )
    val bankApplication = BankApplicationEntity(null,
      prisoner, setOf(newStatus),
      creationDate = now,
      applicationSubmittedDate = bankApplicationDTO.applicationSubmittedDate!!,
      status = statusText)
    bankApplicationRepository.save(bankApplication)
  }

  suspend fun updateBankApplication(existingBankApplication: BankApplicationEntity ,bankApplicationDTO: BankApplicationDTO){
    existingBankApplication.bankResponseDate = bankApplicationDTO.bankResponseDate ?: existingBankApplication.bankResponseDate
    existingBankApplication.status = bankApplicationDTO.status ?: existingBankApplication.status
    existingBankApplication.isAddedToPersonalItems = bankApplicationDTO.isAddedToPersonalItems ?: existingBankApplication.isAddedToPersonalItems
    existingBankApplication.addedToPersonalItemsDate = bankApplicationDTO.addedToPersonalItemsDate ?: existingBankApplication.addedToPersonalItemsDate

    if(bankApplicationDTO.status != null){
      val newStatus = BankApplicationStatusLogEntity(null,null, statusChangedTo = bankApplicationDTO.status, LocalDateTime.now() )
      existingBankApplication.logs = existingBankApplication.logs.plus(newStatus)
    }
    bankApplicationRepository.save(existingBankApplication)
  }
}