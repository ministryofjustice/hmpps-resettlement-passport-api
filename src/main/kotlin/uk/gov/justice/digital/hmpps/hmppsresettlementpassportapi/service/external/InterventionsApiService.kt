package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.CRSAppointmentsDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.ReferralDTO

@Service
class InterventionsApiService(
  private val interventionsWebClientCredentials: WebClient,
) {

  fun fetchProbationCaseReferrals(nomsId: String, crn: String): List<ReferralDTO> = interventionsWebClientCredentials.get()
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

  fun fetchCRSAppointments(crn: String): List<CRSAppointmentsDTO> = interventionsWebClientCredentials.get()
    .uri("/probation-case/$crn/appointments")
    .retrieve()
    .bodyToFlux(CRSAppointmentsDTO::class.java)
    .collectList()
    .onErrorReturn(
      {
        it is WebClientResponseException && it.statusCode.isSameCodeAs(HttpStatus.NOT_FOUND)
      },
      listOf(),
    )
    .block() ?: throw RuntimeException("Unexpected null returned from request.")
}
