package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoners
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonerRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearchList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

@Service
class OffenderSearchApiService(
  private val offendersSearchWebClientClientCredentials: WebClient,
) {

  fun findPrisonersBySearchTerm(prisonId: String): Flow<List<PrisonersSearch>> = flow {
    var page = 0
    do {
      val pageOfData = offendersSearchWebClientClientCredentials.get()
        .uri(
          "/prison/{prisonId}/prisoners?size={size}&page={page}&sort={sort}",
          mapOf(
            "prisonId" to prisonId,
            "size" to 500, // NB: API allows up 3,000 results per page
            "page" to page,
            "sort" to "prisonerNumber,ASC",
          ),
        )
        .retrieve()
        .awaitBody<PrisonersSearchList>()
      emit(pageOfData.content!!)
      page += 1
    } while (!pageOfData.last)
  }

  fun findPrisonersByReleaseDate(prisonId: String, earliestReleaseDate: String, latestReleaseDate: String, prisonIds: List<String>): Flow<List<PrisonersSearch>> = flow {
    var page = 0
    do {
      val pageOfData = offendersSearchWebClientClientCredentials.post()
        .uri(
          "/prisoner-search/release-date-by-prison?size={size}&page={page}&sort={sort}",
          mapOf(
            "prisonId" to prisonId,
            "size" to 50, // NB: API allows up 3,000 results per page
            "page" to page,
            "sort" to "releaseDate,ASC",
          ),
        ).bodyValue(
          PrisonerRequest(
            earliestReleaseDate = earliestReleaseDate,
            latestReleaseDate = latestReleaseDate,
            prisonIds = prisonIds,
          ),
        )
        .retrieve()
        .awaitBody<PrisonersSearchList>()
      emit(pageOfData.content!!)
      page += 1
    } while (!pageOfData.last)
  }

  /**
   * Searches for offenders in a prison using prison ID  (e.g. MDI)
   * returning a complete list.
   * Requires role PRISONER_IN_PRISON_SEARCH or PRISONER_SEARCH
   */
  suspend fun getPrisonersByPrisonId(dateRangeAPI: Boolean, prisonId: String, days: Long, pageNumber: Int, pageSize: Int, sort: String): PrisonersList {
    val offenders = mutableListOf<PrisonersSearch>()
    val prisoners = mutableListOf<Prisoners>()
    if (dateRangeAPI) {
      val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val earliestReleaseDate = LocalDate.now().minusDays(days).format(pattern)
      val latestReleaseDate = LocalDate.now().plusDays(days).format(pattern)
      val prisonIds = ArrayList<String>()
      prisonIds.add(prisonId)
      findPrisonersByReleaseDate(
        prisonId,
        earliestReleaseDate.toString(),
        latestReleaseDate.toString(),
        prisonIds,
      ).collect {
        offenders.addAll(it)
      }
    } else {
      findPrisonersBySearchTerm(prisonId).collect {
        offenders.addAll(it)
      }
    }

    when (sort) {
      "releaseDate,ASC" -> offenders.sortBy { it.releaseDate }
      "firstName,ASC" -> offenders.sortBy { it.firstName }
      "lastName,ASC" -> offenders.sortBy { it.lastName }
      "prisonerNumber,ASC" -> offenders.sortBy { it.prisonerNumber }
      "releaseDate,DESC" -> offenders.sortByDescending { it.releaseDate }
      "firstName,DESC" -> offenders.sortByDescending { it.firstName }
      "lastName,DESC" -> offenders.sortByDescending { it.lastName }
      "prisonerNumber,DESC" -> offenders.sortByDescending { it.prisonerNumber }
    }

    val startIndex = (pageNumber * pageSize)
    val endIndex = (pageNumber * pageSize) + (pageSize)
    val prisonerList = ArrayList<Prisoners>()
    if (startIndex < endIndex && endIndex <= offenders.size) {
      val searchList = offenders.subList(startIndex, endIndex)
      searchList.forEach {
        val prisoner = Prisoners(it.prisonerNumber,it.firstName, it.middleNames, it.lastName, it.releaseDate, it.nonDtoReleaseDateType )
        //PathwayStatus to be Included.
        prisonerList.add(prisoner);
      }
      return PrisonersList(prisonerList, prisonerList.toList().size, pageNumber, sort, prisonerList.size, (endIndex == prisonerList.size))
    } else if (startIndex < endIndex) {
      val searchList = offenders.subList(startIndex, offenders.size)
      //val pathwayList = Pathway.values();
      //val pathwayStatusList = List<PathwayStatus>

      searchList.forEach {
        val prisoner = Prisoners(it.prisonerNumber,it.firstName, it.middleNames, it.lastName, it.releaseDate, it.nonDtoReleaseDateType )
        //PathwayStatus to be Included.
       //prisoner.status =
        prisonerList.add(prisoner);
      }
      return PrisonersList(prisonerList, prisonerList.toList().size, pageNumber, sort, prisonerList.size, true)
    }
    return PrisonersList(null, null, null, null, 0, false)
  }
}
