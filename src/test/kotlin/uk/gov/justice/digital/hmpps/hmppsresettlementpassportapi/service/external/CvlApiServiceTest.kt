package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Conditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.AdditionalCondition
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.Licence
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.StandardCondition

class CvlApiServiceTest {

  private lateinit var cvlApiService: CvlApiService
  private lateinit var webClient: WebClient

  @BeforeEach
  fun setup() {
    webClient = mockk(relaxed = true)
    cvlApiService = CvlApiService(webClient)
  }

  @AfterEach
  fun reset() {
    unmockkAll()
  }

  @Test
  fun `test getLicenceConditionsByLicenceId`() {
    val licenceId = 123L
    val expectedLicenceConditions = createExpectedLicenceConditions()
    val mockedLicence = createMockLicence()
    every { webClient.get().uri(any<String>(), any<Map<String, Long>>()).retrieve().bodyToMono(any<ParameterizedTypeReference<*>>()) } returns Mono.just(mockedLicence)

    val result = cvlApiService.getLicenceConditionsByLicenceId(licenceId)

    assertNotNull(result)
    assertEquals(expectedLicenceConditions, result)
  }

  private fun createMockLicence(): Licence {
    val standardLicenceConditions = listOf(
      StandardCondition(1001, "111", 1, "Condition 1"),
      StandardCondition(1002, "222", 2, "Condition 2"),
    )
    val additionalLicenceConditions = listOf(
      AdditionalCondition(1007, "333", "1", "cat11", 3, "", "Additional Condition 1"),
      AdditionalCondition(1008, "444", "1", "cat13", 4, "", "Additional Condition 2"),
    )
    return Licence(
      123,
      "ACTIVE",
      "1",
      "123",
      "AB1234",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      "20/08/2023",
      "12/07/2023",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      standardLicenceConditions,
      emptyList(),
      additionalLicenceConditions,
      emptyList(),
      emptyList(),
      false,
      null,
      null,
    )
  }

  private fun createExpectedLicenceConditions(): LicenceConditions {
    val standardLicenceConditions = listOf(
      Conditions(1001, false, "Condition 1", 1),
      Conditions(1002, false, "Condition 2", 2),
    )
    val additionalLicenceConditions = listOf(
      Conditions(1007, false, "Additional Condition 1", 3),
      Conditions(1008, false, "Additional Condition 2", 4),
    )
    return LicenceConditions(
      123,
      "123",
      "20/08/2023",
      "12/07/2023",
      standardLicenceConditions,
      additionalLicenceConditions,
    )
  }
}
