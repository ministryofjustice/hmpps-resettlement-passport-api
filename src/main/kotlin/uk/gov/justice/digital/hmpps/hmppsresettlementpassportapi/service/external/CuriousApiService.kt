package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.curiousapi.LearnerEducationDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.curiousapi.LearnersEducationList

@Service
class CuriousApiService(
  private val curiousWebClientCredentials: WebClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Cacheable("curious-api-get-learner-education-by-noms-id")
  fun getLearnersEducation(nomsId: String): LearnersEducationList {
    val listToReturn = LearnersEducationList(
      mutableListOf<LearnerEducationDTO>(), true, false, false, 0, 0, null, 0, emptyList(), 0, 0,
    )

    var page = 0
    do {
      val data = curiousWebClientCredentials.get()
        .uri(
          "/sequation-virtual-campus2-api/learnerEducation/{nomsId}?size={size}&page={page}",
          mapOf(
            "nomsId" to nomsId,
            "size" to 1,
            "page" to page,

          ),
        )
        .retrieve()
        .onStatus(
          { it == HttpStatus.NOT_FOUND },
          { throw ResourceNotFoundException("Prisoner $nomsId not found in learner education curious api") },
        )

      val pageOfData = data.bodyToMono(LearnersEducationList::class.java).block()
      if (pageOfData != null) {
        listToReturn.content?.addAll(pageOfData.content!!)
      }
      page += 1
    } while (!pageOfData?.last!!)
    listToReturn.totalElements = listToReturn.content?.size

    return listToReturn
  }
}
