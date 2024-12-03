package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocationPostResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CasesCountResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.manageusersapi.ManageUser
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ManageUsersApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import java.time.LocalDateTime

@Service
class CaseAllocationService(
  private val prisonerRepository: PrisonerRepository,
  private val caseAllocationRepository: CaseAllocationRepository,
  private val manageUserApiService: ManageUsersApiService,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val pathwayAndStatusService: PathwayAndStatusService,
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
  fun assignCase(caseAllocation: CaseAllocation): MutableList<CaseAllocationPostResponse?> {
    val caseList = emptyList<CaseAllocationPostResponse?>().toMutableList()
    if (caseAllocation.staffId != null && caseAllocation.prisonId != null && caseAllocation.nomsIds.isNotEmpty()) {
      val workersList = getAllResettlementWorkers(caseAllocation.prisonId)
      if (workersList.isEmpty()) {
        throw ResourceNotFoundException("Prison with Id ${caseAllocation.prisonId} not found in database")
      }
      val staffDetails = workersList.find { it.staffId == caseAllocation.staffId.toLong() }
      if (staffDetails == null) {
        throw ResourceNotFoundException("Staff with id ${caseAllocation.staffId} not found in database")
      }
      for (nomsId in caseAllocation.nomsIds) {
        val prisoner = pathwayAndStatusService.getOrCreatePrisoner(
          nomsId,
          caseAllocation.prisonId,
        )
        val caseAllocationExists = caseAllocationRepository.findByPrisonerIdAndIsDeleted(
          prisoner.id(),
          false,
        )
        if (caseAllocationExists != null) {
          delete(caseAllocationExists)
        }

        val case = create(
          nomsId,
          staffDetails?.staffId?.toInt()!!,
          staffDetails.firstName!!,
          staffDetails.lastName!!,
          prisoner.id!!,
        )
        if (case != null) {
          val caseAllocationPostResponse = CaseAllocationPostResponse(
            case.staffId,
            case.staffFirstname,
            case.staffLastname,
            nomsId,
          )
          caseList.add(caseAllocationPostResponse)
        }
      }
    } else {
      throw ValidationException("In sufficient data, required staffId, prisonId and nomsIds")
    }
    return caseList
  }

  @Transactional
  fun create(nomsId: String, staffId: Int, staffFirstName: String, staffLastName: String, prisonerId: Long): CaseAllocationEntity? {
    val caseAllocationEntity =
      CaseAllocationEntity(
        null,
        prisonerId = prisonerId,
        staffId = staffId,
        staffFirstname = staffFirstName,
        staffLastname = staffLastName,
        isDeleted = false,
        creationDate = LocalDateTime.now(),
        deletionDate = null,
      )

    return caseAllocationEntity?.let { caseAllocationRepository.save(it) }
  }

  @Transactional
  fun getAllResettlementWorkers(
    prisonId: String,
  ): List<ManageUser> {
    return manageUserApiService.getManageUsersData(prisonId, "PSFR_RESETTLEMENT_WORKER")
  }

  @Transactional
  fun getAllAssignedResettlementWorkers(prisonId: String): List<CaseAllocationEntity?> {
    val caseAllocation = caseAllocationRepository.findAllByPrisonId(prisonId)

    return caseAllocation
  }

  @Transactional
  fun getCasesAllocationCount(prisonId: String): CasesCountResponse {
    val prisonerSearchList = prisonerSearchApiService.findPrisonersByPrisonId(prisonId)
    if (prisonerSearchList.isEmpty()) {
      throw ResourceNotFoundException("PrisonId $prisonId not found")
    }
    val assignedList = caseAllocationRepository.findCaseCountByPrisonId(prisonId)
    var unassignedCount = 0
    val assignedCount = caseAllocationRepository.findTotalCaseCountByPrisonId(prisonId)
    unassignedCount = prisonerSearchList.size - assignedCount
    val caseCountResponse = CasesCountResponse(unassignedCount, assignedList)
    return caseCountResponse
  }
}
