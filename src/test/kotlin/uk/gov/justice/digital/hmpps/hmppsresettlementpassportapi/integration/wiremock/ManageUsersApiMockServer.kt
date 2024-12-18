package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class ManageUsersApiMockServer : WireMockServerBase() {
  fun stubGetManageUsersData(prisonId: String, status: Int) {
    val getManageUsersList = readFile("testdata/manage-users-api/staff-list.json")
    stubFor(
      get("/prisonusers/find-by-caseload-and-role?activeCaseload=$prisonId&roleCode=PSFR_RESETTLEMENT_WORKER&status=ACTIVE&page=0&size=500&sort=firstName&activeCaseloadOnly=false").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              getManageUsersList,
            )
            .withStatus(status)
        } else {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\"Error\" : \"$status\"}")
            .withStatus(status)
        },
      ),
    )
  }

  fun stubGetManageUsersDataEmptyList(status: Int) {
    val getManageUsersEmptyList = readFile("testdata/manage-users-api/staff-list-empty.json")
    stubFor(
      get("/prisonusers/find-by-caseload-and-role?activeCaseload=MDI1&roleCode=PSFR_RESETTLEMENT_WORKER&status=ACTIVE&page=0&size=500&sort=firstName&activeCaseloadOnly=false").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              getManageUsersEmptyList,
            )
            .withStatus(status)
        } else {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\"Error\" : \"$status\"}")
            .withStatus(status)
        },
      ),
    )
  }
}
