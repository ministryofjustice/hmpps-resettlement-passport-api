package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.ReferralDTO

@Service
class InterventionsApiService(
  private val interventionsWebClientCredentials: WebClient,
) {

  suspend fun fetchProbationCaseReferrals(nomsId: String, crn: String): List<ReferralDTO> = interventionsWebClientCredentials.get()
    .uri("/probation-case/$crn/referral")
    .retrieve()
    .bodyToFlux(ReferralDTO::class.java)
    .collectList()
    .onErrorReturn(
      {
        it is WebClientResponseException && it.statusCode.isSameCodeAs(HttpStatus.NOT_FOUND)
      },
      listOf()
    )
    .awaitSingle()

}
