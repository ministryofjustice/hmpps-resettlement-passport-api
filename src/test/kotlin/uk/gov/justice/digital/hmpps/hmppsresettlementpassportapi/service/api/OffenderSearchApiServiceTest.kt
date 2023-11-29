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
import java.util.*

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
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, null, null, 0, 10, "releaseDate,DESC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending - 2`() {
    mockDatabaseCalls()

    val prisonId = "MDI"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-2.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisoners = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, null, null, 0, 10, "releaseDate,DESC")

    Assertions.assertEquals(getExpectedPrisonersListReleaseDateDesc(), prisoners)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Ascending`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPrisonerId = "A8339DY"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, null, null, 0, 10, "releaseDate,ASC")
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
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, null, null, 0, 10, "firstName,ASC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json for page size`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPageSize = 3

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, null, null, 0, 5, "firstName,ASC")
    Assertions.assertEquals(expectedPageSize, prisonersList.pageSize)
    prisonersList.content?.toList()?.let { Assertions.assertEquals(expectedPageSize, it.size) }
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
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, null, null, 0, 10, "firstName,ASC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json for pathwayView`() {
    mockDatabaseCallsForPathwayView()

    val prisonId = "MDI"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-2.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisoners = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, Pathway.ACCOMMODATION, null, 0, 10, "releaseDate,DESC")

    Assertions.assertEquals(getExpectedPrisonersPathwayView(), prisoners)
  }

  @Test
  fun `test get PrisonersList happy path full json for pathwayView and pathwayStatus`() {
    mockDatabaseCallsForPathwayView()

    val prisonId = "MDI"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-2.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisoners = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, Pathway.ACCOMMODATION, Status.SUPPORT_DECLINED, 0, 10, "releaseDate,DESC")

    Assertions.assertEquals(getExpectedPrisonersPathwayViewAndPathwayStatus(), prisoners)
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

  private fun mockDatabaseCallsForPathwayView() {
    val mockPrisonerEntity1 = PrisonerEntity(1, "A8229DY", LocalDateTime.now(), "1", "xyz", LocalDate.parse("2025-01-23"))
    val mockPrisonerEntity2 = PrisonerEntity(2, "G1458GV", LocalDateTime.now(), "2", "xyz", LocalDate.parse("2025-01-23"))
    val mockPrisonerEntity3 = PrisonerEntity(3, "A8339DY", LocalDateTime.now(), "3", "xyz", LocalDate.parse("2025-01-23"))

    val mockPathwayEntity1 = PathwayEntity(1, "Accommodation", true, LocalDateTime.now())

    val mockStatusEntity1 = StatusEntity(1, "Not Started", true, LocalDateTime.now())
    val mockStatusEntity4 = StatusEntity(4, "Support declined", true, LocalDateTime.now())
    val mockStatusEntity5 = StatusEntity(5, "Done", true, LocalDateTime.now())

    `when`(pathwayAndStatusService.getPathwayEntity(Pathway.ACCOMMODATION)).thenReturn(mockPathwayEntity1)

    `when`(prisonerRepository.findByNomsId("A8229DY")).thenReturn(mockPrisonerEntity1)
    `when`(prisonerRepository.findByNomsId("G1458GV")).thenReturn(mockPrisonerEntity2)
    `when`(prisonerRepository.findByNomsId("A8339DY")).thenReturn(mockPrisonerEntity3)

    `when`(pathwayAndStatusService.findPathwayStatusFromPathwayAndPrisoner(mockPathwayEntity1, mockPrisonerEntity1)).thenReturn(
      PathwayStatusEntity(1, mockPrisonerEntity1, mockPathwayEntity1, mockStatusEntity1, LocalDateTime.now()),
    )
    `when`(pathwayAndStatusService.findPathwayStatusFromPathwayAndPrisoner(mockPathwayEntity1, mockPrisonerEntity2)).thenReturn(
      PathwayStatusEntity(8, mockPrisonerEntity2, mockPathwayEntity1, mockStatusEntity4, LocalDateTime.now()),
    )
    `when`(pathwayAndStatusService.findPathwayStatusFromPathwayAndPrisoner(mockPathwayEntity1, mockPrisonerEntity3)).thenReturn(
      PathwayStatusEntity(15, mockPrisonerEntity3, mockPathwayEntity1, mockStatusEntity5, LocalDateTime.now()),
    )
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
        pathwayStatus = null,
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
        pathwayStatus = null,
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
        pathwayStatus = null,
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

  private fun getExpectedPrisonersPathwayView() = PrisonersList(
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
        status = null,
        pathwayStatus = Status.NOT_STARTED,
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
        status = null,
        pathwayStatus = Status.SUPPORT_DECLINED,
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
        status = null,
        pathwayStatus = Status.DONE,
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

  private fun getExpectedPrisonersPathwayViewAndPathwayStatus() = PrisonersList(
    content =
    listOf(
      Prisoners(
        prisonerNumber = "G1458GV",
        firstName = "FINN",
        middleNames = "CHANDLEVIEVE",
        lastName = "CRAWFIS",
        releaseDate = LocalDate.parse("2098-09-12"),
        releaseType = "CRD",
        lastUpdatedDate = null,
        status = null,
        pathwayStatus = Status.SUPPORT_DECLINED,
        homeDetentionCurfewEligibilityDate = LocalDate.parse("2018-10-16"),
        paroleEligibilityDate = null,
      ),
    ),
    pageSize = 1,
    page = 0,
    sortName = "releaseDate,DESC",
    totalElements = 1,
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

  @Test
  fun `test sort prisoners- sort prisoners by name ascending`() {
    val prisoners = mutableListOf(
      createPrisonerName("BERTRAND", "ANDERSON"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ANDY", "ANDERSON"),
      createPrisonerName("CHARLIE", "FOSTER"),
      createPrisonerName("ARRAN", "PETERSON"),
      createPrisonerName("LOUIS", "MCCARTHY"),
      createPrisonerName("CORMAC", "CRAY"),
      createPrisonerName("VLODIMIR", "MARSH"),
      createPrisonerName("TOM", "WILLIAMSON"),
    )
    val sortedPrisoners = mutableListOf(
      createPrisonerName("ANDY", "ANDERSON"),
      createPrisonerName("BERTRAND", "ANDERSON"),
      createPrisonerName("CORMAC", "CRAY"),
      createPrisonerName("CHARLIE", "FOSTER"),
      createPrisonerName("VLODIMIR", "MARSH"),
      createPrisonerName("LOUIS", "MCCARTHY"),
      createPrisonerName("ARRAN", "PETERSON"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("TOM", "WILLIAMSON"),
    )
    offenderSearchApiService.sortPrisoners("name,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `test sort prisoners- sort prisoners by name descending`() {
    val prisoners = mutableListOf(
      createPrisonerName("BERTRAND", "ANDERSON"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ANDY", "ANDERSON"),
      createPrisonerName("CHARLIE", "FOSTER"),
      createPrisonerName("ARRAN", "PETERSON"),
      createPrisonerName("LOUIS", "MCCARTHY"),
      createPrisonerName("CORMAC", "CRAY"),
      createPrisonerName("VLODIMIR", "MARSH"),
      createPrisonerName("TOM", "WILLIAMSON"),
    )
    val sortedPrisoners = mutableListOf(
      createPrisonerName("ANDY", "ANDERSON"),
      createPrisonerName("BERTRAND", "ANDERSON"),
      createPrisonerName("CORMAC", "CRAY"),
      createPrisonerName("CHARLIE", "FOSTER"),
      createPrisonerName("VLODIMIR", "MARSH"),
      createPrisonerName("LOUIS", "MCCARTHY"),
      createPrisonerName("ARRAN", "PETERSON"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("TOM", "WILLIAMSON"),
    )
    offenderSearchApiService.sortPrisoners("name,DESC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by parole eligibility date- ascending`() {
    val prisoners = mutableListOf(
      createPrisonerParoleEligibilityDate(LocalDate.parse("2029-08-30")),
      createPrisonerParoleEligibilityDate(null),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2037-01-01")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2028-11-11")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2024-12-08")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-07-21")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2078-04-03")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2046-12-03")),
      createPrisonerParoleEligibilityDate(null),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-02-01")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-07-24")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2024-12-09")),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerParoleEligibilityDate(LocalDate.parse("2024-12-08")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2024-12-09")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-02-01")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-07-21")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-07-24")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2028-11-11")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2029-08-30")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2037-01-01")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2046-12-03")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2078-04-03")),
      createPrisonerParoleEligibilityDate(null),
      createPrisonerParoleEligibilityDate(null),
    )
    offenderSearchApiService.sortPrisoners("paroleEligibilityDate,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by parole eligibility date- descending`() {
    val prisoners = mutableListOf(
      createPrisonerParoleEligibilityDate(LocalDate.parse("2029-08-30")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2037-01-01")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2028-11-11")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2024-12-08")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-07-21")),
      createPrisonerParoleEligibilityDate(null),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2078-04-03")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2046-12-03")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-02-01")),
      createPrisonerParoleEligibilityDate(null),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-07-24")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2024-12-09")),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerParoleEligibilityDate(null),
      createPrisonerParoleEligibilityDate(null),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2078-04-03")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2046-12-03")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2037-01-01")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2029-08-30")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2028-11-11")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-07-24")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-07-21")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2026-02-01")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2024-12-09")),
      createPrisonerParoleEligibilityDate(LocalDate.parse("2024-12-08")),
    )
    offenderSearchApiService.sortPrisoners("paroleEligibilityDate,DESC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by pathway status- pathway view set- ascending`() {
    val prisoners = mutableListOf(
      createPrisonerPathwayStatus(Status.SUPPORT_DECLINED),
      createPrisonerPathwayStatus(Status.NOT_STARTED),
      createPrisonerPathwayStatus(Status.IN_PROGRESS),
      createPrisonerPathwayStatus(Status.DONE),
      createPrisonerPathwayStatus(Status.SUPPORT_NOT_REQUIRED),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerPathwayStatus(Status.NOT_STARTED),
      createPrisonerPathwayStatus(Status.IN_PROGRESS),
      createPrisonerPathwayStatus(Status.SUPPORT_NOT_REQUIRED),
      createPrisonerPathwayStatus(Status.SUPPORT_DECLINED),
      createPrisonerPathwayStatus(Status.DONE),
    )
    offenderSearchApiService.sortPrisoners("pathwayStatus,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by pathway status- pathway view set- descending`() {
    val prisoners = mutableListOf(
      createPrisonerPathwayStatus(Status.SUPPORT_DECLINED),
      createPrisonerPathwayStatus(Status.NOT_STARTED),
      createPrisonerPathwayStatus(Status.IN_PROGRESS),
      createPrisonerPathwayStatus(Status.DONE),
      createPrisonerPathwayStatus(Status.SUPPORT_NOT_REQUIRED),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerPathwayStatus(Status.DONE),
      createPrisonerPathwayStatus(Status.SUPPORT_DECLINED),
      createPrisonerPathwayStatus(Status.SUPPORT_NOT_REQUIRED),
      createPrisonerPathwayStatus(Status.IN_PROGRESS),
      createPrisonerPathwayStatus(Status.NOT_STARTED),
    )
    offenderSearchApiService.sortPrisoners("pathwayStatus,DESC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by last updated- descending`() {
    val prisoners = mutableListOf(
      createPrisonerLastUpdatedDate(Status.IN_PROGRESS, LocalDate.parse("2023-05-08")),
      createPrisonerLastUpdatedDate(Status.DONE, LocalDate.parse("2023-11-08")),
      createPrisonerLastUpdatedDate(Status.DONE, LocalDate.parse("2023-10-08")),
      createPrisonerLastUpdatedDate(Status.NOT_STARTED, null),
      createPrisonerLastUpdatedDate(Status.SUPPORT_DECLINED, LocalDate.parse("2023-10-31")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_NOT_REQUIRED, LocalDate.parse("2023-09-30")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_DECLINED, LocalDate.parse("2023-09-30")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_NOT_REQUIRED, LocalDate.parse("2023-08-03")),
      createPrisonerLastUpdatedDate(Status.IN_PROGRESS, LocalDate.parse("2023-11-22")),
    )

    val sortedPrisoners = mutableListOf(

      createPrisonerLastUpdatedDate(Status.NOT_STARTED, null),
      createPrisonerLastUpdatedDate(Status.IN_PROGRESS, LocalDate.parse("2023-11-22")),
      createPrisonerLastUpdatedDate(Status.DONE, LocalDate.parse("2023-11-08")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_DECLINED, LocalDate.parse("2023-10-31")),
      createPrisonerLastUpdatedDate(Status.DONE, LocalDate.parse("2023-10-08")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_NOT_REQUIRED, LocalDate.parse("2023-09-30")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_DECLINED, LocalDate.parse("2023-09-30")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_NOT_REQUIRED, LocalDate.parse("2023-08-03")),
      createPrisonerLastUpdatedDate(Status.IN_PROGRESS, LocalDate.parse("2023-05-08")),
    )
    offenderSearchApiService.sortPrisoners("lastUpdatedDate,DESC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by last updated- ascending`() {
    val prisoners = mutableListOf(
      createPrisonerLastUpdatedDate(Status.IN_PROGRESS, LocalDate.parse("2023-05-08")),
      createPrisonerLastUpdatedDate(Status.DONE, LocalDate.parse("2023-11-08")),
      createPrisonerLastUpdatedDate(Status.DONE, LocalDate.parse("2023-10-08")),
      createPrisonerLastUpdatedDate(Status.NOT_STARTED, null),
      createPrisonerLastUpdatedDate(Status.NOT_STARTED, null),
      createPrisonerLastUpdatedDate(Status.SUPPORT_DECLINED, LocalDate.parse("2023-10-31")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_NOT_REQUIRED, LocalDate.parse("2023-09-30")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_NOT_REQUIRED, LocalDate.parse("2023-08-03")),
      createPrisonerLastUpdatedDate(Status.IN_PROGRESS, LocalDate.parse("2023-11-22")),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerLastUpdatedDate(Status.IN_PROGRESS, LocalDate.parse("2023-05-08")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_NOT_REQUIRED, LocalDate.parse("2023-08-03")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_NOT_REQUIRED, LocalDate.parse("2023-09-30")),
      createPrisonerLastUpdatedDate(Status.DONE, LocalDate.parse("2023-10-08")),
      createPrisonerLastUpdatedDate(Status.SUPPORT_DECLINED, LocalDate.parse("2023-10-31")),
      createPrisonerLastUpdatedDate(Status.DONE, LocalDate.parse("2023-11-08")),
      createPrisonerLastUpdatedDate(Status.IN_PROGRESS, LocalDate.parse("2023-11-22")),
      createPrisonerLastUpdatedDate(Status.NOT_STARTED, null),
      createPrisonerLastUpdatedDate(Status.NOT_STARTED, null),
    )
    offenderSearchApiService.sortPrisoners("lastUpdatedDate,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `test secondary sort by NomsId ascending, primary sort by prisoner name ascending`() {
    val prisoners = mutableListOf(
      createPrisonerNameAndNumber("A123456", "BERTRAND", "ANDERSON"),
      createPrisonerNameAndNumber("C394839", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y945849", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y340302", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("G394839", "ANDY", "ANDERSON"),
      createPrisonerNameAndNumber("B394839", "CHARLIE", "FOSTER"),
      createPrisonerNameAndNumber("N394839", "ARRAN", "PETERSON"),
      createPrisonerNameAndNumber("W394839", "LOUIS", "MCCARTHY"),
      createPrisonerNameAndNumber("S394839", "CORMAC", "CRAY"),
      createPrisonerNameAndNumber("K394839", "VLODIMIR", "MARSH"),
      createPrisonerNameAndNumber("E394839", "TOM", "WILLIAMSON"),
    )
    val sortedPrisoners = mutableListOf(
      createPrisonerNameAndNumber("G394839", "ANDY", "ANDERSON"),
      createPrisonerNameAndNumber("A123456", "BERTRAND", "ANDERSON"),
      createPrisonerNameAndNumber("S394839", "CORMAC", "CRAY"),
      createPrisonerNameAndNumber("B394839", "CHARLIE", "FOSTER"),
      createPrisonerNameAndNumber("K394839", "VLODIMIR", "MARSH"),
      createPrisonerNameAndNumber("W394839", "LOUIS", "MCCARTHY"),
      createPrisonerNameAndNumber("N394839", "ARRAN", "PETERSON"),
      createPrisonerNameAndNumber("C394839", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y340302", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y945849", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("E394839", "TOM", "WILLIAMSON"),
    )
    offenderSearchApiService.sortPrisonersByNomsId("ASC", prisoners)
    offenderSearchApiService.sortPrisoners("name,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `test secondary sort by NomsId descending, primary sort by prisoner name ascending`() {
    val prisoners = mutableListOf(
      createPrisonerNameAndNumber("A123456", "BERTRAND", "ANDERSON"),
      createPrisonerNameAndNumber("C394839", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y945849", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y340302", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("G394839", "ANDY", "ANDERSON"),
      createPrisonerNameAndNumber("B394839", "CHARLIE", "FOSTER"),
      createPrisonerNameAndNumber("N394839", "ARRAN", "PETERSON"),
      createPrisonerNameAndNumber("W394839", "LOUIS", "MCCARTHY"),
      createPrisonerNameAndNumber("S394839", "CORMAC", "CRAY"),
      createPrisonerNameAndNumber("K394839", "VLODIMIR", "MARSH"),
      createPrisonerNameAndNumber("E394839", "TOM", "WILLIAMSON"),
    )
    val sortedPrisoners = mutableListOf(
      createPrisonerNameAndNumber("G394839", "ANDY", "ANDERSON"),
      createPrisonerNameAndNumber("A123456", "BERTRAND", "ANDERSON"),
      createPrisonerNameAndNumber("S394839", "CORMAC", "CRAY"),
      createPrisonerNameAndNumber("B394839", "CHARLIE", "FOSTER"),
      createPrisonerNameAndNumber("K394839", "VLODIMIR", "MARSH"),
      createPrisonerNameAndNumber("W394839", "LOUIS", "MCCARTHY"),
      createPrisonerNameAndNumber("N394839", "ARRAN", "PETERSON"),
      createPrisonerNameAndNumber("Y945849", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y340302", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("C394839", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("E394839", "TOM", "WILLIAMSON"),
    )
    offenderSearchApiService.sortPrisonersByNomsId("DESC", prisoners)
    offenderSearchApiService.sortPrisoners("name,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `test sorts by nomsId by default`() {
    val prisonId = "MDI"
    val prisoners = mutableListOf(
      createPrisonerNumber("G234098"),
      createPrisonerNumber("A009090"),
      createPrisonerNumber("A471234"),
      createPrisonerNumber("G678952"),
      createPrisonerNumber("G567809"),
      createPrisonerNumber("G023456"),
      createPrisonerNumber("G461234"),
    )
    val sortedPrisoners = mutableListOf(
      createPrisonerNumber("A009090"),
      createPrisonerNumber("A471234"),
      createPrisonerNumber("G023456"),
      createPrisonerNumber("G234098"),
      createPrisonerNumber("G461234"),
      createPrisonerNumber("G567809"),
      createPrisonerNumber("G678952"),
    )
    offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, null, null, 0, 10)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  private fun createPrisoner(prisonId: String) = PrisonersSearch(prisonerNumber = "A123456", firstName = "firstName", lastName = "lastName", prisonId = prisonId, prisonName = "prisonName", cellLocation = null, youthOffender = false)
}

private fun createPrisonerNumber(prisonerNumber: String) = Prisoners(prisonerNumber = prisonerNumber, firstName = "firstName", lastName = "lastName", pathwayStatus = null)

private fun createPrisonerName(firstName: String, lastName: String) = Prisoners(prisonerNumber = "A123456", firstName = firstName, lastName = lastName, pathwayStatus = null)

private fun createPrisonerNameAndNumber(prisonerNumber: String, firstName: String, lastName: String) = Prisoners(prisonerNumber = prisonerNumber, firstName = firstName, lastName = lastName, pathwayStatus = null)
private fun createPrisonerParoleEligibilityDate(paroleEligibilityDate: LocalDate?) = Prisoners(prisonerNumber = "A123456", firstName = "SIMON", lastName = "BAMFORD", pathwayStatus = null, paroleEligibilityDate = paroleEligibilityDate)

private fun createPrisonerPathwayStatus(pathwayStatus: Status) = Prisoners(prisonerNumber = "A123456", firstName = "BORIS", lastName = "FRANKLIN", pathwayStatus = pathwayStatus)

private fun createPrisonerLastUpdatedDate(pathwayStatus: Status, lastUpdatedDate: LocalDate?) = Prisoners(prisonerNumber = "A123456", firstName = "OLIVER", lastName = "HAYES", pathwayStatus = pathwayStatus, lastUpdatedDate = lastUpdatedDate)
