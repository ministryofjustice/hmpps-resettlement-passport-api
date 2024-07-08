package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditionsMetadata
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditionsWithMetaData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.LicenceConditionChangeAuditEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.LicenceConditionsChangeAuditRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CvlApiService
import java.time.LocalDateTime

@Service
class LicenceConditionService(
  val cvlApiService: CvlApiService,
  private val licenceConditionsChangeAuditRepository: LicenceConditionsChangeAuditRepository,
  private val prisonerRepository: PrisonerRepository,

) {

  fun getLicenceConditionsByNomsId(nomsId: String): LicenceConditions {
    val licence = cvlApiService.getLicenceByNomsId(nomsId) ?: throw NoDataWithCodeFoundException(
      "Prisoner",
      nomsId,
    )
    return cvlApiService.getLicenceConditionsByLicenceId(licence.licenceId)
  }

  @Transactional
  fun getLicenceConditionsAndUpdateAudit(nomsId: String): LicenceConditionsWithMetaData {
    val licenceConditions = getLicenceConditionsByNomsId(nomsId)
    val metadata = compareAndSave(licenceConditions, nomsId)

    return LicenceConditionsWithMetaData(licenceConditions, metadata)
  }

  fun getImageFromLicenceIdAndConditionId(licenceId: String, conditionId: String): ByteArray =
    cvlApiService.getImageFromLicenceIdAndConditionId(licenceId, conditionId)

  @Transactional
  internal fun compareAndSave(licenceConditions: LicenceConditions, nomsId: String): LicenceConditionsMetadata {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException(
        "Prisoner with id $nomsId not found",
      )
    val prisonerId = prisoner.id!!

    val licenceConditionsChangeAuditEntity =
      licenceConditionsChangeAuditRepository.findFirstByPrisonerIdOrderByCreationDateDesc(prisonerId)

    val existingLicenseConditions = licenceConditionsChangeAuditEntity?.licenceConditions

    if (licenceConditionsChangeAuditEntity == null || existingLicenseConditions != licenceConditions) {
      val newLicenceConditionChangeAuditEntity = LicenceConditionChangeAuditEntity(
        prisonerId = prisonerId,
        licenceConditions = licenceConditions,
        version = licenceConditionsChangeAuditEntity?.version?.plus(1) ?: 1,
      )
      licenceConditionsChangeAuditRepository.save(newLicenceConditionChangeAuditEntity)
      return LicenceConditionsMetadata(changeStatus = true, newLicenceConditionChangeAuditEntity.version, false)
    }
    return LicenceConditionsMetadata(
      changeStatus = !licenceConditionsChangeAuditEntity.seen,
      version = licenceConditionsChangeAuditEntity.version,
      seen = licenceConditionsChangeAuditEntity.confirmationDate != null,
    )
  }

  @Transactional
  fun markConditionsSeen(nomsId: String, version: Int) {
    val licenceConditionChangeAuditEntity =
      (
        licenceConditionsChangeAuditRepository.getByNomsIdAndVersion(nomsId, version)
          ?: throw ResourceNotFoundException("No licence conditions record found for $nomsId / $version")
        )
    licenceConditionsChangeAuditRepository.save(licenceConditionChangeAuditEntity.copy(seen = true, confirmationDate = LocalDateTime.now()))
  }
}
