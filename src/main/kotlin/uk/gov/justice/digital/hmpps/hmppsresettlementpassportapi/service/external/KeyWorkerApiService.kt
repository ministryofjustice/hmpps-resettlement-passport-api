package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.keyworkerapi.KeyWorkerDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.convertNameToTitleCase

@Service
class KeyWorkerApiService(val keyWorkerWebClientCredentials: WebClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getKeyWorkerName(nomsId: String): String? {
    val keyWorker = keyWorkerWebClientCredentials.get()
      .uri("/key-worker/offender/$nomsId")
      .retrieve()
      .bodyToMono(KeyWorkerDTO::class.java)
      .onErrorReturn(
        {
          log.warn("Unexpected error from Key Worker API - ignoring but Key Worker data will be missing from response!", it)
          it is WebClientException
        },
        KeyWorkerDTO(null, null, null),
      )
      .block()

    if (keyWorker?.firstName == null || keyWorker.lastName == null) {
      log.warn("No Key Worker data found in Key Worker API for NomsId $nomsId")
      return null
    }

    return "${keyWorker.firstName} ${keyWorker.lastName}".convertNameToTitleCase()
  }
}
