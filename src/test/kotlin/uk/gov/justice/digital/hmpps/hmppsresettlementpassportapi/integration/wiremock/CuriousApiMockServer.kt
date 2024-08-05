package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class CuriousApiMockServer : WireMockServerBase() {
  fun stubGetLearnerEducationListByNomsId(nomsId: String, status: Int) {
    val getLearnersCourseList = readFile("testdata/curious-api/learners-education-list.json")
    stubFor(
      get("/sequation-virtual-campus2-api/learnerEducation/$nomsId?size=1&page=0").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              getLearnersCourseList,
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

  fun stubGetLearnerEducationEmptyList(status: Int) {
    stubFor(
      get("/sequation-virtual-campus2-api/learnerEducation/A8731DY").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              " [] ",
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
