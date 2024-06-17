package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux

@Service
class PrisonRegisterApiService(
  private val prisonRegisterWebClientClientCredentials: WebClient,
) {

  @Cacheable("prison-register-api-get-prisons")
  fun getPrisons(): List<uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi.Prison> {
    return prisonRegisterWebClientClientCredentials
      .get()
      .uri("/prisons")
      .retrieve()
      .bodyToFlux<uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi.Prison>()
      .collectList()
      .block() ?: throw RuntimeException("Unexpected null returned from request.")
  }
}
