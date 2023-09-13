package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.web.reactive.function.client.WebClient
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AllocationManagerApiServiceTest {

  private lateinit var allocationManagerApiService: AllocationManagerApiService

  @BeforeEach
  fun beforeEach() {
    val webClientMock = Mockito.mock(WebClient::class.java)
    allocationManagerApiService = AllocationManagerApiService(webClientMock)
  }

  @ParameterizedTest
  @MethodSource("test convert name data")
  fun `test convert name`(inputString: String, expectedString: String) {
    Assertions.assertEquals(expectedString, allocationManagerApiService.convertName(inputString))
  }

  private fun `test convert name data`(): Stream<Arguments> = Stream.of(
    Arguments.of("", ""),
    Arguments.of("QAZI, ASFAND", "Asfand Qazi"),
    Arguments.of("   QAZI, ASFAND   ", "Asfand Qazi"),
    Arguments.of("Asfand Qazi", "Asfand Qazi"),
  )
}
