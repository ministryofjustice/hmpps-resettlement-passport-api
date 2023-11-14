package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.ZonedDateTime

class ResettlementPassportDeliusApiServiceTest {

  private val mockWebServer: MockWebServer = MockWebServer()
  private lateinit var rpDeliusApiService: ResettlementPassportDeliusApiService

  @BeforeEach
  fun beforeEach() {
    mockWebServer.start()
    val webClient = WebClient.create(mockWebServer.url("/").toUrl().toString())
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

    val mockedJsonResponse = readFile("testdata/resettlement-passport-delius-api/offender-details-valid-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))

    Assertions.assertEquals(expectedCrn, rpDeliusApiService.getCrn(nomsId))
  }

  @Test
  fun `test get CRN happy path min json`() {
    val nomsId = "ABC1234"
    val expectedCrn = "D345678"

    val mockedJsonResponse = readFile("testdata/resettlement-passport-delius-api/offender-details-valid-2.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))

    Assertions.assertEquals(expectedCrn, rpDeliusApiService.getCrn(nomsId))
  }

  @Test
  fun `test get CRN not found upstream`() {
    val nomsId = "ABC1234"

    mockWebServer.enqueue(MockResponse().setBody("{}").addHeader("Content-Type", "application/json").setResponseCode(404))
    assertNull(rpDeliusApiService.getCrn(nomsId))
  }

  @Test
  fun `test get CRN 500 error upstream`() {
    val nomsId = "ABC1234"

    mockWebServer.enqueue(MockResponse().setBody("{}").addHeader("Content-Type", "application/json").setResponseCode(500))
    assertNull(rpDeliusApiService.getCrn(nomsId))
  }

  @Test
  fun `test fetch appointments  happy path full json`() {
    val nomsId = "ABC1234"
    val crn = "CRN1"
    val expectedType = "Appointment with CRS Staff (NS)"

    val mockedJsonResponse = readFile("testdata/resettlement-passport-delius-api/appointments-list.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))

    val appointmentList = rpDeliusApiService.fetchAppointments(nomsId, crn, LocalDate.now().minusDays(1), LocalDate.now().plusDays(365), 0, 10)
    Assertions.assertEquals(expectedType, appointmentList.results[0].type.description)
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
}
