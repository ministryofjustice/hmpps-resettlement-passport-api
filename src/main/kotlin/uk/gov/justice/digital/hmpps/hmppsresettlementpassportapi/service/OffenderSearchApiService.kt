package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi.OffendersList

@Service
class OffenderSearchApiService(
  private val offenderSearchWebClient: WebClient,
) {

  fun findOffendersBySearchTerm(prisonId: String, term: String = "", pageable: Pageable): Flow<OffendersList> = flow {
    // var page = 0
    // do {
    val pageOfData = offenderSearchWebClient.get()
      .uri(
        "/prison/{prisonId}/prisoners?term={term}&size={size}&page={page}&sort={sort}",
        mapOf(
          "prisonId" to prisonId,
          "term" to term,
          "size" to pageable.pageSize, // NB: API allows up 3,000 results per page
          "page" to pageable.pageNumber,
          "sort" to "prisonerNumber,ASC",
        ),
      )
      .retrieve()
      .awaitBody<OffendersList>()
    emit(pageOfData)
    //  page += 1
    //  } while (!pageOfData.last)
  }

  /**
   * Searches for offenders in a prison using term firstName or LastName  (e.g. ARC)
   * returning a complete list.
   * Requires role PRISONER_IN_PRISON_SEARCH or PRISONER_SEARCH
   */
  suspend fun getOffendersBySearchTerm(prisonId: String, term: String = "", pageable: Pageable): Flow<OffendersList> {
    /*val offenders = mutableListOf<Offenders>()
    findOffendersBySearchTerm(prisonId, term, pageable).collect {
      offenders.addAll(it)
    }
    return offenders
    */
    return findOffendersBySearchTerm(prisonId, term, pageable)
  }
}
