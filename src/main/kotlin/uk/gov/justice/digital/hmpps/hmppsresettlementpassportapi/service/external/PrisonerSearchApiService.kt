package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearchList

@Service
class PrisonerSearchApiService(
  private val prisonerSearchWebClientClientCredentials: WebClient,
  private val registry: MeterRegistry,
) {
  private val componentName = "PRISONER-SEARCH"

  @Cacheable("prisoner-search-api-find-prisoners-by-prison-id")
  fun findPrisonersByPrisonId(prisonId: String): List<PrisonersSearch> {
    val listToReturn = mutableListOf<PrisonersSearch>()
    try {
      var page = 0
      do {
        val data = prisonerSearchWebClientClientCredentials.get()
          .uri(
            "/prison/{prisonId}/prisoners?size={size}&page={page}&sort={sort}",
            mapOf(
              "prisonId" to prisonId,
              // NB: API allows up 3,000 results per page
              "size" to 500,
              "page" to page,
              "sort" to "prisonerNumber",
            ),
          )
          .retrieve()
          .onStatus({ it.is5xxServerError }, {
            registry.counter("external_api_5xx_fail", Tags.of("service", componentName)).increment()
            throw Exception(it.toString())
          })
          .onStatus(
            { it.is4xxClientError },
          ) {
            registry.counter("external_api_4xx_fail", Tags.of("service", componentName)).increment()
            if (it.statusCode() == HttpStatus.NOT_FOUND) {
              throw ResourceNotFoundException("PrisonId $prisonId not found")
            } else {
              throw Exception(it.toString())
            }
          }
          .onStatus(
            { !it.is2xxSuccessful },
          ) {
            registry.counter("external_api_fail", Tags.of("service", componentName)).increment()
            throw Exception(it.toString())
          }

        val pageOfData = data.bodyToMono<PrisonersSearchList>().block()
        if (pageOfData != null) {
          listToReturn.addAll(pageOfData.content!!)
        }
        page += 1
      } while (!pageOfData?.last!!)
    } catch (e: Exception) {
      registry.counter("external_api_fail", Tags.of("service", componentName)).increment()
      throw e
    }
    return listToReturn
  }

  @Cacheable("prisoner-search-api-find-prisoners-personal-details")
  fun findPrisonerPersonalDetails(nomsId: String): PrisonersSearch {
    return prisonerSearchWebClientClientCredentials
      .get()
      .uri(
        "/prisoner/{nomsId}",
        mapOf(
          "nomsId" to nomsId,
        ),
      )
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Prisoner $nomsId not found in prisoner search api") },
      )
      .bodyToMono<PrisonersSearch>()
      .block() ?: throw RuntimeException("Unexpected null returned from request.")
  }
}
