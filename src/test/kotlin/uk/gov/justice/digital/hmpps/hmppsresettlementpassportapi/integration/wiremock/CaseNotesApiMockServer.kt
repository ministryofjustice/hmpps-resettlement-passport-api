package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CaseNotesApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8099
  }

  fun stubGetCaseNotesOldList(nomisId: String, size: Int, page: Int, type: String, subType: String?, status: Int) {
    var casenotesJSON = readFile("testdata/casenotes-api/case-notes-gen.json")

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

  fun stubGetCaseNotesNewList(nomisId: String, size: Int, page: Int, type: String, status: Int) {
    val casenotesJSON = readFile("testdata/casenotes-api/case-notes.json")
    // TODO "GEN" Need to be replace with "RESET" --DONE
    stubFor(
      get("/case-notes/$nomisId?page=$page&size=$size&type=$type").willReturn(
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

  fun stubGetCaseNotesOldListWithDateRange(nomisId: String, size: Int, page: Int, type: String, subType: String?, days: Int, status: Int) {
    var casenotesJSON = readFile("testdata/casenotes-api/case-notes-gen.json")
    casenotesJSON.replace(
      "\"occurrenceDateTime\": \"2023-08-10T07:00:48\",",
      "\"occurrenceDateTime\":" + LocalDate.now().minusDays(days.toLong()).atStartOfDay().format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      ),
    )
    val startDate = LocalDate.now().minusDays(days.toLong()).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString()
    val endDate = LocalDate.now().plusDays(1).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString()
    stubFor(
      get("/case-notes/$nomisId?page=$page&size=$size&type=$type&subType=$subType&startDate=$startDate&endDate=$endDate").willReturn(
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

  fun stubGetCaseNotesNewListWithDateRange(nomisId: String, size: Int, page: Int, type: String, days: Int, status: Int) {
    val casenotesJSON = readFile("testdata/casenotes-api/case-notes.json")
    casenotesJSON.replace("\"occurrenceDateTime\": \"2023-08-10T07:00:48\",", "\"occurrenceDateTime\":" + LocalDate.now().minusDays(days.toLong()).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    val startDate = LocalDate.now().minusDays(days.toLong()).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString()
    val endDate = LocalDate.now().plusDays(1).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString()

    stubFor(
      get("/case-notes/$nomisId?page=$page&size=$size&type=$type&startDate=$startDate&endDate=$endDate").willReturn(

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

  fun stubGetCaseNotesSpecificPathway(nomisId: String, size: Int, page: Int, type: String, subType: String?, status: Int) {
    var casenotesJSON = readFile("testdata/casenotes-api/case-notes-pathway-accom.json")

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

  fun stubPostCaseNotes(nomisId: String, type: String, subType: String?, text: String, prisonId: String, status: Int) {
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
      post("/case-notes/$nomisId")
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
