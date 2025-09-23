package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.keyworkerapi.AllocationsDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.convertNameToTitleCase

@Service
class KeyWorkerApiService(val keyWorkerWebClientCredentials: WebClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Cacheable("key-worker-api-get-key-worker-name", unless = "#result == null")
  fun getKeyWorkerName(nomsId: String): String? {
    val allocations = keyWorkerWebClientCredentials.get()
      .uri("/prisoners/$nomsId/allocations/current")
      .retrieve()
      .bodyToMono(AllocationsDTO::class.java)
      .onErrorReturn(
        {
          log.warn("Unexpected error from Key Worker API - ignoring but Key Worker data will be missing from response!", it)
          it is WebClientException
        },
        AllocationsDTO(null),
      )
      .block()

    // There will be only one key worker allocation, with policy.code == KEY_WORKER.
    val keyWorkerAllocation = allocations?.allocations?.firstOrNull { it.policy.code == "KEY_WORKER" }
    val keyWorkerFirstName = keyWorkerAllocation?.staffMember?.firstName
    val keyWorkerLastName = keyWorkerAllocation?.staffMember?.lastName
    if (keyWorkerFirstName == null || keyWorkerLastName == null) {
      log.warn("No Key Worker data found in Key Worker API for NomsId $nomsId")
      return null
    }

    return "$keyWorkerFirstName $keyWorkerLastName".convertNameToTitleCase()
  }
}
