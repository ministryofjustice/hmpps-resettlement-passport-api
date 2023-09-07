package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MappaData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi.MappaDataDto
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.communityapi.OffenderDetailSummaryDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.communityapi.OffenderManagerDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository

@Service
class CommunityApiService(
  private val communityWebClientClientCredentials: WebClient,
  private val prisonerRepository: PrisonerRepository,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun findCrn(nomsId: String): String? {
    return prisonerRepository.findByNomsId(nomsId)?.crn
  }

  suspend fun getCrn(nomsId: String): String? {
    val offenderDetails = communityWebClientClientCredentials.get()
      .uri("/secure/offenders/nomsNumber/$nomsId")
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in Community API") },
      )
      .bodyToMono(OffenderDetailSummaryDTO::class.java)
      .awaitSingle()

    return offenderDetails.otherIds?.crn
  }

  suspend fun getMappaDataByNomsId(nomsId: String): MappaData? {
    val crn = findCrn(nomsId) ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in database")
    val fullMappaData = communityWebClientClientCredentials.get()
      .uri("/secure/offenders/crn/$crn/risk/mappa")
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Cannot find MAPPA Data for NomsId $nomsId / CRN $crn in Community API") },
      )
      .bodyToMono(MappaDataDto::class.java)
      .awaitSingle()
    return MappaData(
      fullMappaData.level,
      fullMappaData.levelDescription,
      fullMappaData.category,
      fullMappaData.categoryDescription,
      fullMappaData.startDate,
      fullMappaData.reviewDate,
    )
  }

  suspend fun getComByNomsId(nomsId: String): String? {
    val crn = findCrn(nomsId) ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in database")

    val offenderManagers = communityWebClientClientCredentials.get()
      .uri("/secure/offenders/crn/$crn/allOffenderManagers?includeProbationAreaTeams=true")
      .retrieve()
      .bodyToMono(object : ParameterizedTypeReference<List<OffenderManagerDTO>>() {})
      .onErrorReturn(
        {
          log.warn("Unexpected error from Community API - ignoring but COM data will be missing from response!", it)
          it is WebClientException
        },
        listOf(),
      )
      .awaitSingle()

    val comOffenderManager = findComFromPrisonOffenderManager(offenderManagers)
    if (comOffenderManager?.staff?.forenames == null || comOffenderManager.staff.surname == null) {
      log.warn("No COM data found in Community API for NomsId $nomsId / CRN $crn")
      return null
    }

    return "${comOffenderManager.staff.forenames} ${comOffenderManager.staff.surname}".convertNameToTitleCase()
  }

  protected fun findComFromPrisonOffenderManager(offenderManagers: List<OffenderManagerDTO>) = offenderManagers.firstOrNull {
    it.isUnallocated == false && it.isPrisonOffenderManager == false
  }

}
