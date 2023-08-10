package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.google.common.io.Resources

class ArnApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8097
  }

  fun stubGetRisksPredictorsFromCrn(crn: String, status: Int, jsonResponseFile: String?) {
    stubFor(
      get("/risks/crn/$crn/predictors/all").willReturn(
        if (status == 200) {
          val riskScoresJson: String = if (jsonResponseFile != null) {
            Resources.getResource(jsonResponseFile).readText()
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
