package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.microsoft.applicationinsights.TelemetryClient
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocationPostResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CasesCountResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.manageusersapi.ManageUser
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ManageUsersApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class CaseAllocationService(
  private val prisonerRepository: PrisonerRepository,
  private val caseAllocationRepository: CaseAllocationRepository,
  private val manageUserApiService: ManageUsersApiService,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val pathwayAndStatusService: PathwayAndStatusService,
  private val telemetryClient: TelemetryClient,
) {

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
  fun unAssignCase(caseAllocation: CaseAllocation, auth: String): List<CaseAllocationEntity?> {
    val staffUserName =
      getClaimFromJWTToken(auth, "sub") ?: throw ServerWebInputException("Cannot get name from auth token")
    val caseList = emptyList<CaseAllocationEntity?>().toMutableList()
    for (nomsId in caseAllocation.nomsIds) {
      val prisoner = prisonerRepository.findByNomsId(nomsId)
        ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
      getCaseAllocationByPrisonerId(prisoner.id())?.let { caseAllocationEntity ->
        val case = delete(caseAllocationEntity)
        trackUnallocationEvent(prisoner, staffUserName)
        caseList.add(case)
      }
        ?: logger.info { "No officer assigned for prisoner with id $nomsId, ignoring this nomsId from case unallocation request." }
    }
    return caseList
  }

  @Transactional
  fun getCaseAllocationByPrisonerId(prisonerId: Long): CaseAllocationEntity? = caseAllocationRepository.findByPrisonerIdAndIsDeleted(prisonerId, false)

  fun getCaseAllocationHistoryByPrisonerId(
    prisonerId: Long,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
  ): List<CaseAllocationEntity> = caseAllocationRepository.findByPrisonerIdAndCreationDateBetween(prisonerId, startDate, endDate)

  @Transactional
  fun delete(caseAllocation: CaseAllocationEntity): CaseAllocationEntity {
    caseAllocation.isDeleted = true
    caseAllocation.deletionDate = LocalDateTime.now()
    caseAllocationRepository.save(caseAllocation)
    return caseAllocation
  }

  @Transactional
  fun assignCase(caseAllocation: CaseAllocation, auth: String): MutableList<CaseAllocationPostResponse?> {
    val assignedByUsername =
      getClaimFromJWTToken(auth, "sub") ?: throw ServerWebInputException("Cannot get username from auth token")

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
          staffDetails.staffId.toInt(),
          staffDetails.firstName!!,
          staffDetails.lastName!!,
          prisoner.id!!,
        )
        telemetryClient.trackEvent(
          "PSFR_CaseAllocation",
          mapOf(
            "prisonId" to caseAllocation.prisonId,
            "prisonerId" to prisoner.nomsId,
            "allocatedToStaffId" to staffDetails.staffId.toString(),
            "allocatedByUsername" to assignedByUsername,
          ),
          null,
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
  fun create(
    nomsId: String,
    staffId: Int,
    staffFirstName: String,
    staffLastName: String,
    prisonerId: Long,
  ): CaseAllocationEntity? {
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

    return caseAllocationEntity.let { caseAllocationRepository.save(it) }
  }

  @Transactional
  fun getAllResettlementWorkers(
    prisonId: String,
  ): List<ManageUser> = manageUserApiService.getManageUsersData(prisonId, "PSFR_RESETTLEMENT_WORKER")

  @Transactional
  fun getAllAssignedResettlementWorkers(prisonId: String): List<CaseAllocationEntity?> {
    val caseAllocation = caseAllocationRepository.findAllByPrisonId(prisonId)

    return caseAllocation
  }

  @Transactional
  fun getCasesAllocationCount(prisonId: String): CasesCountResponse {
    val prisonerSearchList = prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "")
    if (prisonerSearchList.isEmpty()) {
      throw ResourceNotFoundException("PrisonId $prisonId not found")
    }
    val assignedList = caseAllocationRepository.findCaseCountByPrisonId(prisonId)
    val assignedCount = caseAllocationRepository.findTotalCaseCountByPrisonId(prisonId)
    val unassignedCount = prisonerSearchList.size - assignedCount
    val caseCountResponse = CasesCountResponse(unassignedCount, assignedList)
    return caseCountResponse
  }

  @Transactional
  fun getAssignedResettlementWorkerByNomsId(nomsId: String): List<CaseAllocationEntity> {
    val caseAllocationList = caseAllocationRepository.findByNomsIdAndIsDeleted(nomsId)
    return caseAllocationList
  }

  fun getNumberOfAssignedPrisoners(prisonId: String) = caseAllocationRepository.findTotalCaseCountByPrisonId(prisonId)

  private fun trackUnallocationEvent(
    prisonerEntity: PrisonerEntity,
    staffUserName: String,
  ) {
    telemetryClient.trackEvent(
      "PSFR_CaseUnallocation",
      mapOf(
        "prisonId" to prisonerEntity.prisonId,
        "prisonerId" to prisonerEntity.nomsId,
        "unallocatedByUsername" to staffUserName,
      ),
      null,
    )
  }
}
