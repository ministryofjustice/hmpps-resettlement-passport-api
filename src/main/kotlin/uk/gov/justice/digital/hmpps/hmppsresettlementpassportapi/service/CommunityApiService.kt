package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MappaData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi.MappaDataDto
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.communityapi.OffenderDetailSummaryDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository

@Service
class CommunityApiService(
  private val communityWebClientClientCredentials: WebClient,
  private val prisonerRepository: PrisonerRepository,
) {

  suspend fun findCrn(nomsId: String): String? {
    return prisonerRepository.findByNomsId(nomsId)?.crn
  }
  suspend fun getCrn(nomsId: String): String? {
    val offenderDetails = communityWebClientClientCredentials.get()
      .uri("/secure/offenders/nomsNumber/$nomsId")
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in Community API") })
      .bodyToMono(OffenderDetailSummaryDTO::class.java)
      .awaitSingle()

    return offenderDetails.otherIds?.crn
  }

  suspend fun getMappaDataByNomsId(nomsId: String): MappaData? {
    val crn = findCrn(nomsId) ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in database")
    val fullMappaData = communityWebClientClientCredentials.get()
      .uri("/secure/offenders/crn/$crn/risk/mappa")
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Cannot find MAPPA Data for NomsId $nomsId / CRN $crn in Community API") })
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
}
