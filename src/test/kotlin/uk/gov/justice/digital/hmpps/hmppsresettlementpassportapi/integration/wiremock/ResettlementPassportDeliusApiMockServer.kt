package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import java.time.LocalDate

class ResettlementPassportDeliusApiMockServer : WireMockServerBase(9102) {

  fun stubGetAppointmentsFromCRN(crn: String, status: Int) {
    val appointmentsListJSON = readFile("testdata/resettlement-passport-delius-api/appointments-list.json")
    val formattedStartDate = LocalDate.now().toString()
    val formattedEndDate = LocalDate.now().plusDays(365).toString()
    stubFor(
      get("/appointments/$crn?page=0&size=1000&startDate=$formattedStartDate&endDate=$formattedEndDate").willReturn(
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

  fun stubGetAppointmentsFromCRNNoResults(crn: String) {
    val appointmentsListJSON = """
      {
        "results": [],
        "totalElements": 0,
        "totalPages": 0,
        "page": 0,
        "size": 1000
      }
    """.trimIndent()
    val formattedStartDate = LocalDate.now().toString()
    val formattedEndDate = LocalDate.now().plusDays(365).toString()
    stubFor(
      get("/appointments/$crn?page=0&size=1000&startDate=$formattedStartDate&endDate=$formattedEndDate").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            appointmentsListJSON,
          )
          .withStatus(200),
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

  fun stubGetCrnFromNomsIdNotFound(nomsId: String) {
    stubFor(
      get("/probation-cases/$nomsId/crn").willReturn(
        aResponse()
          .withStatus(404),
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

  fun stubGetAllAppointmentsFromCRN(crn: String, status: Int) {
    val appointmentsListJSON = readFile("testdata/resettlement-passport-delius-api/appointments-list.json")
    val formattedStartDate = LocalDate.now().minusDays(365).toString()
    val formattedEndDate = LocalDate.now().plusDays(365).toString()
    stubFor(
      get("/appointments/$crn?page=0&size=1000&startDate=$formattedStartDate&endDate=$formattedEndDate").willReturn(
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

  fun stubGetPersonalDetailsFromCrn(crn: String, status: Int) {
    val personalDetailsJSON = readFile("testdata/resettlement-passport-delius-api/prisoner-personal-details.json")
    stubFor(
      get("/probation-cases/$crn").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              personalDetailsJSON,
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

  fun stubCreateAppointmentOK(crn: String) {
    stubFor(
      post("/appointments/$crn")
        .willReturn(
          aResponse().withStatus(201),

        ),
    )
  }

  fun stubGetAppointmentsFromCRNNoProbationAppointment(crn: String, status: Int) {
    var appointmentsListJSON = readFile("testdata/resettlement-passport-delius-api/appointments-list.json")
    appointmentsListJSON = appointmentsListJSON.replace("COAI Initial Appointment - In office (NS)", "Appointment with CRS Staff (NS)")
    val formattedStartDate = LocalDate.now().toString()
    val formattedEndDate = LocalDate.now().plusDays(365).toString()
    stubFor(
      get("/appointments/$crn?page=0&size=1000&startDate=$formattedStartDate&endDate=$formattedEndDate").willReturn(
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

  fun stubPostCaseNote(crn: String, type: String, prisonId: String, forename: String, surname: String, caseNoteText: String, fakeNow: String) {
    stubFor(
      post("/nomis-case-note/$crn")
        .withRequestBody(
          equalToJson(
            """
            {
              "type": "$type",
              "dateTime": "$fakeNow",
              "notes": "$caseNoteText",
              "author": {
                "prisonCode": "$prisonId",
                "forename": "$forename",
                "surname": "$surname"
              }
            }
            """.trimIndent(),
          ),
        ).willReturn(aResponse().withStatus(200)),
    )
  }

  fun stubPostCaseNoteError(crn: String, errorCode: Int) {
    stubFor(
      post("/nomis-case-note/$crn")
        .willReturn(aResponse().withStatus(errorCode)),
    )
  }
}
