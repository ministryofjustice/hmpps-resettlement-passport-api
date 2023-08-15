package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import java.io.File

class OffenderSearchApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8094
  }

  fun stubGetPrisonersList(prisonId: String, term: String, size: Int, page: Int, status: Int) {
    val prisonersListJSON = File("src/test/resources/testdata/prisoners/prisoner-offender-search.json").inputStream().readBytes().toString(Charsets.UTF_8)
    stubFor(
      get("/prison/$prisonId/prisoners?term=$term&size=$size&page=$page&sort=prisonerNumber").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              prisonersListJSON,
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
