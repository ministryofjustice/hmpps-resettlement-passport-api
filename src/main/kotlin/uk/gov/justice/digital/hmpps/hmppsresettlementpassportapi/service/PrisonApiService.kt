package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi.Prison

@Service
class PrisonApiService(
  private val prisonWebClient: WebClient,
  private val prisonWebClientClientCredentials: WebClient,
) {

  private fun getClient(useClientCredentials: Boolean = false): WebClient {
    return if (useClientCredentials) prisonWebClientClientCredentials else prisonWebClient
  }

  suspend fun getPrisons(useClientCredentials: Boolean = false): List<Prison> {
    return getClient(useClientCredentials)
      .get()
      .uri("/prisons")
      .retrieve()
      .awaitBody()
  }

  suspend fun getPrisonById(useClientCredentials: Boolean = false, prisonId: String): Prison {
    return getClient(useClientCredentials)
      .get()
      .uri("/prisons/id/" + prisonId)
      .retrieve()
      .awaitBody()
  }

  suspend fun getPrisonVideolinkConferenceCentreEmailAddress(useClientCredentials: Boolean = false, prisonId: String): String {
    return getClient(useClientCredentials)
      .get()
      .uri("/secure/prisons/id/" + prisonId + "/videolink-conferencing-centre/email-address")
      .retrieve()
      .awaitBody()
  }
}
