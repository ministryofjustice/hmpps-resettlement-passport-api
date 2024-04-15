package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.AssertProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.test.json.JsonContentAssert
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusCreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusCreateAppointmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
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
      .codecs {
        it.defaultCodecs()
          .jackson2JsonEncoder(
            Jackson2JsonEncoder(
              jacksonObjectMapper()
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS),
            ),
          )
      }
      .build()
    val prisonerRepository: PrisonerRepository = mock()
    rpDeliusApiService = ResettlementPassportDeliusApiService(webClient, prisonerRepository)
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

    val appointment = DeliusCreateAppointment(
      type = DeliusCreateAppointmentType.Health,
      start = LocalDate.of(2024, 4, 15).atTime(14, 32).atZone(ZoneId.of("Europe/London")),
      duration = Duration.ofMinutes(30),
      notes = "notes",
    )
    rpDeliusApiService.createAppointment(crn, appointment)

    assertThat(mockWebServer.requestCount).isEqualTo(1)
    val request = mockWebServer.takeRequest()
    assertThat(request.path).isEqualTo("/appointments/CRN1")
    assertThat(request.headers).contains("Content-Type" to "application/json")

    assertThat(forJson(request.body.readUtf8())).isEqualToJson(
      """
      {
        "type": "Health",
        "start": "2024-04-15T14:32:00.000+0100",
        "duration": "PT30M",
        "notes": "notes",
      }
      """.trimIndent(),
    )
  }

  private fun forJson(json: String): AssertProvider<JsonContentAssert> {
    return AssertProvider<JsonContentAssert> {
      JsonContentAssert(
        ResettlementPassportDeliusApiServiceTest::class.java,
        json,
      )
    }
  }
}
