package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.AllocationManagerApiService

class AllocationManagerApiServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var allocationManagerApiService: AllocationManagerApiService

  @BeforeEach
  override fun beforeEach() {
    cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
  }

  @Test
  fun `should get POM data`() {
    val nomsId = "123"
    val responseJson = readFile("testdata/allocation-manager-api/poms-1.json")

    allocationManagerApiMockServer.stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/allocation/$nomsId"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson),
        ),
    )

    val poms = allocationManagerApiService.getPomsByNomsId(nomsId)

    Assertions.assertEquals(poms.primaryPom, "David Jones")
    Assertions.assertEquals(poms.secondaryPom, "Barbara Winter")
  }

  @Test
  fun `should handle missing primary POM data`() {
    val nomsId = "123"
    val responseJson = readFile("testdata/allocation-manager-api/poms-2.json")

    allocationManagerApiMockServer.stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/allocation/$nomsId"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson),
        ),
    )

    val poms = allocationManagerApiService.getPomsByNomsId(nomsId)

    Assertions.assertNull(poms.primaryPom)
  }

  @Test
  fun `should handle missing secondary POM data`() {
    val nomsId = "123"
    val responseJson = readFile("testdata/allocation-manager-api/poms-2.json")

    allocationManagerApiMockServer.stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/allocation/$nomsId"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson),
        ),
    )

    val poms = allocationManagerApiService.getPomsByNomsId(nomsId)

    Assertions.assertNull(poms.secondaryPom)
  }
}
