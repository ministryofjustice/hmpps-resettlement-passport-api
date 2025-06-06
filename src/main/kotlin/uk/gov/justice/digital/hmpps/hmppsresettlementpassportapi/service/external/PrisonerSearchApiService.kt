package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearchList

@Service
class PrisonerSearchApiService(
  private val prisonerSearchWebClientClientCredentials: WebClient,
) {

  @Cacheable("prisoner-search-api-find-prisoners-by-search-term")
  fun findPrisonersBySearchTerm(prisonId: String, searchTerm: String?): List<PrisonersSearch> {
    val listToReturn = mutableListOf<PrisonersSearch>()

    var page = 0
    do {
      val data = prisonerSearchWebClientClientCredentials.get()
        .uri(
          "/prison/{prisonId}/prisoners?term={term}&size={size}&page={page}&sort={sort}",
          mapOf(
            "prisonId" to prisonId,
            "term" to (searchTerm ?: ""),
            // NB: API allows up 3,000 results per page
            "size" to 500,
            "page" to page,
            "sort" to "prisonerNumber",
          ),
        )
        .retrieve()
        .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("PrisonId $prisonId not found") })

      val pageOfData = data.bodyToMono<PrisonersSearchList>().block()
      if (pageOfData != null) {
        listToReturn.addAll(pageOfData.content!!)
      }
      page += 1
    } while (!pageOfData?.last!!)
    return listToReturn
  }

  @Cacheable("prisoner-search-api-find-prisoners-personal-details")
  fun findPrisonerPersonalDetails(nomsId: String): PrisonersSearch = prisonerSearchWebClientClientCredentials
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

  @Cacheable("prisoner-search-api-match-prisoners")
  fun match(matchRequest: PrisonerMatchRequest): List<PrisonersSearch> = prisonerSearchWebClientClientCredentials.post()
    .uri("/prisoner-search/match-prisoners")
    .bodyValue(matchRequest)
    .retrieve()
    .bodyToFlux<PrisonersSearch>()
    .collectList()
    .block() ?: throw RuntimeException("Unexpected null returned from request.")
}

data class PrisonerMatchRequest(
  val firstName: String,
  val lastName: String,
)
