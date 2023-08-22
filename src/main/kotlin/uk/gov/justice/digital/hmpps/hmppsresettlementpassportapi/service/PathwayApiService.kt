package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import java.time.LocalDateTime

@Service
class PathwayApiService(
  private val pathwayStatusRepository: PathwayStatusRepository,
  private val prisonerRepository: PrisonerRepository,
  private val pathwayRepository: PathwayRepository,
  private val statusRepository: StatusRepository,
  private val communityApiService: CommunityApiService,
) {

  @Transactional
  fun updatePathwayStatus(prisonerId: String, pathwayAndStatus: PathwayAndStatus): ResponseEntity<Void> {
    val prisoner = getPrisonerEntityFromNomsId(prisonerId)
    val pathway = getPathwayEntity(pathwayAndStatus.pathway)
    val newStatus = getStatusEntity(pathwayAndStatus.status)

    val pathwayStatus = findPathwayStatusFromPathwayAndPrisoner(pathway, prisoner)
    updatePathwayStatusWithNewStatus(pathwayStatus, newStatus)

    return ResponseEntity.ok().build()
  }

  fun getPathwayEntity(pathway: Pathway) = pathwayRepository.findById(pathway.id).get()
  fun getStatusEntity(status: Status) = statusRepository.findById(status.id).get()
  fun getPrisonerEntityFromNomsId(nomsId: String) = prisonerRepository.findByNomsId(nomsId)
    ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
  fun findPathwayStatusFromPathwayAndPrisoner(pathway: PathwayEntity, prisoner: PrisonerEntity) = pathwayStatusRepository.findByPathwayAndPrisoner(pathway, prisoner)
    ?: throw ResourceNotFoundException("Prisoner with id ${prisoner.nomsId} has no pathway_status entry for ${pathway.name} in database")
  fun updatePathwayStatusWithNewStatus(pathwayStatus: PathwayStatusEntity, newStatus: StatusEntity) {
    pathwayStatus.status = newStatus
    pathwayStatus.updatedDate = LocalDateTime.now()
    pathwayStatusRepository.save(pathwayStatus)
  }

  @Transactional
  suspend fun addPrisonerAndInitialPathwayStatus(nomsId: String) {
    // Seed the Prisoner data into the DB
    var prisonerEntity = prisonerRepository.findByNomsId(nomsId)
    if (prisonerEntity == null) {
      val crn = communityApiService.getCrn(nomsId)
      prisonerEntity = PrisonerEntity(null, nomsId, LocalDateTime.now(), crn.toString())
      prisonerEntity = prisonerRepository.save(prisonerEntity)
      val statusRepoData = statusRepository.findById(Status.NOT_STARTED.id)
      val pathwayRepoData = pathwayRepository.findAll()
      pathwayRepoData.forEach {
        if (it.active) {
          val pathwayStatusEntity =
            PathwayStatusEntity(null, prisonerEntity, it, statusRepoData.get(), LocalDateTime.now())
          pathwayStatusRepository.save(pathwayStatusEntity)
        }
      }
    }
  }
}
