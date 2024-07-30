package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class CaseNotesApiMockServer : WireMockServerBase() {

  fun stubGetCaseNotesOldList(nomsId: String, size: Int, page: Int, type: String, subType: String?, status: Int) {
    val caseNotesJSON = readFile("testdata/case-notes-api/case-notes-gen.json")

    stubFor(
      get("/case-notes/$nomsId?page=$page&size=$size&type=$type&subType=$subType").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              caseNotesJSON,
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
    val caseNotesJSON = readFile("testdata/case-notes-api/case-notes.json")
    stubFor(
      get("/case-notes/$nomsId?page=$page&size=$size&type=$type").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              caseNotesJSON,
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
    val caseNotesJSON = readFile("testdata/case-notes-api/case-notes-pathway-accom.json")

    stubFor(
      get("/case-notes/$nomsId?page=$page&size=$size&type=$type&subType=$subType").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              caseNotesJSON,
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

    val caseNotesJSON =
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
        .withRequestBody(equalToJson(caseNotesJSON, true, true))
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

  fun stubGetCaseNotesNewSubTypeList(nomsId: String, size: Int, page: Int, type: String, status: Int) {
    val caseNotesJSON = readFile("testdata/case-notes-api/case-notes-new-subtype.json")
    stubFor(
      get("/case-notes/$nomsId?page=$page&size=$size&type=$type").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              caseNotesJSON,
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
