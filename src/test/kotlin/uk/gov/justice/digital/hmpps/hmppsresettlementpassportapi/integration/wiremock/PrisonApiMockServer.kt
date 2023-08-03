package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get

class PrisonApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8093
  }

  fun stubPrisonList(status: Int) {
    stubFor(
      get("/prisons").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              // language=json
              """
              [
                {
                  "prisonId": "AKI",
                  "prisonName": "Acklington (HMP)",
                  "active": "true"
                },
                {
                  "prisonId": "SWI",
                  "prisonName": "Swansea (HMP & YOI)",
                  "active": "false"
                }
              ]
              """,
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
