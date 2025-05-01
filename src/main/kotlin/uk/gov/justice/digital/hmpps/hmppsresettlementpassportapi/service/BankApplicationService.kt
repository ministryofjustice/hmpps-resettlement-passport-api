package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.DuplicateDataFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplication
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplicationLog
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplicationResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationStatusLogEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.BankApplicationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.BankApplicationStatusLogRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class BankApplicationService(
  private val prisonerRepository: PrisonerRepository,
  private val bankApplicationRepository: BankApplicationRepository,
  private val bankApplicationStatusLogRepository: BankApplicationStatusLogRepository,
) {

  @Transactional
  fun getBankApplicationById(id: Long) = bankApplicationRepository.findById(id)
    ?: throw ResourceNotFoundException("Bank application with id $id not found in database")

  @Transactional
  fun getBankApplicationByIdAndNomsId(id: Long, nomsId: String) = bankApplicationRepository.findByIdAndNomsId(id, nomsId)
    ?: throw ResourceNotFoundException("Bank application with id $id not found in database")

  @Transactional
  fun getBankApplicationByNomsId(nomsId: String): BankApplicationResponse? {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val bankApplication = bankApplicationRepository.findByPrisonerIdAndIsDeleted(prisoner.id())
      ?: throw ResourceNotFoundException(" no none deleted bank applications for prisoner: ${prisoner.nomsId} found in database")
    return getBankApplicationResponse(bankApplication, prisoner)
  }

  @Transactional
  fun getBankApplicationsByPrisonerAndCreationDate(
    prisoner: PrisonerEntity,
    fromDate: LocalDate,
    toDate: LocalDate,
  ): List<BankApplicationResponse> = bankApplicationRepository.findByPrisonerIdAndCreationDateBetween(
    prisoner.id(),
    fromDate = fromDate.atStartOfDay(),
    toDate = toDate.atTime(LocalTime.MAX),
  ).map { getBankApplicationResponse(it, prisoner) }

  private fun getBankApplicationResponse(
    bankApplication: BankApplicationEntity,
    prisoner: PrisonerEntity,
  ): BankApplicationResponse {
    bankApplication.logs = emptySet()
    val logs = bankApplicationStatusLogRepository.findByBankApplication(bankApplication)
    return BankApplicationResponse(
      id = bankApplication.id!!,
      prisoner = prisoner,
      logs = if (logs.isNullOrEmpty()) {
        emptyList()
      } else {
        logs.map {
          BankApplicationLog(
            it.id!!,
            it.statusChangedTo,
            it.changedAtDate,
          )
        }
      },
      currentStatus = bankApplication.status,
      bankName = bankApplication.bankName,
      applicationSubmittedDate = bankApplication.applicationSubmittedDate,
      bankResponseDate = bankApplication.bankResponseDate,
      addedToPersonalItemsDate = bankApplication.addedToPersonalItemsDate,
      isAddedToPersonalItems = bankApplication.isAddedToPersonalItems,
    )
  }

  @Transactional
  fun deleteBankApplication(bankApplication: BankApplicationEntity) {
    bankApplication.isDeleted = true
    bankApplication.deletionDate = LocalDateTime.now()
    bankApplicationRepository.save(bankApplication)
  }

  @Transactional
  fun createBankApplication(bankApplication: BankApplication, nomsId: String, notUnitTest: Boolean = true): BankApplicationResponse {
    val now = LocalDateTime.now()
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val statusText = "Pending"

    val bankApplicationExists = bankApplicationRepository.findByPrisonerIdAndIsDeleted(prisoner.id())
    if (notUnitTest && bankApplicationExists != null) {
      throw DuplicateDataFoundException("Bank application for prisoner with id $nomsId already exists in database")
    }

    val bankApplicationEntity = BankApplicationEntity(
      null,
      prisoner.id(),
      emptySet(),
      bankName = bankApplication.bankName ?: throw ValidationException("Bank name cannot be null"),
      creationDate = now,
      applicationSubmittedDate = bankApplication.applicationSubmittedDate!!,
      status = statusText,
    )
    val newStatus = BankApplicationStatusLogEntity(
      null,
      bankApplicationEntity,
      statusChangedTo = statusText,
      bankApplicationEntity.applicationSubmittedDate,
    )
    bankApplicationStatusLogRepository.save(newStatus)
    return getBankApplicationByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Bank application for prisoner with id $nomsId not found in database ")
  }

  @Transactional
  fun patchBankApplication(nomsId: String, bankApplicationId: String, bankApplication: BankApplication): BankApplicationResponse {
    val existingBankApplication = getBankApplicationByIdAndNomsId(bankApplicationId.toLong(), nomsId)

    updateBankApplication(existingBankApplication = existingBankApplication, bankApplication)
    return getBankApplicationByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Bank application for prisoner: $nomsId not found after update")
  }

  @Transactional
  fun updateBankApplication(existingBankApplication: BankApplicationEntity, bankApplication: BankApplication) {
    val logs = bankApplicationStatusLogRepository.findByBankApplication(existingBankApplication)
      ?: throw ResourceNotFoundException("Bank application for prisoner with id ${existingBankApplication.prisonerId} not found in database")

    logs[0].bankApplication?.bankResponseDate = bankApplication.bankResponseDate ?: logs[0].bankApplication?.bankResponseDate
    logs[0].bankApplication?.status = bankApplication.status ?: logs[0].bankApplication?.status!!
    logs[0].bankApplication?.isAddedToPersonalItems = bankApplication.isAddedToPersonalItems ?: logs[0].bankApplication?.isAddedToPersonalItems
    logs[0].bankApplication?.addedToPersonalItemsDate = bankApplication.addedToPersonalItemsDate ?: logs[0].bankApplication?.addedToPersonalItemsDate
    bankApplicationStatusLogRepository.saveAll(logs)
    if (bankApplication.resubmissionDate != null) {
      val newStatus = BankApplicationStatusLogEntity(
        null,
        logs[0].bankApplication,
        statusChangedTo = "Account resubmitted",
        changedAtDate = bankApplication.resubmissionDate,
      )
      logs.plus(newStatus)
      bankApplicationStatusLogRepository.save(newStatus)
    }
    if (bankApplication.status != null) {
      val newStatus = BankApplicationStatusLogEntity(
        null,
        logs[0].bankApplication,
        statusChangedTo = bankApplication.status,
        changedAtDate = bankApplication.bankResponseDate
          ?: throw ValidationException("changedAtDate cant be null when changing status"),
      )
      logs.plus(newStatus)
      bankApplicationStatusLogRepository.save(newStatus)
    }
  }
}
