package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import java.util.Base64

class CvlApiMockServer : WireMockServerBase() {
  companion object {
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
  fun stubFindLicencesByNomsId(nomsId: String, status: Int = 200, responseBody: String = readFile("testdata/cvl-api/licence-summary.json")) {
    val requestJson = "{ \"nomsId\" :  [\"$nomsId\"] }"
    stubFor(
      post("/licence/match").withRequestBody(
        equalToJson(requestJson, true, true),
      )
        .willReturn(
          if (status == 200) {
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(
                responseBody,
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

  fun stubFetchLicenceConditionsByLicenceId(licenceId: Int, status: Int, licenceJSON: String = readFile("testdata/cvl-api/licence.json")) {
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

  fun stubFindNoLicencesByNomsId(nomsId: String, status: Int) {
    var licenceSummaryJSON = " [] "
    if (status == 404) {
      licenceSummaryJSON = " [] "
    }
    val requestJson = "{ \"nomsId\" :  [\"$nomsId\"] }"
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
}
