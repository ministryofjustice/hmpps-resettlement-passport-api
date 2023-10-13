package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import java.time.LocalDate

class ResettlementPassportDeliusApiMockServer : WireMockServerBase(9102) {

  fun stubGetAppointmentsFromCRN(crn: String, status: Int) {
    val appointmentsListJSON = readFile("testdata/resettlement-passport-delius-api/appointments-list.json")
    val formattedStartDate = LocalDate.now().toString()
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

  fun stubGetAccommodationFromCRN(crn: String, noFixAbode: Boolean, status: Int) {
    var dutyToReferNSIJSON = readFile("testdata/resettlement-passport-delius-api/duty-to-refer-nsi-abode-false.json")
    if (noFixAbode) {
      dutyToReferNSIJSON = readFile("testdata/resettlement-passport-delius-api/duty-to-refer-nsi-abode-true.json")
    }
    stubFor(
      get("/duty-to-refer-nsi/$crn").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              dutyToReferNSIJSON,
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

  fun stubGetComByCrn(crn: String, status: Int) {
    stubFor(
      get("/probation-cases/$crn/community-manager").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"name\" : { \"forename\" : \"John\", \"surname\": \"Doe\" }, \"unallocated\":false }")
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
