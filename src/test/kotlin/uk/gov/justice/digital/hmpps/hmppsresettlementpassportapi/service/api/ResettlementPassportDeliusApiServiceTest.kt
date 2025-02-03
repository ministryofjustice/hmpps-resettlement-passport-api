package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.AssertProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.json.JsonContentAssert
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusCreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusCreateAppointmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.jacksonCodecsConfigurer
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class ResettlementPassportDeliusApiServiceTest {

  private val mockWebServer: MockWebServer = MockWebServer()
  private lateinit var rpDeliusApiService: ResettlementPassportDeliusApiService

  @BeforeEach
  fun beforeEach() {
    mockWebServer.start()
    val webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toUrl().toString())
      .codecs(jacksonCodecsConfigurer)
      .build()
    rpDeliusApiService = ResettlementPassportDeliusApiService(webClient)
  }

  @AfterEach
  fun afterEach() {
    mockWebServer.shutdown()
  }

  @Test
  fun `test get CRN happy path full json`() {
    val nomsId = "ABC1234"
    val expectedCrn = "D345678"

    val mockedJsonResponse = readFile("testdata/resettlement-passport-delius-api/prisoner-details-valid-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))

    Assertions.assertEquals(expectedCrn, rpDeliusApiService.getCrn(nomsId))
  }

  @Test
  fun `test get CRN happy path min json`() {
    val nomsId = "ABC1234"
    val expectedCrn = "D345678"

    val mockedJsonResponse = readFile("testdata/resettlement-passport-delius-api/prisoner-details-valid-2.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))

    Assertions.assertEquals(expectedCrn, rpDeliusApiService.getCrn(nomsId))
  }

  @Test
  fun `test get CRN not found upstream`() {
    val nomsId = "ABC1234"

    mockWebServer.enqueue(
      MockResponse().setBody("{}").addHeader("Content-Type", "application/json").setResponseCode(404),
    )
    assertNull(rpDeliusApiService.getCrn(nomsId))
  }

  @Test
  fun `test get CRN 500 error upstream`() {
    val nomsId = "ABC1234"

    mockWebServer.enqueue(
      MockResponse().setBody("{}").addHeader("Content-Type", "application/json").setResponseCode(500),
    )
    assertNull(rpDeliusApiService.getCrn(nomsId))
  }

  @Test
  fun `test fetch appointments  happy path full json`() {
    val nomsId = "ABC1234"
    val crn = "CRN1"
    val expectedType = "Appointment with CRS Staff (NS)"

    val mockedJsonResponse = readFile("testdata/resettlement-passport-delius-api/appointments-list.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))

    val appointmentList =
      rpDeliusApiService.fetchAppointments(nomsId, crn, LocalDate.now().minusDays(1), LocalDate.now().plusDays(365))
    Assertions.assertEquals(expectedType, appointmentList[0].type.description)
  }

  @Test
  fun `test fetch accommodation  happy path full json`() {
    val nomsId = "ABC1234"
    val crn = "CRN1"
    val expectedBuildingName = "New Court"
    val expectedReferralDate = LocalDate.parse("2023-08-24")
    val expectedStartDateTime = ZonedDateTime.parse("2022-09-18T13:46:00Z")

    val mockedJsonResponse = readFile("testdata/resettlement-passport-delius-api/duty-to-refer-nsi-abode-true.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))

    val accommodation = rpDeliusApiService.fetchAccommodation(nomsId, crn)
    Assertions.assertEquals(expectedReferralDate, accommodation.referralDate)
    Assertions.assertEquals(expectedStartDateTime, accommodation.startDateTime)
    Assertions.assertEquals(expectedBuildingName, accommodation.mainAddress?.buildingName)
  }

  @Test
  fun `create appointment`() {
    val crn = "CRN1"

    mockWebServer.enqueue(response = MockResponse().setResponseCode(201))

    val appointment = aCreateAppointmentRequest()
    rpDeliusApiService.createAppointment(crn, appointment)

    assertThat(mockWebServer.requestCount).isEqualTo(1)
    val request = mockWebServer.takeRequest()
    assertThat(request.path).isEqualTo("/appointments/CRN1")
    assertThat(request.headers).contains("Content-Type" to "application/json")

    assertThat(forJson(request.body.readUtf8())).isEqualToJson(
      """
      {
        "type": "Health",
        "start": "2024-04-15T14:32:00.000+01",
        "duration": "PT30M",
        "notes": "notes",
      }
      """.trimIndent(),
    )
  }

  @Test
  fun `Create appointment bad request`() {
    val crn = "CRN1"

    mockWebServer.enqueue(response = MockResponse().setResponseCode(400))

    val appointment = aCreateAppointmentRequest()
    assertThatThrownBy { rpDeliusApiService.createAppointment(crn, appointment) }
      .isInstanceOf(WebClientResponseException.BadRequest::class.java)
  }

  private fun aCreateAppointmentRequest() = DeliusCreateAppointment(
    type = DeliusCreateAppointmentType.Health,
    start = LocalDate.of(2024, 4, 15).atTime(14, 32).atZone(ZoneId.of("Europe/London")),
    duration = Duration.ofMinutes(30),
    notes = "notes",
  )

  private fun forJson(json: String): AssertProvider<JsonContentAssert> = AssertProvider<JsonContentAssert> {
    JsonContentAssert(
      ResettlementPassportDeliusApiServiceTest::class.java,
      json,
    )
  }
}
