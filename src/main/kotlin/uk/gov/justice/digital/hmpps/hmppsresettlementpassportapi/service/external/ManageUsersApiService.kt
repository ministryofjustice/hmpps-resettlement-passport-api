package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.manageusersapi.ManageUser
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.manageusersapi.PagedResponse

@Service
class ManageUsersApiService(val manageUsersWebClientCredentials: WebClient) {

  fun getManageUsersData(prisonId: String, roleCode: String): List<ManageUser> {
    val listToReturn = mutableListOf<ManageUser>()
    var page = 0
    do {
      val data = manageUsersWebClientCredentials.get()
        .uri(
          "/prisonusers/find-by-caseload-and-role?activeCaseload={activeCaseload}&roleCode={roleCode}&status={status}&page={page}&size={size}&sort={sort}&activeCaseloadOnly=false",
          mapOf(
            "activeCaseload" to prisonId,
            // NB: API allows up 3,000 results per page
            "roleCode" to roleCode,
            "status" to "ACTIVE",
            "page" to page,
            "size" to 500,
            "sort" to "firstName",
          ),
        )
        .retrieve()
        .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("PrisonId $prisonId with case load $roleCode not found") })

      val pageOfData = data.bodyToMono<PagedResponse>().block()
      if (pageOfData != null) {
        listToReturn.addAll(pageOfData.content)
      }
      page++
    } while (!pageOfData?.last!!)
    return listToReturn
  }
}
