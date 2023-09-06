package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class CaseNotesApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8099
  }

  // &startDate=$startDate&endDate=$endDate
  // &startDate=$startDate&endDate=$endDate
  fun stubGetCaseNotesOldList(nomisId: String, size: Int, page: Int, type: String, subType: String?, status: Int) {
    var casenotesJSON = readFile("testdata/casenotes-api/case-notes.json")

    stubFor(
      get("/case-notes/$nomisId?page=$page&size=$size&type=$type&subType=$subType").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              casenotesJSON,
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

  fun stubGetCaseNotesNewList(nomisId: String, size: Int, page: Int, status: Int) {
    var casenotesJSON = readFile("testdata/casenotes-api/case-notes-gen.json")
    // TODO "GEN" Need to be replace with "RESET"
    stubFor(
      get("/case-notes/$nomisId?page=$page&size=$size&type=GEN").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              casenotesJSON,
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
