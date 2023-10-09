package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class CaseNotesApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8099
  }

  fun stubGetCaseNotesOldList(nomsId: String, size: Int, page: Int, type: String, subType: String?, status: Int) {
    var casenotesJSON = readFile("testdata/casenotes-api/case-notes-gen.json")

    stubFor(
      get("/case-notes/$nomsId?page=$page&size=$size&type=$type&subType=$subType").willReturn(
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

  fun stubGetCaseNotesNewList(nomsId: String, size: Int, page: Int, type: String, status: Int) {
    val casenotesJSON = readFile("testdata/casenotes-api/case-notes.json")
    stubFor(
      get("/case-notes/$nomsId?page=$page&size=$size&type=$type").willReturn(
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

  fun stubGetCaseNotesSpecificPathway(nomsId: String, size: Int, page: Int, type: String, subType: String?, status: Int) {
    val casenotesJSON = readFile("testdata/casenotes-api/case-notes-pathway-accom.json")

    stubFor(
      get("/case-notes/$nomsId?page=$page&size=$size&type=$type&subType=$subType").willReturn(
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

  fun stubPostCaseNotes(nomsId: String, type: String, subType: String?, text: String, prisonId: String, status: Int) {
    val expectedCreateCaseNotesResponseJSON = readFile("testdata/expectation/case-notes-create-response.json")

    val casenotesJSON =
      """
      {
        "locationId": "$prisonId",
        "type": "$type",
        "subType": "$subType",
        "text": "$text"
      }
      """.trimIndent()

    stubFor(
      post("/case-notes/$nomsId")
        .withRequestBody(equalToJson(casenotesJSON, true, true))
        .willReturn(
          if (status == 200) {
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(
                expectedCreateCaseNotesResponseJSON,
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
