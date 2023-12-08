package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class OffenderSearchApiMockServer : WireMockServerBase(9094) {

  fun stubGetPrisonersList(prisonId: String, term: String, size: Int, page: Int, status: Int) {
    stubGetPrisonersList("testdata/offender-search-api/prisoner-offender-search-1.json", prisonId, term, size, page, status)
  }

  fun stubGetPrisonersList(jsonResponseFile: String, prisonId: String, term: String, size: Int, page: Int, status: Int) {
    val prisonersListJSON = readFile(jsonResponseFile)
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

  fun stubGetPrisonerDetails(nomsId: String, status: Int) {
    val prisonerDataJSON = readFile("testdata/offender-search-api/prisoner-offender-details.json")
    stubFor(
      get("/prisoner/$nomsId").willReturn(
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
