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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class PathwayAndStatusService(
  private val pathwayStatusRepository: PathwayStatusRepository,
  private val prisonerRepository: PrisonerRepository,
  private val resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService,
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

  fun findAllPathwayStatusForPrisoner(prisoner: PrisonerEntity) = pathwayStatusRepository.findByPrisonerId(prisoner.id())

  fun updatePathwayStatusWithNewStatus(pathwayStatus: PathwayStatusEntity, newStatus: Status) {
    pathwayStatus.status = newStatus
    pathwayStatus.updatedDate = LocalDateTime.now()
    pathwayStatusRepository.save(pathwayStatus)
  }

  fun getOrCreatePrisoner(nomsId: String, prisonId: String?, releaseDate: LocalDate? = null, crn: String? = null): PrisonerEntity {
    // Seed the Prisoner data into the DB
    val existingPrisonerEntity = prisonerRepository.findByNomsId(nomsId)
    if (existingPrisonerEntity == null) {
      val resolvedCrn = crn ?: resettlementPassportDeliusApiService.getCrn(nomsId)
      return createPrisoner(nomsId, resolvedCrn, prisonId, releaseDate)
    } else if (existingPrisonerEntity.crn == null) {
      // If the CRN failed to be added last time, try again
      val resolvedCrn = crn ?: resettlementPassportDeliusApiService.getCrn(nomsId)
      if (resolvedCrn != null) {
        existingPrisonerEntity.crn = resolvedCrn
        return prisonerRepository.save(existingPrisonerEntity)
      }
    }
    return existingPrisonerEntity
  }

  internal fun createPrisoner(
    nomsId: String,
    resolvedCrn: String?,
    prisonId: String?,
    releaseDate: LocalDate?,
  ): PrisonerEntity = try {
    transactionOperations.execute {
      val newPrisonerEntity = prisonerRepository.save(
        PrisonerEntity(
          nomsId = nomsId,
          crn = resolvedCrn,
          prisonId = prisonId,
          releaseDate = releaseDate,
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
