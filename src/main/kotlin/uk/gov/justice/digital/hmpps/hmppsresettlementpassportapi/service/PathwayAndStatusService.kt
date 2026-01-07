package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class PathwayAndStatusService(
  private val pathwayStatusRepository: PathwayStatusRepository,
  private val prisonerRepository: PrisonerRepository,
  private val transactionOperations: TransactionOperations,
) {

  fun updatePathwayStatus(nomsId: String, pathwayAndStatus: PathwayAndStatus): ResponseEntity<Void> {
    val prisoner = getPrisonerEntityFromNomsId(nomsId)

    val pathwayStatus = findPathwayStatusFromPathwayAndPrisoner(pathwayAndStatus.pathway, prisoner)
    updatePathwayStatusWithNewStatus(pathwayStatus, pathwayAndStatus.status)

    return ResponseEntity.ok().build()
  }

  fun getPrisonerEntityFromNomsId(nomsId: String) = prisonerRepository.findByNomsId(nomsId)
    ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

  fun findPathwayStatusFromPathwayAndPrisoner(pathway: Pathway, prisoner: PrisonerEntity) = pathwayStatusRepository.findByPathwayAndPrisonerId(pathway, prisoner.id())
    ?: throw ResourceNotFoundException("Prisoner with id ${prisoner.nomsId} has no pathway_status entry for ${pathway.name} in database")

  fun findAllPathwayStatusForPrisoner(prisoner: PrisonerEntity): List<PathwayStatusEntity> = pathwayStatusRepository.findByPrisonerId(prisoner.id())

  fun findAllPathwayStatusSarContentForPrisoner(prisoner: PrisonerEntity): List<PathwayStatusSarContent> = pathwayStatusRepository.findByPrisonerId(prisoner.id()).map {
    PathwayStatusSarContent(it.pathway.displayName, it.status.displayText, it.updatedDate)
  }

  data class PathwayStatusSarContent(
    val pathway: String,
    var status: String,
    var updatedDate: LocalDateTime? = null,
  )

  fun updatePathwayStatusWithNewStatus(pathwayStatus: PathwayStatusEntity, newStatus: Status) {
    pathwayStatus.status = newStatus
    pathwayStatus.updatedDate = LocalDateTime.now()
    pathwayStatusRepository.save(pathwayStatus)
  }

  fun getOrCreatePrisoner(nomsId: String, prisonId: String?, crn: String? = null): PrisonerEntity {
    // Seed the Prisoner data into the DB
    val existingPrisonerEntity = prisonerRepository.findByNomsId(nomsId)
    if (existingPrisonerEntity == null) {
      return createPrisoner(nomsId, prisonId)
    }
    return existingPrisonerEntity
  }

  internal fun createPrisoner(
    nomsId: String,
    prisonId: String?,
  ): PrisonerEntity = try {
    transactionOperations.execute {
      val newPrisonerEntity = prisonerRepository.save(
        PrisonerEntity(
          nomsId = nomsId,
          prisonId = prisonId,
          supportNeedsLegacyProfile = false,
        ),
      )
      Pathway.entries.forEach {
        val pathwayStatusEntity =
          PathwayStatusEntity(null, newPrisonerEntity.id(), it, Status.NOT_STARTED, null)
        pathwayStatusRepository.save(pathwayStatusEntity)
      }
      newPrisonerEntity
    }!!
  } catch (e: DataIntegrityViolationException) {
    logger.warn(e) { "Failed to create prisoner as it already exists, loading" }
    getPrisonerEntityFromNomsId(nomsId)
  }
}
