package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.EducationEmploymentApiService

class EducationEmploymentApiServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var educationEmploymentApiService: EducationEmploymentApiService

  @BeforeEach
  override fun beforeEach() {
    cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
  }

  @Test
  fun `should get readiness profile by nomsId`() {
    val nomsId = "123"
    val responseJson = readFile("testdata/education-employment-api/readiness-profile.json")

    educationEmploymentApiMockServer.stubFor(
      WireMock.get(WireMock.urlEqualTo("/readiness-profiles/$nomsId"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson),
        ),
    )

    val readinessProfile = educationEmploymentApiService.getReadinessProfileByNomsId(nomsId)

    Assertions.assertNotNull(readinessProfile)
  }
}
