package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class PoPUserApiMockServer : WireMockServerBase(9106) {

  fun stubPostPoPUserVerification(status: Int) {
    val popUserDataResponseJSON = readFile("testdata/pop-user-api/pop-user-verify-response.json")
    val popUserRequestJSON = readFile("testdata/pop-user-api/pop-user-request.json")
    stubFor(
      WireMock.post("/person-on-probation-user/user")
        .withRequestBody(WireMock.equalToJson(popUserRequestJSON, true, true))
        .willReturn(
          if (status == 200) {
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(
                popUserDataResponseJSON,
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
  fun stubGetPopUserVerifiedList(status: Int) {
    val getAllVerifiedUserListJSON = readFile("testdata/pop-user-api/pop-user-verify-list-response.json")
    stubFor(
      get("/person-on-probation-user/users/all").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              getAllVerifiedUserListJSON,
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

  fun stubGetPopUserVerifiedEmptyList(status: Int) {
    val getAllVerifiedUserListJSON = readFile("testdata/pop-user-api/pop-user-verify-list-response.json")
    stubFor(
      get("/person-on-probation-user/users/all").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              " [] ",
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
