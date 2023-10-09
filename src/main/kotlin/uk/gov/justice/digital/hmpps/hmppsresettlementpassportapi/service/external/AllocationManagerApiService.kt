package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonOffenderManagers
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.allocationapi.AllocationDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.allocationapi.AllocationStaffDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.convertNameToTitleCase

@Service
class AllocationManagerApiService(val allocationManagerWebClientCredentials: WebClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getPomsByNomsId(nomsId: String): PrisonOffenderManagers {
    val allocations = allocationManagerWebClientCredentials.get()
      .uri("/api/allocation/$nomsId")
      .retrieve()
      .bodyToMono(AllocationDTO::class.java)
      .onErrorReturn(
        {
          log.warn("Unexpected error from Allocation Manager API - ignoring but POM data will be missing from response!", it)
          it is WebClientException
        },
        AllocationDTO(AllocationStaffDTO(null, null), AllocationStaffDTO(null, null)),
      )
      .awaitSingle()

    var primaryPomName: String? = null
    if (allocations.primaryPom.name == null) {
      log.warn("No primaryPom found in Allocation Manager API for NomsId $nomsId")
    } else {
      primaryPomName = convertName(allocations.primaryPom.name)
    }

    var secondaryPomName: String? = null
    if (allocations.secondaryPom.name == null) {
      log.warn("No secondaryPom found in Allocation Manager API for NomsId $nomsId")
    } else {
      secondaryPomName = convertName(allocations.secondaryPom.name)
    }

    return PrisonOffenderManagers(primaryPomName, secondaryPomName)
  }

  fun convertName(name: String) = name.trim().split(", ").reversed().joinToString(" ").convertNameToTitleCase()
}
