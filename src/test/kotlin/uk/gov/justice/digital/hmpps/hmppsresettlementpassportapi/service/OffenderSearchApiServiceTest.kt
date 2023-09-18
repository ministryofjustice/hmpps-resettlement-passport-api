package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoners
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
class OffenderSearchApiServiceTest {

  private val mockWebServer: MockWebServer = MockWebServer()

  @Mock
  private lateinit var pathwayRepository: PathwayRepository

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Mock
  private lateinit var offenderSearchApiService: OffenderSearchApiService

  @Mock
  private lateinit var pathwayApiService: PathwayApiService

  @BeforeEach
  fun beforeEach() {
    mockWebServer.start()
    val webClient = WebClient.create(mockWebServer.url("/").toUrl().toString())
    offenderSearchApiService = OffenderSearchApiService(
      pathwayRepository,
      prisonerRepository,
      pathwayStatusRepository,
      webClient,
      webClient,
      pathwayApiService,
    )
    mockDatabaseCalls()
  }

  @AfterEach
  fun afterEach() {
    mockWebServer.shutdown()
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6933GF"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 10, "releaseDate,DESC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending with youth offenders`() = runTest {
    val prisonId = "MDI"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-2.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisoners = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 10, "releaseDate,DESC")

    Assertions.assertEquals(getExpectedPrisonersListReleaseDateDescWithYO(), prisoners)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Ascending`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "A8257DY"

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
  fun `test get PrisonersList happy path full json with sort firstName Ascending`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6628UE"

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 10, "firstName,ASC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json for page size`() = runTest {
    val prisonId = "MDI"
    val expectedPageSize = 5

    val mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 5, "firstName,ASC")
    Assertions.assertEquals(expectedPageSize, prisonersList.pageSize)
    prisonersList.content?.toList()?.let { Assertions.assertEquals(expectedPageSize, it.size) }
  }

  private fun mockDatabaseCalls() {
    val mockPrisonerEntity = PrisonerEntity(1, "TEST", LocalDateTime.now(), "test")
    val mockPathwayEntity1 = PathwayEntity(1, "Accommodation", true, LocalDateTime.now())
    val mockPathwayEntity2 = PathwayEntity(2, "Attitudes, thinking and behaviour", true, LocalDateTime.now())
    val mockPathwayEntity3 = PathwayEntity(3, "Children, families and communities", false, LocalDateTime.now())

    val mockStatusEntity = StatusEntity(1, "Not Started", true, LocalDateTime.now())

    `when`(prisonerRepository.findByNomsId(any())).thenReturn(mockPrisonerEntity)
    `when`(pathwayRepository.findAll()).thenReturn(listOf(mockPathwayEntity1, mockPathwayEntity2, mockPathwayEntity3))

    `when`(pathwayStatusRepository.findByPathwayAndPrisoner(any(), any())).thenReturn(
      PathwayStatusEntity(1, mockPrisonerEntity, mockPathwayEntity1, mockStatusEntity, LocalDateTime.now()),
    )
  }

  @Test
  fun `test get PrisonersList happy path full json with release date filter`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6628UE"
    var days = 84
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var releaseDate = LocalDate.now().minusDays(84)
    var mockedJsonResponse = readFile("testdata/offender-search-api/prisoner-offender-search-1.json")
    mockedJsonResponse = mockedJsonResponse.replace("\"releaseDate\": \"2024-07-31\",", "\"releaseDate\": \"" + releaseDate.format(pattern) + "\",")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId("", prisonId, 0, 0, 10, "firstName,ASC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }
  private fun getExpectedPrisonersListReleaseDateDescWithYO() = PrisonersList(
    content =
    listOf(
      Prisoners(
        prisonerNumber = "G6933GF",
        firstName = "BUSTER",
        middleNames = "CHRISTABERT HECTUR",
        lastName = "CORALLO",
        releaseDate = LocalDate.parse("2024-08-02"),
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
      ),
      Prisoners(
        prisonerNumber = "G6335VX",
        firstName = "GARRETT",
        middleNames = "SYLVANNA",
        lastName = "COUTCHER",
        releaseDate = LocalDate.parse("2024-05-11"),
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
      ),
      Prisoners(
        prisonerNumber = "G1458GV",
        firstName = "FINN",
        middleNames = "CHANDLEVIEVE",
        lastName = "CRAWFIS",
        releaseDate = LocalDate.parse("2023-12-12"),
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
      ),
      Prisoners(
        prisonerNumber = "A8258DY",
        firstName = "COBBIE",
        middleNames = null,
        lastName = "FEDDER",
        releaseDate = LocalDate.parse("2023-09-15"),
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
      ),
      Prisoners(
        prisonerNumber = "A8314DY",
        firstName = "CHAIM",
        middleNames = null,
        lastName = "WITTKOPP",
        releaseDate = LocalDate.parse("2023-07-01"),
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
      ),
      Prisoners(
        prisonerNumber = "A8229DY",
        firstName = "STEPHEN",
        middleNames = null,
        lastName = "MCVEIGH",
        releaseDate = LocalDate.parse("2023-07-01"),
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
      ),
      Prisoners(
        prisonerNumber = "A8257DY",
        firstName = "GLENN",
        middleNames = null,
        lastName = "MCGRATH",
        releaseDate = null,
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
      ),
    ),
    pageSize = 7,
    page = 0,
    sortName = "releaseDate,DESC",
    totalElements = 7,
    last = true,
  )
}
