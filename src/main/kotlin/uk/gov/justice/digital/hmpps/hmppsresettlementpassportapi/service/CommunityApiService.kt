package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.communityapi.OffenderDetailSummaryDTO

@Service
class CommunityApiService(
  private val communityWebClientClientCredentials: WebClient,
) {
  suspend fun findCrn(nomsId: String): String? {
    val offenderDetails = communityWebClientClientCredentials.get()
      .uri("/secure/offenders/nomsNumber/$nomsId")
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in Community API") })
      .bodyToMono(OffenderDetailSummaryDTO::class.java)
      .awaitSingle()

    return offenderDetails.otherIds?.crn
  }
}
