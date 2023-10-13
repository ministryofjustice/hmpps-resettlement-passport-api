package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import java.util.*

class PrisonApiMockServer : WireMockServerBase(9098) {
  companion object {
    const val TEST_IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII"
  }

  fun stubGetPrisonerImages(nomsId: String, status: Int) {
    val prisonerImagesListJSON = readFile("testdata/prison-api/prisoner-images-list.json")
    stubFor(
      get("/api/images/offenders/$nomsId").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              prisonerImagesListJSON,
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

  fun stubGetPrisonerFacialImage(imageId: String, status: Int) {
    stubFor(
      get("/api/images/$imageId/data").willReturn(
        if (status == 200) {
          aResponse()
            .withHeader("Content-Type", "image/jpeg")
            .withBody(
              Base64.getDecoder()
                .decode(PrisonApiMockServer.TEST_IMAGE_BASE64),
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
