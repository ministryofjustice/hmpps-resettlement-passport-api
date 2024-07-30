package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class PrisonRegisterApiMockServer : WireMockServerBase() {

  fun stubPrisonList(status: Int) {
    val prisonListJSON = readFile("testdata/prison-register-api/prison.json")
    stubFor(
      get("/prisons").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              prisonListJSON,
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

  fun stubPrisonListNoData(status: Int) {
    val prisonListJSON = readFile("testdata/prison-register-api/prison-no-data.json")
    stubFor(
      get("/prisons").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              prisonListJSON,
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
