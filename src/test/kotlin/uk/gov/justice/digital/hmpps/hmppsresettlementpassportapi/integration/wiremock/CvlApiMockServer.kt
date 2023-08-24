package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.google.common.io.Resources
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import java.io.File
import java.util.Base64

class CvlApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8095
    const val TEST_IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII"
  }

  fun stubGetImageFromLicenceIdAndConditionId(licenceId: String, conditionId: String, status: Int) {
    stubFor(
      get("/exclusion-zone/id/$licenceId/condition/id/$conditionId/full-size-image").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/jpeg")
            .withBody(
              Base64.getDecoder()
                .decode(TEST_IMAGE_BASE64),
            )
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
  fun stubFindLicencesByNomisId(nomisId: String, status: Int) {
    var licenceSummaryJSON = readFile("testdata/cvl-api/licence-summary.json")
    if (status == 404) {
      licenceSummaryJSON = " [] "
    }
    val requestJson = "{ \"nomsId\" :  [\"$nomisId\"] }"
    stubFor(
      post("/licence/match").withRequestBody(
        equalToJson(requestJson, true, true),
      )
        .willReturn(
          if (status == 200 || status == 404) {
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(
                licenceSummaryJSON,
              )
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

  fun stubFetchLicenceConditionsByLicenceId(licenceId: Int, status: Int) {
    val licenceJSON = readFile("testdata/cvl-api/licence.json")
    licenceJSON.replace("Active", "InActive")
    stubFor(
      get("/licence/id/$licenceId").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              licenceJSON,
            )
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
