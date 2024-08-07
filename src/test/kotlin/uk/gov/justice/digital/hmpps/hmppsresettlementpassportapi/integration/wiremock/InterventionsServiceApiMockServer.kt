package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock
import com.github.tomakehurst.wiremock.client.WireMock
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
class InterventionsServiceApiMockServer : WireMockServerBase() {
  fun stubGetCRSAppointmentsFromCRN(crn: String, status: Int) {
    val appointmentsListJSON = readFile("testdata/interventions-service-api/crs-appointments.json")
    stubFor(
      WireMock.get("/appointments-location/$crn").willReturn(
        if (status == 200) {
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              appointmentsListJSON,
            )
            .withStatus(status)
        } else {
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\"Error\" : \"$status\"}")
            .withStatus(status)
        },
      ),
    )
  }

  fun stubGetCRSAppointmentsFromCRNNoResults(crn: String, status: Int) {
    val appointmentsListJSON = "{\n" +
      "  \"crn\": \"U416100\",\n" +
      "  \"referral\": []" +
      " }"
    stubFor(
      WireMock.get("/appointments-location/$crn").willReturn(
        if (status == 200) {
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              appointmentsListJSON,
            )
            .withStatus(status)
        } else {
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\"Error\" : \"$status\"}")
            .withStatus(status)
        },
      ),
    )
  }

  fun stubGetCRSAppointmentsFromCRNNoReferrals(crn: String, status: Int) {
    val appointmentsListJSON = "{\n" +
      "  \"crn\": \"U416100\"" +
      " }"
    stubFor(
      WireMock.get("/appointments-location/$crn").willReturn(
        if (status == 200) {
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              appointmentsListJSON,
            )
            .withStatus(status)
        } else {
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\"Error\" : \"$status\"}")
            .withStatus(status)
        },
      ),
    )
  }
}
