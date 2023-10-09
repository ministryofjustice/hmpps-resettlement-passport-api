package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prison

@Service
class PrisonRegisterApiService(
  private val prisonRegisterWebClientClientCredentials: WebClient,
) {

  private suspend fun getPrisons(): List<uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi.Prison> {
    return prisonRegisterWebClientClientCredentials
      .get()
      .uri("/prisons")
      .retrieve()
      .awaitBody()
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
