package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CvlApiService

class CvlApiServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var cvlApiService: CvlApiService

  @BeforeEach
  override fun beforeEach() {
    cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
  }

  @Test
  fun `should get licences by noms id`() {
    val nomsId = listOf("123", "abc")
    val responseJson = readFile("testdata/cvl-api/licence-summary.json")

    cvlApiMockServer.stubFor(
      WireMock.post(WireMock.urlEqualTo("/licence/match"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson),
        ),
    )

    val licences = cvlApiService.findLicencesByNomsId(nomsId)

    Assertions.assertNotNull(licences)
    Assertions.assertEquals(7, licences.size)
    Assertions.assertTrue(licences.all { it.licenceStatus.isNotBlank() })
  }

  @Test
  fun `should get licence conditions by licence id`() {
    val licenceId = 572L
    val responseJson = readFile("testdata/cvl-api/licence.json")

    cvlApiMockServer.stubFor(
      WireMock.get(WireMock.urlEqualTo("/licence/id/$licenceId"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson),
        ),
    )

    val licenceConditions = cvlApiService.getLicenceConditionsByLicenceId(licenceId)

    Assertions.assertNotNull(licenceConditions)
    Assertions.assertNotNull(licenceConditions.standardLicenceConditions)
    Assertions.assertNotNull(licenceConditions.otherLicenseConditions)

    Assertions.assertEquals("ACTIVE", licenceConditions.status)
  }

  @Test
  fun `should get image by licence id and conditions id`() {
    val licenceId = "123"
    val conditionId = "456"
    val responseJson = readFile("testdata/cvl-api/licence.json")

    cvlApiMockServer.stubFor(
      WireMock.get(WireMock.urlEqualTo("/exclusion-zone/id/$licenceId/condition/id/$conditionId/full-size-image"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "image/jpeg")
            .withBody(responseJson),
        ),
    )

    val image = cvlApiService.getImageFromLicenceIdAndConditionId(licenceId, conditionId)

    Assertions.assertNotNull(image)
  }

  @Test
  fun `should throw ResourceNotFoundException when image not found`() {
    val licenceId = "123"
    val conditionId = "456"

    cvlApiMockServer.stubFor(
      WireMock.get(WireMock.urlEqualTo("/exclusion-zone/id/$licenceId/condition/id/$conditionId/full-size-image"))
        .willReturn(
          WireMock.aResponse()
            .withStatus(404), // Simulate image not found
        ),
    )

    Assertions.assertThrows(ResourceNotFoundException::class.java) {
      cvlApiService.getImageFromLicenceIdAndConditionId(licenceId, conditionId)
    }
  }
}
