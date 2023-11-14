package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ciagapi.CIAGProfileDTO

@Service
class CiagApiService(
  val ciagWebClientCredentials: WebClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getCiagProfileByNomsId(nomsId: String): CIAGProfileDTO {
    return ciagWebClientCredentials.get()
      .uri("/ciag/induction/$nomsId")
      .retrieve()
      .bodyToMono(CIAGProfileDTO::class.java)
      .onErrorReturn(
        {
          log.warn("Unexpected error from CIAG API - ignoring but data will be missing from response!", it)
          it is WebClientException
        },
        CIAGProfileDTO(null, null, null, null, null, null, null, null, null),
      )
      .block() ?: throw RuntimeException("Unexpected null returned from request.")
  }
}
