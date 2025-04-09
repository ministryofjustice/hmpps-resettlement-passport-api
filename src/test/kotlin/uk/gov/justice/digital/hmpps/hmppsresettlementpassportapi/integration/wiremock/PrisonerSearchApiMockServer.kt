package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class PrisonerSearchApiMockServer : WireMockServerBase() {

  fun stubGetPrisonersList(prisonId: String, size: Int, page: Int, term: String, status: Int) {
    stubGetPrisonersList("testdata/prisoner-search-api/prisoner-search-1.json", prisonId, size, page, term, status)
  }

  fun stubGetPrisonersList(jsonResponseFile: String, prisonId: String, size: Int, page: Int, term: String, status: Int) {
    val prisonersListJSON = readFile(jsonResponseFile)
    stubFor(
      get("/prison/$prisonId/prisoners?term=$term&size=$size&page=$page&sort=prisonerNumber").willReturn(
        if (status == 200) {
          jsonSuccess(prisonersListJSON)
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
    val prisonerDataJSON = readFile("testdata/prisoner-search-api/prisoner-details.json")
    stubFor(
      get("/prisoner/$nomsId").willReturn(
        if (status == 200) {
          jsonSuccess(prisonerDataJSON)
        } else {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\"Error\" : \"$status\"}")
            .withStatus(status)
        },
      ),
    )
  }

  fun stubMatchPrisonerOneMatch() {
    stubFor(
      post("/prisoner-search/match-prisoners").withRequestBody(
        equalToJson(
          """{ "firstName": "John", "lastName": "Smith" }""",
        ),
      ).willReturn(jsonSuccess(readFile("testdata/prisoner-search-api/match-response-1-match.json"))),
    )
  }

  fun stubMatchPrisonerNoMatch() {
    stubFor(
      post("/prisoner-search/match-prisoners").withRequestBody(
        equalToJson(
          """{ "firstName": "John", "lastName": "Smith" }""",
        ),
      ).willReturn(jsonSuccess("[]")),
    )
  }

  fun stubMatchPrisonerDuplicatedMatch() {
    stubFor(
      post("/prisoner-search/match-prisoners").withRequestBody(
        equalToJson(
          """{ "firstName": "John", "lastName": "Smith" }""",
        ),
      ).willReturn(jsonSuccess(readFile("testdata/prisoner-search-api/match-response-duplicate-match.json"))),
    )
  }
}
