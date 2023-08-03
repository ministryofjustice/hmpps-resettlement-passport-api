package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prison

@Service
class PrisonApiService(
  private val prisonWebClient: WebClient,
  private val prisonWebClientClientCredentials: WebClient,
) {

  suspend fun getPrisons(): List<uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi.Prison> {
    return prisonWebClientClientCredentials
      .get()
      .uri("/prisons")
      .retrieve()
      .awaitBody()
  }

  suspend fun getPrisonsList(): MutableList<Prison> {
    val prisons = getPrisons()
    val prisonList = mutableListOf<Prison>()
    for (item in prisons) {
      prisonList.add(Prison(item.prisonId, item.prisonName, item.active))
    }
    prisonList.sortBy { it.name }
    return prisonList
  }

  suspend fun getActivePrisonsList(): MutableList<Prison> {
    val prisons = getPrisons()

    val prisonList = mutableListOf<Prison>()
    for (item in prisons) {
      if (item.active) {
        prisonList.add(Prison(item.prisonId, item.prisonName, true))
      }
    }
    prisonList.sortBy { it.name }
    return prisonList
  }
}
