package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.test.util.TestSocketUtils.findAvailableTcpPort
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

open class WireMockServerBase : WireMockServer(findAvailableTcpPort()) {
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

  fun jsonSuccess(body: String): ResponseDefinitionBuilder = WireMock.aResponse()
    .withStatus(200)
    .withHeader("Content-Type", "application/json")
    .withBody(body)
}
