package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class AllocationManagerApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8101
  }
  fun stubGet(path: String, status: Int, jsonResponseFile: String?) {
    stubFor(
      WireMock.get(path).willReturn(
        if (status == 200) {
          val json: String = if (jsonResponseFile != null) {
            readFile(jsonResponseFile)
          } else {
            "{}"
          }
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(json)
            .withStatus(200)
        } else {
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\"Error\" : \"$status\"}")
            .withStatus(status)
        },
      ),
    )
  }
}