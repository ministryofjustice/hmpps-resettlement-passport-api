package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import java.time.LocalDate

class ResettlementPassportDeliusApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8102
  }

  fun stubGetAppointmentsFromCRN(crn: String, status: Int) {
    val appointmentsListJSON = readFile("testdata/resettlement-passport-delius-api/appointments-list.json")
    val formattedStartDate = LocalDate.now().minusDays(365).toString()
    val formattedEndDate = LocalDate.now().plusDays(365).toString()
    stubFor(
      get("/appointments/$crn?page=0&size=50&startDate=$formattedStartDate&endDate=$formattedEndDate").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              appointmentsListJSON,
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

  fun stubGetCrnFromNomsId(nomsId: String, crn: String) {
    stubFor(
      get("/probation-cases/$nomsId/crn").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            """
              {
                "crn": "$crn"
              }
            """.trimIndent(),
          )
          .withStatus(200),
      ),
    )
  }

  fun stubGetToCrn(path: String, status: Int, jsonResponseFile: String?) {
    stubFor(
      get(path).willReturn(
        if (status == 200) {
          val riskScoresJson: String = if (jsonResponseFile != null) {
            readFile(jsonResponseFile)
          } else {
            "{}"
          }
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(riskScoresJson)
            .withStatus(200)
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