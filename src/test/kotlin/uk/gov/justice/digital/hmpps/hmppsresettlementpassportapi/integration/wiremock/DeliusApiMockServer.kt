package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import org.hibernate.query.sqm.tree.SqmNode.log
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import java.time.LocalDate

class DeliusApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8102
  }

  fun stubGetAppointmentsFromCRN(crn: String, status: Int) {
    val appointmentsListJSON = readFile("testdata/delius-api/appointments-list.json")
    val formattedStartDate = LocalDate.now().minusDays(365).toString()
    val formattedEndDate = LocalDate.now().plusDays(365).toString()
    log.fatal("Start Date $formattedStartDate and End Date $formattedEndDate")
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
}
