package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoners
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearchList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ExtendWith(MockitoExtension::class)
class PrisonerServiceTest {

  private lateinit var prisonerService: PrisonerService

  @Mock
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Mock
  private lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Mock
  private lateinit var prisonApiService: PrisonApiService

  @Mock
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Mock
  private lateinit var deliusApiService: ResettlementPassportDeliusApiService

  @Mock
  private lateinit var watchlistService: WatchlistService

  @Mock
  private lateinit var prisonRegisterApiService: PrisonRegisterApiService

  @BeforeEach
  fun beforeEach() {
    prisonerService = PrisonerService(
      prisonerSearchApiService,
      prisonApiService,
      prisonerRepository,
      pathwayStatusRepository,
      resettlementAssessmentRepository,
      watchlistService,
      pathwayAndStatusService,
      deliusApiService,
      prisonRegisterApiService,
    )
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken("123", "sub") } returns "ABC11D"
  }

  @AfterEach
  fun afterEach() {
    unmockkAll()
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending - 1`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPrisonerId = "A8339DY"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-1.json")
    `when`(prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "")).thenReturn(mockedJsonResponse.content)

    val prisonersList =
      prisonerService.getPrisonersByPrisonId(
        "",
        prisonId,
        0,
        null,
        null,
        null,
        0,
        10,
        "releaseDate,DESC",
        false,
        "123",
      )
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending - 2`() {
    mockDatabaseCalls()

    val prisonId = "MDI"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-2.json")
    `when`(prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "")).thenReturn(mockedJsonResponse.content)

    val prisoners =
      prisonerService.getPrisonersByPrisonId(
        "",
        prisonId,
        0,
        null,
        null,
        null,
        0,
        10,
        "releaseDate,DESC",
        false,
        "123",
      )

    Assertions.assertEquals(getExpectedPrisonersListReleaseDateDesc(), prisoners)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Ascending`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPrisonerId = "A8339DY"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-1.json")
    `when`(prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "")).thenReturn(mockedJsonResponse.content)

    val prisonersList =
      prisonerService.getPrisonersByPrisonId(
        "",
        prisonId,
        0,
        null,
        null,
        null,
        0,
        20,
        "releaseDate,ASC",
        false,
        "123",
      )
    Assertions.assertEquals(
      expectedPrisonerId,
      prisonersList.content?.get((prisonersList.content!!.toList().size - 1))?.prisonerNumber
        ?: 0,
    )
  }

  @Test
  fun `test get PrisonersList happy path full json with sort name Ascending`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPrisonerId = "G1458GV"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-1.json")
    `when`(prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "")).thenReturn(mockedJsonResponse.content)

    val prisonersList =
      prisonerService.getPrisonersByPrisonId(
        "",
        prisonId,
        0,
        null,
        null,
        null,
        0,
        10,
        "name,ASC",
        false,
        "123",
      )
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json for page size`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPageSize = 3

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-1.json")
    `when`(prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "")).thenReturn(mockedJsonResponse.content)

    val prisonersList =
      prisonerService.getPrisonersByPrisonId("", prisonId, 0, null, null, null, 0, 5, "name,ASC", false, "123")
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

    var mockedJsonResponseString = readFile("testdata/prisoner-search-api/prisoner-search-1.json")
    mockedJsonResponseString = mockedJsonResponseString.replace(
      "\"releaseDate\": \"2024-07-31\",",
      "\"releaseDate\": \"" + releaseDate.format(pattern) + "\",",
    )

    val mockedJsonResponse: PrisonersSearchList = readStringAsObject(mockedJsonResponseString)
    `when`(prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "")).thenReturn(mockedJsonResponse.content)
    val prisonersList =
      prisonerService.getPrisonersByPrisonId(
        "",
        prisonId,
        0,
        null,
        null,
        null,
        0,
        10,
        "name,ASC",
        false,
        "123",
      )
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json for pathwayView`() {
    mockDatabaseCallsForPathwayView()

    val prisonId = "MDI"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-2.json")
    `when`(prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "")).thenReturn(mockedJsonResponse.content)

    val prisoners = prisonerService.getPrisonersByPrisonId(
      "",
      prisonId,
      0,
      Pathway.ACCOMMODATION,
      null,
      null,
      0,
      10,
      "releaseDate,DESC",
      false,
      "123",
    )

    Assertions.assertEquals(getExpectedPrisonersPathwayView(), prisoners)
  }

  @Test
  fun `test get PrisonersList happy path full json for pathwayView and pathwayStatus`() {
    mockDatabaseCallsForPathwayView()

    val prisonId = "MDI"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-2.json")
    `when`(prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "")).thenReturn(mockedJsonResponse.content)

    val prisoners = prisonerService.getPrisonersByPrisonId(
      "",
      prisonId,
      0,
      Pathway.ACCOMMODATION,
      Status.SUPPORT_DECLINED,
      null,
      0,
      10,
      "releaseDate,DESC",
      false,
      "123",
    )

    Assertions.assertEquals(getExpectedPrisonersPathwayViewAndPathwayStatus(), prisoners)
  }

  private fun mockDatabaseCalls() {
    val mockPrisonerEntity1 =
      PrisonerEntity(1, "G1458GV", LocalDateTime.now(), "test", "xyz", LocalDate.parse("2025-01-23"))
    val mockPrisonerEntity2 =
      PrisonerEntity(2, "A8339DY", LocalDateTime.now(), "test", "xyz", LocalDate.parse("2025-01-23"))
    val mockPrisonerEntity3 =
      PrisonerEntity(3, "A8229DY", LocalDateTime.now(), "test", "xyz", LocalDate.parse("2025-01-23"))

    val pathway1 = Pathway.ACCOMMODATION
    val pathway2 = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
    val pathway3 = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY

    val status = Status.NOT_STARTED

    val mockPathwayStatusEntities = listOf(
      PathwayStatusEntity(1, mockPrisonerEntity2, pathway1, status, LocalDateTime.now()),
      PathwayStatusEntity(2, mockPrisonerEntity2, pathway2, status, LocalDateTime.now()),
      PathwayStatusEntity(3, mockPrisonerEntity3, pathway2, status, LocalDateTime.now()),
      PathwayStatusEntity(4, mockPrisonerEntity3, pathway3, status, LocalDateTime.now()),
      PathwayStatusEntity(5, mockPrisonerEntity1, pathway3, status, LocalDateTime.now()),
      PathwayStatusEntity(6, mockPrisonerEntity1, pathway1, status, LocalDateTime.now()),
    )

    `when`(pathwayStatusRepository.findByPrison(any())).thenReturn(mockPathwayStatusEntities)
  }

  private fun mockDatabaseCallsForPathwayView() {
    val mockPrisonerEntity1 =
      PrisonerEntity(1, "A8229DY", LocalDateTime.now(), "1", "MDI", LocalDate.parse("2025-01-23"))
    val mockPrisonerEntity2 =
      PrisonerEntity(2, "G1458GV", LocalDateTime.now(), "2", "MDI", LocalDate.parse("2025-01-23"))
    val mockPrisonerEntity3 =
      PrisonerEntity(3, "A8339DY", LocalDateTime.now(), "3", "MDI", LocalDate.parse("2025-01-23"))

    val pathway = Pathway.ACCOMMODATION

    val status1 = Status.NOT_STARTED
    val status4 = Status.SUPPORT_DECLINED
    val status5 = Status.DONE

    `when`(
      resettlementAssessmentRepository.findPrisonersWithAllAssessmentsInStatus(
        "MDI",
        ResettlementAssessmentType.BCST2,
        ResettlementAssessmentStatus.SUBMITTED,
        Pathway.entries.size,
      ),
    )
      .thenReturn(listOf(mockPrisonerEntity1, mockPrisonerEntity2, mockPrisonerEntity3))

    `when`(pathwayStatusRepository.findByPrison("MDI")).thenReturn(
      listOf(
        PathwayStatusEntity(1, mockPrisonerEntity1, pathway, status1, LocalDateTime.now()),
        PathwayStatusEntity(8, mockPrisonerEntity2, pathway, status4, LocalDateTime.now()),
        PathwayStatusEntity(15, mockPrisonerEntity3, pathway, status5, LocalDateTime.now()),
      ),
    )
  }

  private fun getExpectedPrisonersListReleaseDateDesc() = PrisonersList(
    content =
    listOf(
      Prisoners(
        prisonerNumber = "A8339DY",
        firstName = "MR",
        middleNames = "BRIDGILLA",
        lastName = "CRD-LR-TEST",
        releaseDate = null,
        releaseType = "PRRD",
        lastUpdatedDate = LocalDate.now(),
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
        releaseEligibilityDate = null,
        releaseEligibilityType = null,
        assessmentRequired = true,
      ),
      Prisoners(
        prisonerNumber = "A8229DY",
        firstName = "STEPHEN",
        middleNames = null,
        lastName = "MCVEIGH",
        releaseDate = LocalDate.parse("2099-08-01"),
        releaseType = "CRD",
        lastUpdatedDate = LocalDate.now(),
        status = listOf(
          PathwayStatus(
            pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
            status = Status.NOT_STARTED,
            lastDateChange = LocalDate.now(),
          ),
          PathwayStatus(
            pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
            status = Status.NOT_STARTED,
            lastDateChange = LocalDate.now(),
          ),
        ),
        pathwayStatus = null,
        homeDetentionCurfewEligibilityDate = LocalDate.parse("2021-02-03"),
        paroleEligibilityDate = null,
        releaseEligibilityDate = LocalDate.parse("2021-02-03"),
        releaseEligibilityType = "HDCED",
        assessmentRequired = true,
      ),
      Prisoners(
        prisonerNumber = "G1458GV",
        firstName = "FINN",
        middleNames = "CHANDLEVIEVE",
        lastName = "CRAWFIS",
        releaseDate = LocalDate.parse("2098-09-12"),
        releaseType = "CRD",
        lastUpdatedDate = LocalDate.now(),
        status = listOf(
          PathwayStatus(
            pathway = Pathway.ACCOMMODATION,
            status = Status.NOT_STARTED,
            lastDateChange = LocalDate.now(),
          ),
          PathwayStatus(
            pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
            status = Status.NOT_STARTED,
            lastDateChange = LocalDate.now(),
          ),
        ),
        pathwayStatus = null,
        homeDetentionCurfewEligibilityDate = LocalDate.parse("2018-10-16"),
        paroleEligibilityDate = null,
        releaseEligibilityDate = LocalDate.parse("2018-10-16"),
        releaseEligibilityType = "HDCED",
        assessmentRequired = true,
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
        prisonerNumber = "A8339DY",
        firstName = "MR",
        middleNames = "BRIDGILLA",
        lastName = "CRD-LR-TEST",
        releaseDate = null,
        releaseType = "PRRD",
        lastUpdatedDate = LocalDate.now(),
        status = null,
        pathwayStatus = Status.DONE,
        homeDetentionCurfewEligibilityDate = null,
        paroleEligibilityDate = null,
        releaseEligibilityDate = null,
        releaseEligibilityType = null,
        assessmentRequired = false,
      ),
      Prisoners(
        prisonerNumber = "A8229DY",
        firstName = "STEPHEN",
        middleNames = null,
        lastName = "MCVEIGH",
        releaseDate = LocalDate.parse("2099-08-01"),
        releaseType = "CRD",
        lastUpdatedDate = LocalDate.now(),
        status = null,
        pathwayStatus = Status.NOT_STARTED,
        homeDetentionCurfewEligibilityDate = LocalDate.parse("2021-02-03"),
        paroleEligibilityDate = null,
        releaseEligibilityDate = LocalDate.parse("2021-02-03"),
        releaseEligibilityType = "HDCED",
        assessmentRequired = false,
      ),
      Prisoners(
        prisonerNumber = "G1458GV",
        firstName = "FINN",
        middleNames = "CHANDLEVIEVE",
        lastName = "CRAWFIS",
        releaseDate = LocalDate.parse("2098-09-12"),
        releaseType = "CRD",
        lastUpdatedDate = LocalDate.now(),
        status = null,
        pathwayStatus = Status.SUPPORT_DECLINED,
        homeDetentionCurfewEligibilityDate = LocalDate.parse("2018-10-16"),
        paroleEligibilityDate = null,
        releaseEligibilityDate = LocalDate.parse("2018-10-16"),
        releaseEligibilityType = "HDCED",
        assessmentRequired = false,
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
        lastUpdatedDate = LocalDate.now(),
        status = null,
        pathwayStatus = Status.SUPPORT_DECLINED,
        homeDetentionCurfewEligibilityDate = LocalDate.parse("2018-10-16"),
        paroleEligibilityDate = null,
        releaseEligibilityDate = LocalDate.parse("2018-10-16"),
        releaseEligibilityType = "HDCED",
        assessmentRequired = false,
      ),
    ),
    pageSize = 1,
    page = 0,
    sortName = "releaseDate,DESC",
    totalElements = 1,
    last = true,
  )

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
    prisonerService.sortPrisonersByField("name,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `test sort prisoners- sort prisoners by name descending`() {
    val prisoners = mutableListOf(
      createPrisonerName("BERTRAND", "ANDERSON"),
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
      createPrisonerName("TOM", "WILLIAMSON"),
      createPrisonerName("ZACHARY", "SMITH"),
      createPrisonerName("ARRAN", "PETERSON"),
      createPrisonerName("LOUIS", "MCCARTHY"),
      createPrisonerName("VLODIMIR", "MARSH"),
      createPrisonerName("CHARLIE", "FOSTER"),
      createPrisonerName("CORMAC", "CRAY"),
      createPrisonerName("BERTRAND", "ANDERSON"),
      createPrisonerName("ANDY", "ANDERSON"),
    )
    prisonerService.sortPrisonersByField("name,DESC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `Unit test- get release eligibility date from PED and HDCED`() {
    val prisoner = (
      createPrisonerPEDandHDCED(LocalDate.parse("2029-08-30"), null)
      )
    val releaseEligibilityDate = prisonerService.getDisplayedReleaseEligibilityDate(prisoner)
    Assertions.assertEquals(LocalDate.parse("2029-08-30"), releaseEligibilityDate)
  }

  @Test
  fun `Prisoner List- get release eligibility date from PED and HDCED`() {
    val prisoners = listOf(
      createPrisonerPEDandHDCED(LocalDate.parse("2029-08-30"), null),
      createPrisonerPEDandHDCED(null, null),
      createPrisonerPEDandHDCED(null, LocalDate.parse("2037-01-01")),
      createPrisonerPEDandHDCED(LocalDate.parse("2028-11-11"), null),
      createPrisonerPEDandHDCED(null, LocalDate.parse("2024-12-08")),
      createPrisonerPEDandHDCED(null, LocalDate.parse("2026-07-21")),
      createPrisonerPEDandHDCED(LocalDate.parse("2078-04-03"), LocalDate.parse("2036-04-03")),
      createPrisonerPEDandHDCED(LocalDate.parse("2078-12-03"), LocalDate.parse("2046-12-03")),
      createPrisonerPEDandHDCED(null, null),
      createPrisonerPEDandHDCED(null, LocalDate.parse("2026-02-01")),
      createPrisonerPEDandHDCED(LocalDate.parse("2026-07-24"), null),
      createPrisonerPEDandHDCED(LocalDate.parse("2024-12-09"), null),
    )

    val prisonersMapped = mutableListOf(
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2029-08-30"),
        null,
        LocalDate.parse("2029-08-30"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(null, null, null, null),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2037-01-01"),
        LocalDate.parse("2037-01-01"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2028-11-11"),
        null,
        LocalDate.parse("2028-11-11"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2024-12-08"),
        LocalDate.parse("2024-12-08"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2026-07-21"),
        LocalDate.parse("2026-07-21"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2078-04-03"),
        LocalDate.parse("2036-04-03"),
        LocalDate.parse("2036-04-03"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2078-12-03"),
        LocalDate.parse("2046-12-03"),
        LocalDate.parse("2046-12-03"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(null, null, null, null),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2026-02-01"),
        LocalDate.parse("2026-02-01"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2026-07-24"),
        null,
        LocalDate.parse("2026-07-24"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2024-12-09"),
        null,
        LocalDate.parse("2024-12-09"),
        "HDCED",
      ),
    )
    val actualPrisoners = prisonerService.objectMapper(prisoners, null, null, "MDI", null, false, "123")
    Assertions.assertEquals(prisonersMapped, actualPrisoners)
  }

  @Test
  fun `sort prisoners by release eligibility date- ascending`() {
    val prisoners = mutableListOf(
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2029-08-30"),
        null,
        LocalDate.parse("2029-08-30"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(null, null, null, null),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2037-01-01"),
        LocalDate.parse("2037-01-01"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2028-11-11"),
        null,
        LocalDate.parse("2028-11-11"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2024-12-08"),
        LocalDate.parse("2024-12-08"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2026-07-21"),
        LocalDate.parse("2026-07-21"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2078-04-03"),
        LocalDate.parse("2036-04-03"),
        LocalDate.parse("2036-04-03"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2078-12-03"),
        LocalDate.parse("2046-12-03"),
        LocalDate.parse("2046-12-03"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(null, null, null, null),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2026-02-01"),
        LocalDate.parse("2026-02-01"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2026-07-24"),
        null,
        LocalDate.parse("2026-07-24"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2024-12-09"),
        null,
        LocalDate.parse("2024-12-09"),
        "HDCED",
      ),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2024-12-08"),
        LocalDate.parse("2024-12-08"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2024-12-09"),
        null,
        LocalDate.parse("2024-12-09"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2026-02-01"),
        LocalDate.parse("2026-02-01"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2026-07-21"),
        LocalDate.parse("2026-07-21"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2026-07-24"),
        null,
        LocalDate.parse("2026-07-24"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2028-11-11"),
        null,
        LocalDate.parse("2028-11-11"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2029-08-30"),
        null,
        LocalDate.parse("2029-08-30"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2078-04-03"),
        LocalDate.parse("2036-04-03"),
        LocalDate.parse("2036-04-03"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2037-01-01"),
        LocalDate.parse("2037-01-01"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2078-12-03"),
        LocalDate.parse("2046-12-03"),
        LocalDate.parse("2046-12-03"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(null, null, null, null),
      createPrisonerReleaseEligibilityDateAndType(null, null, null, null),
    )
    prisonerService.sortPrisoners("releaseEligibilityDate,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by release eligibility date- descending`() {
    val prisoners = mutableListOf(
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2029-08-30"),
        null,
        LocalDate.parse("2029-08-30"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(null, null, null, null),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2037-01-01"),
        LocalDate.parse("2037-01-01"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2028-11-11"),
        null,
        LocalDate.parse("2028-11-11"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2024-12-08"),
        LocalDate.parse("2024-12-08"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2026-07-21"),
        LocalDate.parse("2026-07-21"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2078-04-03"),
        LocalDate.parse("2036-04-03"),
        LocalDate.parse("2036-04-03"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2078-12-03"),
        LocalDate.parse("2046-12-03"),
        LocalDate.parse("2046-12-03"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(null, null, null, null),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2026-02-01"),
        LocalDate.parse("2026-02-01"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2024-12-09"),
        null,
        LocalDate.parse("2024-12-09"),
        "HDCED",
      ),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerReleaseEligibilityDateAndType(null, null, null, null),
      createPrisonerReleaseEligibilityDateAndType(null, null, null, null),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2078-12-03"),
        LocalDate.parse("2046-12-03"),
        LocalDate.parse("2046-12-03"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2037-01-01"),
        LocalDate.parse("2037-01-01"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2078-04-03"),
        LocalDate.parse("2036-04-03"),
        LocalDate.parse("2036-04-03"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2029-08-30"),
        null,
        LocalDate.parse("2029-08-30"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2028-11-11"),
        null,
        LocalDate.parse("2028-11-11"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2026-07-21"),
        LocalDate.parse("2026-07-21"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2026-02-01"),
        LocalDate.parse("2026-02-01"),
        "PED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        LocalDate.parse("2024-12-09"),
        null,
        LocalDate.parse("2024-12-09"),
        "HDCED",
      ),
      createPrisonerReleaseEligibilityDateAndType(
        null,
        LocalDate.parse("2024-12-08"),
        LocalDate.parse("2024-12-08"),
        "PED",
      ),
    )
    prisonerService.sortPrisoners("releaseEligibilityDate,DESC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by release date- descending`() {
    val prisoners = mutableListOf(
      createPrisonerReleaseDate(LocalDate.parse("2029-08-30")),
      createPrisonerReleaseDate(LocalDate.parse("2037-01-01")),
      createPrisonerReleaseDate(LocalDate.parse("2028-11-11")),
      createPrisonerReleaseDate(LocalDate.parse("2024-12-08")),
      createPrisonerReleaseDate(LocalDate.parse("2026-07-21")),
      createPrisonerReleaseDate(null),
      createPrisonerReleaseDate(LocalDate.parse("2078-04-03")),
      createPrisonerReleaseDate(LocalDate.parse("2046-12-03")),
      createPrisonerReleaseDate(LocalDate.parse("2026-02-01")),
      createPrisonerReleaseDate(null),
      createPrisonerReleaseDate(LocalDate.parse("2026-07-24")),
      createPrisonerReleaseDate(LocalDate.parse("2024-12-09")),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerReleaseDate(null),
      createPrisonerReleaseDate(null),
      createPrisonerReleaseDate(LocalDate.parse("2078-04-03")),
      createPrisonerReleaseDate(LocalDate.parse("2046-12-03")),
      createPrisonerReleaseDate(LocalDate.parse("2037-01-01")),
      createPrisonerReleaseDate(LocalDate.parse("2029-08-30")),
      createPrisonerReleaseDate(LocalDate.parse("2028-11-11")),
      createPrisonerReleaseDate(LocalDate.parse("2026-07-24")),
      createPrisonerReleaseDate(LocalDate.parse("2026-07-21")),
      createPrisonerReleaseDate(LocalDate.parse("2026-02-01")),
      createPrisonerReleaseDate(LocalDate.parse("2024-12-09")),
      createPrisonerReleaseDate(LocalDate.parse("2024-12-08")),
    )
    prisonerService.sortPrisonersByField("releaseDate,DESC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by release date- ascending`() {
    val prisoners = mutableListOf(
      createPrisonerReleaseDate(LocalDate.parse("2029-08-30")),
      createPrisonerReleaseDate(LocalDate.parse("2037-01-01")),
      createPrisonerReleaseDate(LocalDate.parse("2028-11-11")),
      createPrisonerReleaseDate(LocalDate.parse("2024-12-08")),
      createPrisonerReleaseDate(LocalDate.parse("2026-07-21")),
      createPrisonerReleaseDate(null),
      createPrisonerReleaseDate(LocalDate.parse("2078-04-03")),
      createPrisonerReleaseDate(LocalDate.parse("2046-12-03")),
      createPrisonerReleaseDate(LocalDate.parse("2026-02-01")),
      createPrisonerReleaseDate(null),
      createPrisonerReleaseDate(LocalDate.parse("2026-07-24")),
      createPrisonerReleaseDate(LocalDate.parse("2024-12-09")),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerReleaseDate(LocalDate.parse("2024-12-08")),
      createPrisonerReleaseDate(LocalDate.parse("2024-12-09")),
      createPrisonerReleaseDate(LocalDate.parse("2026-02-01")),
      createPrisonerReleaseDate(LocalDate.parse("2026-07-21")),
      createPrisonerReleaseDate(LocalDate.parse("2026-07-24")),
      createPrisonerReleaseDate(LocalDate.parse("2028-11-11")),
      createPrisonerReleaseDate(LocalDate.parse("2029-08-30")),
      createPrisonerReleaseDate(LocalDate.parse("2037-01-01")),
      createPrisonerReleaseDate(LocalDate.parse("2046-12-03")),
      createPrisonerReleaseDate(LocalDate.parse("2078-04-03")),
      createPrisonerReleaseDate(null),
      createPrisonerReleaseDate(null),
    )
    prisonerService.sortPrisonersByField("releaseDate,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by release on temporary licence date- ascending`() {
    val prisoners = mutableListOf(
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2029-08-30")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2037-01-01")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2028-11-11")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2024-12-08")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-07-21")),
      createPrisonerReleaseOnTempLicenceDate(null),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2078-04-03")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2046-12-03")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-02-01")),
      createPrisonerReleaseOnTempLicenceDate(null),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-07-24")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2024-12-09")),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2024-12-08")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2024-12-09")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-02-01")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-07-21")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-07-24")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2028-11-11")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2029-08-30")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2037-01-01")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2046-12-03")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2078-04-03")),
      createPrisonerReleaseOnTempLicenceDate(null),
      createPrisonerReleaseOnTempLicenceDate(null),
    )
    prisonerService.sortPrisoners("releaseOnTemporaryLicenceDate,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `sort prisoners by release on temporary licence date- descending`() {
    val prisoners = mutableListOf(
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2029-08-30")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2037-01-01")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2028-11-11")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2024-12-08")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-07-21")),
      createPrisonerReleaseOnTempLicenceDate(null),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2078-04-03")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2046-12-03")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-02-01")),
      createPrisonerReleaseOnTempLicenceDate(null),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-07-24")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2024-12-09")),
    )

    val sortedPrisoners = mutableListOf(
      createPrisonerReleaseOnTempLicenceDate(null),
      createPrisonerReleaseOnTempLicenceDate(null),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2078-04-03")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2046-12-03")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2037-01-01")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2029-08-30")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2028-11-11")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-07-24")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-07-21")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2026-02-01")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2024-12-09")),
      createPrisonerReleaseOnTempLicenceDate(LocalDate.parse("2024-12-08")),
    )
    prisonerService.sortPrisoners("releaseOnTemporaryLicenceDate,DESC", prisoners)
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
    prisonerService.sortPrisonersByField("pathwayStatus,ASC", prisoners)
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
    prisonerService.sortPrisonersByField("pathwayStatus,DESC", prisoners)
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
    prisonerService.sortPrisonersByField("lastUpdatedDate,DESC", prisoners)
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
    prisonerService.sortPrisonersByField("lastUpdatedDate,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `test secondary sort by NomsId ascending, primary sort by prisoner number ascending`() {
    val prisoners = mutableListOf(
      createPrisonerNumber("A123456"),
      createPrisonerNumber("C394839"),
      createPrisonerNumber("Y945849"),
      createPrisonerNumber("Y340302"),
      createPrisonerNumber("G394839"),
      createPrisonerNumber("B394839"),
      createPrisonerNumber("N394839"),
      createPrisonerNumber("W394839"),
      createPrisonerNumber("S394839"),
      createPrisonerNumber("K394839"),
      createPrisonerNumber("E394839"),
    )
    val sortedPrisoners = mutableListOf(
      createPrisonerNumber("A123456"),
      createPrisonerNumber("B394839"),
      createPrisonerNumber("C394839"),
      createPrisonerNumber("E394839"),
      createPrisonerNumber("G394839"),
      createPrisonerNumber("K394839"),
      createPrisonerNumber("N394839"),
      createPrisonerNumber("S394839"),
      createPrisonerNumber("W394839"),
      createPrisonerNumber("Y340302"),
      createPrisonerNumber("Y945849"),

    )
    prisonerService.sortPrisoners("prisonerNumber,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `test secondary sort by NomsId ascending, primary sort by prisoner name ascending`() {
    val prisoners = mutableListOf(
      createPrisonerNameAndNumber("A123456", "BERTRAND", "ANDERSON"),
      createPrisonerNameAndNumber("C394839", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y340302", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("G394839", "ANDY", "ANDERSON"),
      createPrisonerNameAndNumber("B394839", "CHARLIE", "FOSTER"),
      createPrisonerNameAndNumber("N394839", "ARRAN", "PETERSON"),
      createPrisonerNameAndNumber("W394839", "LOUIS", "MCCARTHY"),
      createPrisonerNameAndNumber("S394839", "CORMAC", "CRAY"),
      createPrisonerNameAndNumber("Y945849", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("A098762", "SAMUEL", "MARSH"),
      createPrisonerNameAndNumber("P234501", "ADAM", "MARSH"),
      createPrisonerNameAndNumber("K394839", "VLODIMIR", "MARSH"),
      createPrisonerNameAndNumber("E394839", "TOM", "WILLIAMSON"),
      createPrisonerNameAndNumber("A645849", "ZACHARY", "SMITH"),
    )
    val sortedPrisoners = mutableListOf(
      createPrisonerNameAndNumber("G394839", "ANDY", "ANDERSON"),
      createPrisonerNameAndNumber("A123456", "BERTRAND", "ANDERSON"),
      createPrisonerNameAndNumber("S394839", "CORMAC", "CRAY"),
      createPrisonerNameAndNumber("B394839", "CHARLIE", "FOSTER"),
      createPrisonerNameAndNumber("P234501", "ADAM", "MARSH"),
      createPrisonerNameAndNumber("A098762", "SAMUEL", "MARSH"),
      createPrisonerNameAndNumber("K394839", "VLODIMIR", "MARSH"),
      createPrisonerNameAndNumber("W394839", "LOUIS", "MCCARTHY"),
      createPrisonerNameAndNumber("N394839", "ARRAN", "PETERSON"),
      createPrisonerNameAndNumber("A645849", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("C394839", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y340302", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y945849", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("E394839", "TOM", "WILLIAMSON"),
    )
    prisonerService.sortPrisoners("name,ASC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `test secondary sort by NomsId descending, primary sort by prisoner name descending`() {
    val prisoners = mutableListOf(
      createPrisonerNameAndNumber("A123456", "BERTRAND", "ANDERSON"),
      createPrisonerNameAndNumber("C394839", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y340302", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("G394839", "ANDY", "ANDERSON"),
      createPrisonerNameAndNumber("B394839", "CHARLIE", "FOSTER"),
      createPrisonerNameAndNumber("N394839", "ARRAN", "PETERSON"),
      createPrisonerNameAndNumber("W394839", "LOUIS", "MCCARTHY"),
      createPrisonerNameAndNumber("S394839", "CORMAC", "CRAY"),
      createPrisonerNameAndNumber("Y945849", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("A098762", "SAMUEL", "MARSH"),
      createPrisonerNameAndNumber("P234501", "ADAM", "MARSH"),
      createPrisonerNameAndNumber("K394839", "VLODIMIR", "MARSH"),
      createPrisonerNameAndNumber("E394839", "TOM", "WILLIAMSON"),
      createPrisonerNameAndNumber("A645849", "ZACHARY", "SMITH"),
    )
    val sortedPrisoners = mutableListOf(
      createPrisonerNameAndNumber("E394839", "TOM", "WILLIAMSON"),
      createPrisonerNameAndNumber("Y945849", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("Y340302", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("C394839", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("A645849", "ZACHARY", "SMITH"),
      createPrisonerNameAndNumber("N394839", "ARRAN", "PETERSON"),
      createPrisonerNameAndNumber("W394839", "LOUIS", "MCCARTHY"),
      createPrisonerNameAndNumber("K394839", "VLODIMIR", "MARSH"),
      createPrisonerNameAndNumber("A098762", "SAMUEL", "MARSH"),
      createPrisonerNameAndNumber("P234501", "ADAM", "MARSH"),
      createPrisonerNameAndNumber("B394839", "CHARLIE", "FOSTER"),
      createPrisonerNameAndNumber("S394839", "CORMAC", "CRAY"),
      createPrisonerNameAndNumber("A123456", "BERTRAND", "ANDERSON"),
      createPrisonerNameAndNumber("G394839", "ANDY", "ANDERSON"),
    )
    prisonerService.sortPrisoners("name,DESC", prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  @Test
  fun `test sorts by nomsId by default`() {
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
    prisonerService.sortPrisoners(null, prisoners)
    Assertions.assertEquals(sortedPrisoners, prisoners)
  }

  private fun createPrisonerNumber(prisonerNumber: String) =
    Prisoners(
      prisonerNumber = prisonerNumber,
      firstName = "firstName",
      lastName = "lastName",
      pathwayStatus = null,
      assessmentRequired = true,
    )

  private fun createPrisonerName(firstName: String, lastName: String) =
    Prisoners(
      prisonerNumber = "A123456",
      firstName = firstName,
      lastName = lastName,
      pathwayStatus = null,
      assessmentRequired = true,
    )

  private fun createPrisonerNameAndNumber(prisonerNumber: String, firstName: String, lastName: String) =
    Prisoners(
      prisonerNumber = prisonerNumber,
      firstName = firstName,
      lastName = lastName,
      pathwayStatus = null,
      assessmentRequired = true,
    )

  private fun createPrisonerPEDandHDCED(
    homeDetentionCurfewEligibilityDate: LocalDate?,
    paroleEligibilityDate: LocalDate?,
  ) = PrisonersSearch(
    prisonerNumber = "A123456",
    firstName = "SIMON",
    lastName = "BAMFORD",
    homeDetentionCurfewEligibilityDate = homeDetentionCurfewEligibilityDate,
    paroleEligibilityDate = paroleEligibilityDate,
    youthOffender = true,
    prisonId = "MDI",
    prisonName = "Midlands",
    cellLocation = "2A",
  )

  private fun createPrisonerReleaseEligibilityDateAndType(
    homeDetentionCurfewEligibilityDate: LocalDate?,
    paroleEligibilityDate: LocalDate?,
    releaseEligibilityDate: LocalDate?,
    releaseEligibilityType: String?,
  ) = Prisoners(
    prisonerNumber = "A123456",
    firstName = "SIMON",
    lastName = "BAMFORD",
    pathwayStatus = null,
    homeDetentionCurfewEligibilityDate = homeDetentionCurfewEligibilityDate,
    paroleEligibilityDate = paroleEligibilityDate,
    releaseEligibilityDate = releaseEligibilityDate,
    releaseEligibilityType = releaseEligibilityType,
    assessmentRequired = true,
    status = Pathway.entries.map { PathwayStatus(it, Status.NOT_STARTED) },
  )

  private fun createPrisonerReleaseDate(releaseDate: LocalDate?) = Prisoners(
    prisonerNumber = "A123456",
    firstName = "PATRICK",
    lastName = "WICKENDEN",
    pathwayStatus = null,
    releaseDate = releaseDate,
    assessmentRequired = true,
  )

  private fun createPrisonerReleaseOnTempLicenceDate(releaseOnTempLicenceDate: LocalDate?) = Prisoners(
    prisonerNumber = "A123456",
    firstName = "PATRICK",
    lastName = "WICKENDEN",
    pathwayStatus = null,
    releaseOnTemporaryLicenceDate = releaseOnTempLicenceDate,
    assessmentRequired = true,
  )

  private fun createPrisonerPathwayStatus(pathwayStatus: Status) =
    Prisoners(
      prisonerNumber = "A123456",
      firstName = "BORIS",
      lastName = "FRANKLIN",
      pathwayStatus = pathwayStatus,
      assessmentRequired = true,
    )

  private fun createPrisonerLastUpdatedDate(pathwayStatus: Status, lastUpdatedDate: LocalDate?) = Prisoners(
    prisonerNumber = "A123456",
    firstName = "OLIVER",
    lastName = "HAYES",
    pathwayStatus = pathwayStatus,
    lastUpdatedDate = lastUpdatedDate,
    assessmentRequired = true,
  )

  private inline fun <reified T> readFileAsObject(filename: String): T = readStringAsObject(readFile(filename))
  private inline fun <reified T> readStringAsObject(string: String): T = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerKotlinModule().registerModule(JavaTimeModule()).readValue(string)
}
