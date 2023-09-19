package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class CommunityApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8096
  }

  fun stubGetCrnFromNomsId(nomsId: String, crn: String) {
    stubFor(
      get("/probation-cases/$nomsId/crn").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            """
              {
                "crn": "$crn"
              }
            """.trimIndent(),
          )
          .withStatus(200),
      ),
    )
  }

  fun stubGetToCrn(path: String, status: Int, jsonResponseFile: String?) {
    stubFor(
      get(path).willReturn(
        if (status == 200) {
          val riskScoresJson: String = if (jsonResponseFile != null) {
            readFile(jsonResponseFile)
          } else {
            "{}"
          }
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(riskScoresJson)
            .withStatus(200)
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
