package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.google.common.io.Resources
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import java.io.File

class OffenderSearchApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8094
  }

  fun stubGetPrisonersList(prisonId: String, term: String, size: Int, page: Int, status: Int) {
    val prisonersListJSON = readFile("testdata/offender-search-api/prisoner-offender-search.json")
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

  fun stubGetPrisonerDetails(nomisId: String, status: Int) {
    val prisonerDataJSON = readFile("testdata/offender-search-api/prisoner-offender-details.json")
    stubFor(
      get("/prisoner/$nomisId").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              prisonerDataJSON,
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
