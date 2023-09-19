package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MappaData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.communityapi.CaseIdentifiers
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.communityapi.Manager
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.communityapi.MappaDetail
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
      .uri("/probation-cases/$nomsId/crn")
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in Community API") },
      )
      .bodyToMono<CaseIdentifiers>()
      .awaitSingle()

    return offenderDetails.crn
  }

  suspend fun getMappaDataByNomsId(nomsId: String): MappaData? {
    val crn = findCrn(nomsId) ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in database")
    val mappaDetail = communityWebClientClientCredentials.get()
      .uri("/probation-cases/$crn/mappa")
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Cannot find MAPPA Data for NomsId $nomsId / CRN $crn in Community API") },
      )
      .bodyToMono<MappaDetail>()
      .awaitSingle()
    return MappaData(
      mappaDetail.level,
      mappaDetail.levelDescription,
      mappaDetail.category,
      mappaDetail.categoryDescription,
      mappaDetail.startDate,
      mappaDetail.reviewDate,
    )
  }

  suspend fun getComByNomsId(nomsId: String): String? {
    val crn = findCrn(nomsId) ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in database")

    val communityManager = communityWebClientClientCredentials.get()
      .uri("/probation-cases/$crn/community-manager")
      .retrieve()
      .bodyToMono<Manager>()
      .onErrorReturn(
        {
          log.warn("Unexpected error from Community API - ignoring but COM data will be missing from response!", it)
          true
        },
        Manager(null, true),
      ).awaitSingle()

    if (communityManager.unallocated || communityManager.name == null) {
      log.warn("No COM data found in Community API for NomsId $nomsId / CRN $crn")
      return null
    }

    return "${communityManager.name.forename} ${communityManager.name.surname}".convertNameToTitleCase()
  }
}
