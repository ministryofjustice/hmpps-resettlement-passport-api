package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.CRSAppointmentsDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.ReferralDTO

@Service
class InterventionsApiService(
  private val interventionsWebClientCredentials: WebClient,
) {

  @Cacheable("interventions-api-fetch-probation-case-referrals")
  fun fetchProbationCaseReferrals(crn: String): List<ReferralDTO> = interventionsWebClientCredentials.get()
    .uri("/probation-case/$crn/referral")
    .retrieve()
    .bodyToFlux(ReferralDTO::class.java)
    .collectList()
    .onErrorReturn(
      {
        it is WebClientResponseException && it.statusCode.isSameCodeAs(HttpStatus.NOT_FOUND)
      },
      listOf(),
    )
    .block() ?: throw RuntimeException("Unexpected null returned from request.")

  fun fetchCRSAppointments(crn: String): CRSAppointmentsDTO {
    return interventionsWebClientCredentials.get()
      .uri("/appointments-location/$crn")
      .retrieve()
      .bodyToMono(CRSAppointmentsDTO::class.java)
      .onErrorReturn(
        {
          log.warn("Unexpected error from Intervention Service  API - ignoring but data will be missing from response!", it)
          it is WebClientException
        },
        CRSAppointmentsDTO(crn = crn, referral = mutableListOf()),
      )
      .block() ?: throw RuntimeException("Unexpected null returned from request.")
  }
}
