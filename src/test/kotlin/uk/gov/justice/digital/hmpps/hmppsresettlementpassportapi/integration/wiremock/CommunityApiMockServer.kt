package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get

class CommunityApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8096
  }

  fun stubGetCrnFromNomsId(nomsId: String, crn: String) {
    stubFor(
      get("/secure/offenders/nomsNumber/$nomsId").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            """
              {
                "offenderId": 0,
                "otherIds": {
                  "crn": "$crn"
                }
              }
            """.trimIndent(),
          )
          .withStatus(200),
      ),
    )
  }

  fun stubGetCrnFromNomsIdNotFound(nomsId: String) {
    stubFor(
      get("/secure/offenders/nomsNumber/$nomsId").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody("{\"Error\" : \"Not Found\"}")
          .withStatus(404),
      ),
    )
  }
}
