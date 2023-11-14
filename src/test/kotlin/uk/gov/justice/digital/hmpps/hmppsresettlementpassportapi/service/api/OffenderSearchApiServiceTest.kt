package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prison
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoners
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PathwayAndStatusService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ExtendWith(MockitoExtension::class)
class OffenderSearchApiServiceTest {

  private val mockWebServer: MockWebServer = MockWebServer()
  private lateinit var offenderSearchApiService: OffenderSearchApiService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Mock
  private lateinit var prisonRegisterApiService: PrisonRegisterApiService

  @Mock
  private lateinit var prisonApiService: PrisonApiService

  @BeforeEach
  fun beforeEach() {
    mockWebServer.start()
    val webClient = WebClient.create(mockWebServer.url("/").toUrl().toString())
    offenderSearchApiService = OffenderSearchApiService(
      prisonerRepository,
      webClient,
      pathwayAndStatusService,
      prisonRegisterApiService,
      prisonApiService,
    )
  }

  @AfterEach
  fun afterEach() {
    mockWebServer.shutdown()
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending - 1`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPrisonerId = "G1458GV"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 10, "releaseDate,DESC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending - 2`() {
    mockDatabaseCalls()

    val prisonId = "MDI"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-2.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisoners = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 10, "releaseDate,DESC")

    Assertions.assertEquals(getExpectedPrisonersListReleaseDateDesc(), prisoners)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Ascending`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPrisonerId = "A8339DY"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 10, "releaseDate,ASC")
    Assertions.assertEquals(
      expectedPrisonerId,
      prisonersList.content?.get((prisonersList.content!!.toList().size - 1))?.prisonerNumber
        ?: 0,
    )
  }

  @Test
  fun `test get PrisonersList happy path full json with sort firstName Ascending`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPrisonerId = "G1458GV"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 10, "firstName,ASC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json for page size`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPageSize = 3

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 5, "firstName,ASC")
    Assertions.assertEquals(expectedPageSize, prisonersList.pageSize)
    prisonersList.content?.toList()?.let { Assertions.assertEquals(expectedPageSize, it.size) }
  }

  private fun mockDatabaseCalls() {
    val mockPrisonerEntity = PrisonerEntity(1, "TEST", LocalDateTime.now(), "test", "xyz", LocalDate.parse("2025-01-23"))
    val mockPathwayEntity1 = PathwayEntity(1, "Accommodation", true, LocalDateTime.now())
    val mockPathwayEntity2 = PathwayEntity(2, "Attitudes, thinking and behaviour", true, LocalDateTime.now())
    val mockPathwayEntity3 = PathwayEntity(3, "Children, families and communities", false, LocalDateTime.now())

    val mockStatusEntity = StatusEntity(1, "Not Started", true, LocalDateTime.now())

    `when`(prisonerRepository.findByNomsId(any())).thenReturn(mockPrisonerEntity)
    `when`(pathwayAndStatusService.findAllPathways()).thenReturn(listOf(mockPathwayEntity1, mockPathwayEntity2, mockPathwayEntity3))

    `when`(pathwayAndStatusService.findPathwayStatusFromPathwayAndPrisoner(any(), any())).thenReturn(
      PathwayStatusEntity(1, mockPrisonerEntity, mockPathwayEntity1, mockStatusEntity, LocalDateTime.now()),
    )
  }

  @Test
  fun `test get PrisonersList happy path full json with release date filter`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPrisonerId = "G1458GV"
    val days = 84
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val releaseDate = LocalDate.now().minusDays(days.toLong())
    var mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockedJsonResponse = mockedJsonResponse.replace("\"releaseDate\": \"2024-07-31\",", "\"releaseDate\": \"" + releaseDate.format(pattern) + "\",")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 10, "firstName,ASC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }
  private fun getExpectedPrisonersListReleaseDateDesc() = PrisonersList(
    content =
    listOf(
      Prisoners(
        prisonerNumber = "A8229DY",
        firstName = "STEPHEN",
        middleNames = null,
        lastName = "MCVEIGH",
        releaseDate = LocalDate.parse("2099-08-01"),
        releaseType = "CRD",
        lastUpdatedDate = null,
        status = listOf(
          PathwayStatus(
            pathway = Pathway.ACCOMMODATION,
            status = Status.NOT_STARTED,
            lastDateChange = LocalDate.now(),
          ),
          PathwayStatus(
            pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
            status = Status.NOT_STARTED,
            lastDateChange = LocalDate.now(),
          ),
        ),
        homeDetentionCurfewEligibilityDate = LocalDate.parse("2021-02-03"),
        paroleEligibilityDate = null,
      ),
      Prisoners(
        prisonerNumber = "G1458GV",
        firstName = "FINN",
        middleNames = "CHANDLEVIEVE",
        lastName = "CRAWFIS",
        releaseDate = LocalDate.parse("2098-09-12"),
        releaseType = "CRD",
        lastUpdatedDate = null,
        status = listOf(
          PathwayStatus(
            pathway = Pathway.ACCOMMODATION,
            status = Status.NOT_STARTED,
            lastDateChange = LocalDate.now(),
          ),
          PathwayStatus(
            pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
            status = Status.NOT_STARTED,
            lastDateChange = LocalDate.now(),
          ),
        ),
        homeDetentionCurfewEligibilityDate = LocalDate.parse("2018-10-16"),
        paroleEligibilityDate = null,
      ),
      Prisoners(
        prisonerNumber = "A8339DY",
        firstName = "MR",
        middleNames = "BRIDGILLA",
        lastName = "CRD-LR-TEST",
        releaseDate = null,
        releaseType = "PRRD",
        lastUpdatedDate = null,
        status = listOf(
          PathwayStatus(
            pathway = Pathway.ACCOMMODATION,
            status = Status.NOT_STARTED,
            lastDateChange = LocalDate.now(),
          ),
          PathwayStatus(
            pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
            status = Status.NOT_STARTED,
            lastDateChange = LocalDate.now(),
          ),
        ),
        homeDetentionCurfewEligibilityDate = null,
        paroleEligibilityDate = null,
      ),
    ),
    pageSize = 3,
    page = 0,
    sortName = "releaseDate,DESC",
    totalElements = 3,
    last = true,
  )

  @Test
  fun `test check prisoner is in active prison - happy path`() {
    `when`(prisonRegisterApiService.getActivePrisonsList()).thenReturn(
      mutableListOf(
        Prison("ABC", "Test prison ABC", true),
        Prison("DEF", "Test prison DEF", true),
        Prison("GHI", "Test prison GHI", true),
      ),
    )
    assertDoesNotThrow {
      offenderSearchApiService.checkPrisonerIsInActivePrison(createPrisoner("ABC"))
      offenderSearchApiService.checkPrisonerIsInActivePrison(createPrisoner("DEF"))
      offenderSearchApiService.checkPrisonerIsInActivePrison(createPrisoner("GHI"))
    }
  }

  @Test
  fun `test check prisoner is in active prison - not found`() {
    `when`(prisonRegisterApiService.getActivePrisonsList()).thenReturn(mutableListOf())
    assertThrows<ResourceNotFoundException> {
      offenderSearchApiService.checkPrisonerIsInActivePrison(createPrisoner("ABC"))
      offenderSearchApiService.checkPrisonerIsInActivePrison(createPrisoner("DEF"))
      offenderSearchApiService.checkPrisonerIsInActivePrison(createPrisoner("GHI"))
    }
  }

  private fun createPrisoner(prisonId: String) = PrisonersSearch(prisonerNumber = "A123456", firstName = "firstName", lastName = "lastName", prisonId = prisonId, prisonName = "prisonName", cellLocation = null, youthOffender = false)
}
