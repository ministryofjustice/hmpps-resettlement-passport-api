package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.manageusersapi.ManageUser
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ManageUsersApiService
import java.time.LocalDateTime

@Service
class CaseAllocationService(
  private val prisonerRepository: PrisonerRepository,
  private val caseAllocationRepository: CaseAllocationRepository,
  private val manageUserApiService: ManageUsersApiService,
) {

  @Transactional
  fun getCaseAllocationByNomsId(
    nomsId: String,
  ): CaseAllocationEntity? {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val caseAllocationEntity = caseAllocationRepository.findByPrisonerIdAndIsDeleted(
      prisoner.id(),
      false,
    )
    return caseAllocationEntity ?: throw ResourceNotFoundException("No officer assigned for prisoner with id $nomsId")
  }

  @Transactional
  fun getAllCaseAllocationByStaffId(
    staffId: Int,
  ): List<CaseAllocationEntity?> {
    val caseAllocationEntityList = caseAllocationRepository.findByStaffIdAndIsDeleted(
      staffId,
      false,
    )
    return caseAllocationEntityList
  }

  @Transactional
  fun unAssignCase(caseAllocation: CaseAllocation): List<CaseAllocationEntity?> {
    val caseList = emptyList<CaseAllocationEntity?>().toMutableList()
    for (nomsId in caseAllocation.nomsIds) {
      val prisoner = prisonerRepository.findByNomsId(nomsId)
        ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
      val caseAllocationExists = caseAllocationRepository.findByPrisonerIdAndIsDeleted(
        prisoner.id(), false,
      ) ?: throw ResourceNotFoundException("Unable to unassign, no officer assigned for prisoner with id $nomsId")
      val case = caseAllocationExists?.let { delete(it) }
      caseList.add(case)
    }
    return caseList
  }

  @Transactional
  fun delete(caseAllocation: CaseAllocationEntity): CaseAllocationEntity {
    caseAllocation.isDeleted = true
    caseAllocation.deletionDate = LocalDateTime.now()
    caseAllocationRepository.save(caseAllocation)
    return caseAllocation
  }

  @Transactional
  fun assignCase(caseAllocation: CaseAllocation): MutableList<CaseAllocationEntity?> {
    val caseList = emptyList<CaseAllocationEntity?>().toMutableList()
    if (caseAllocation.staffId != null && caseAllocation.staffFirstName != null && caseAllocation.staffLastName != null) {
      for (nomsId in caseAllocation.nomsIds) {
        val prisoner = prisonerRepository.findByNomsId(nomsId)
          ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
        val caseAllocationExists = caseAllocationRepository.findByPrisonerIdAndIsDeleted(
          prisoner.id(),
          false,
        )
        if (caseAllocationExists != null) {
          delete(caseAllocationExists)
        }

        val case = create(
          nomsId,
          caseAllocation.staffId,
          caseAllocation.staffFirstName,
          caseAllocation.staffLastName,
        )
        caseList.add(case)
      }
    }
    return caseList
  }

  @Transactional
  fun create(nomsId: String, staffId: Int, staffFirstName: String, staffLastName: String): CaseAllocationEntity? {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val caseAllocationEntity = prisoner?.id?.let {
      CaseAllocationEntity(
        null,
        prisonerId = it,
        staffId = staffId,
        staffFirstname = staffFirstName,
        staffLastname = staffLastName,
        isDeleted = false,
        creationDate = LocalDateTime.now(),
        deletionDate = null,
      )
    }
    return caseAllocationEntity?.let { caseAllocationRepository.save(it) }
  }

  @Transactional
  fun getAllResettlementWorkers(
    prisonId: String,
  ): List<ManageUser> {
    return manageUserApiService.getManageUsersData(prisonId, "ADD_SENSITIVE_CASE_NOTES")
  }
}
