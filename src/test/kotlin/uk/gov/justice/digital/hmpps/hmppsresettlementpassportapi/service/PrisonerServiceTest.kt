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
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoners
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearchList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerWithStatusProjection
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ProfileTagsRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
  private lateinit var profileTagsRepository: ProfileTagsRepository

  @Mock
  private lateinit var caseAllocationService: CaseAllocationService

  @Mock
  private lateinit var resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService

  @Mock
  private lateinit var supportNeedsService: SupportNeedsService

  @Mock
  private lateinit var resettlementAssessmentService: ResettlementAssessmentService

  @BeforeEach
  fun beforeEach() {
    prisonerService = PrisonerService(
      prisonerSearchApiService,
      prisonApiService,
      prisonerRepository,
      pathwayStatusRepository,
      resettlementAssessmentRepository,
      profileTagsRepository,
      watchlistService,
      pathwayAndStatusService,
      deliusApiService,
      caseAllocationService,
      resettlementPassportDeliusApiService,
      supportNeedsService,
      resettlementAssessmentService,
    )
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken("123", "sub") } returns "ABC11D"
  }

  @AfterEach
  fun afterEach() {
    unmockkAll()
  }

  @Test
  fun `getPrisonerDetailsByNomsId with isHomeDetention true`() {
    mockDatabaseCalls(false)
    val expectedPrisonerId = "A8339DY"
    val mockEntity = PrisonerEntity(
      nomsId = expectedPrisonerId,
      creationDate = LocalDateTime.now(),
      prisonId = "MDI",
      id = 1L,
    )

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-1.json")
    whenever(prisonerSearchApiService.findPrisonerPersonalDetails(expectedPrisonerId)).thenReturn(mockedJsonResponse.content!![0])
    whenever(pathwayAndStatusService.getOrCreatePrisoner(expectedPrisonerId, "MDI")).thenReturn(mockEntity)
    whenever(pathwayAndStatusService.findAllPathwayStatusForPrisoner(eq(mockEntity))).thenReturn(
      Pathway.entries.mapIndexed { index, pathway ->
        PathwayStatusEntity(id = index.toLong(), prisonerId = 1, pathway = pathway, status = Status.NOT_STARTED)
      },
    )

    val prisoner =
      prisonerService.getPrisonerDetailsByNomsId(
        expectedPrisonerId,
        false,
        "123",
      )
    Assertions.assertTrue(prisoner.personalDetails?.isHomeDetention!!)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending - 1`() {
    mockDatabaseCalls()

    val prisonId = "MDI"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-1.json")

    whenever(prisonerSearchApiService.findPrisonersByPrisonId(prisonId)).thenReturn(mockedJsonResponse.content)

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
        false,
        "123",
        null,
      )

    val expectedPrisonerList = PrisonersList(
      content = listOf(
        Prisoners(
          prisonerNumber = "A8339DY",
          firstName = "MR",
          middleNames = "BRIDGILLA",
          lastName = "CRD-LR-TEST",
          releaseDate = null,
          releaseType = "PRRD",
          lastUpdatedDate = LocalDate.now(),
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = null,
          paroleEligibilityDate = null,
          releaseEligibilityDate = null,
          releaseEligibilityType = null,
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
        Prisoners(
          prisonerNumber = "G1458GV",
          firstName = "FINN",
          middleNames = "CHANDLEVIEVE",
          lastName = "CRAWFIS",
          releaseDate = LocalDate.parse("2099-09-12"),
          releaseType = "CRD",
          lastUpdatedDate = LocalDate.now(),
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = LocalDate.parse("2018-10-16"),
          paroleEligibilityDate = null,
          releaseEligibilityDate = LocalDate.parse("2018-10-16"),
          releaseEligibilityType = "HDCED",
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
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
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = LocalDate.parse("2021-02-03"),
          paroleEligibilityDate = null,
          releaseEligibilityDate = LocalDate.parse("2021-02-03"),
          releaseEligibilityType = "HDCED",
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
      ),
      pageSize = 3,
      page = 0,
      sortName = "releaseDate,DESC",
      totalElements = 3,
      last = true,
    )
    Assertions.assertEquals(expectedPrisonerList, prisonersList)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending - include past release dates`() {
    mockDatabaseCalls()

    val prisonId = "MDI"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-1.json")
    whenever(prisonerSearchApiService.findPrisonersByPrisonId(prisonId)).thenReturn(mockedJsonResponse.content)

    val prisonersList =
      prisonerService.getPrisonersByPrisonId(
        searchTerm = "",
        prisonId = prisonId,
        days = 0,
        pathwayView = null,
        pathwayStatus = null,
        assessmentRequired = null,
        pageNumber = 0,
        pageSize = 10,
        sort = "releaseDate,DESC",
        watchList = false,
        includePastReleaseDates = true,
        auth = "123",
        null,
      )

    val expectedPrisonerList = PrisonersList(
      content = listOf(
        Prisoners(
          prisonerNumber = "A8339DY",
          firstName = "MR",
          middleNames = "BRIDGILLA",
          lastName = "CRD-LR-TEST",
          releaseDate = null,
          releaseType = "PRRD",
          lastUpdatedDate = LocalDate.now(),
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = null,
          paroleEligibilityDate = null,
          releaseEligibilityDate = null,
          releaseEligibilityType = null,
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
        Prisoners(
          prisonerNumber = "G1458GV",
          firstName = "FINN",
          middleNames = "CHANDLEVIEVE",
          lastName = "CRAWFIS",
          releaseDate = LocalDate.parse("2099-09-12"),
          releaseType = "CRD",
          lastUpdatedDate = LocalDate.now(),
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = LocalDate.parse("2018-10-16"),
          paroleEligibilityDate = null,
          releaseEligibilityDate = LocalDate.parse("2018-10-16"),
          releaseEligibilityType = "HDCED",
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
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
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = LocalDate.parse("2021-02-03"),
          paroleEligibilityDate = null,
          releaseEligibilityDate = LocalDate.parse("2021-02-03"),
          releaseEligibilityType = "HDCED",
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
        Prisoners(
          prisonerNumber = "A8258DY",
          firstName = "COBBIE",
          middleNames = null,
          lastName = "FEDDER",
          releaseDate = LocalDate.parse("2023-07-15"),
          releaseType = "CRD",
          lastUpdatedDate = null,
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.FINANCE_AND_ID, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.HEALTH, status = Status.NOT_STARTED, lastDateChange = null),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = null,
          paroleEligibilityDate = null,
          releaseEligibilityDate = null,
          releaseEligibilityType = null,
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
        Prisoners(
          prisonerNumber = "A8257DY",
          firstName = "GLENN",
          middleNames = null,
          lastName = "MCGRATH",
          releaseDate = LocalDate.parse("2022-08-28"),
          releaseType = "CRD",
          lastUpdatedDate = null,
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.FINANCE_AND_ID, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.HEALTH, status = Status.NOT_STARTED, lastDateChange = null),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = LocalDate.parse("2022-05-07"),
          paroleEligibilityDate = LocalDate.parse("2023-04-28"),
          releaseEligibilityDate = LocalDate.parse("2022-05-07"),
          releaseEligibilityType = "HDCED",
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
        Prisoners(
          prisonerNumber = "A8314DY",
          firstName = "CHAIM",
          middleNames = null,
          lastName = "WITTKOPP",
          releaseDate = LocalDate.parse("2022-04-13"),
          releaseType = "CRD",
          lastUpdatedDate = null,
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.FINANCE_AND_ID, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.HEALTH, status = Status.NOT_STARTED, lastDateChange = null),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = LocalDate.parse("2021-12-07"),
          paroleEligibilityDate = null,
          releaseEligibilityDate = LocalDate.parse("2021-12-07"),
          releaseEligibilityType = "HDCED",
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
        Prisoners(
          prisonerNumber = "G6933GF",
          firstName = "BUSTER",
          middleNames = "CHRISTABERT HECTUR",
          lastName = "CORALLO",
          releaseDate = LocalDate.parse("2021-10-14"),
          releaseType = "CRD",
          lastUpdatedDate = null,
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.FINANCE_AND_ID, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.HEALTH, status = Status.NOT_STARTED, lastDateChange = null),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = null,
          paroleEligibilityDate = LocalDate.parse("2019-03-28"),
          releaseEligibilityDate = LocalDate.parse("2019-03-28"),
          releaseEligibilityType = "PED",
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
        Prisoners(
          prisonerNumber = "G6335VX",
          firstName = "GARRETT",
          middleNames = "SYLVANNA",
          lastName = "COUTCHER",
          releaseDate = LocalDate.parse("2017-05-11"),
          releaseType = "PRRD",
          lastUpdatedDate = null,
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.FINANCE_AND_ID, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.HEALTH, status = Status.NOT_STARTED, lastDateChange = null),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = null,
          paroleEligibilityDate = null,
          releaseEligibilityDate = null,
          releaseEligibilityType = null,
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
        Prisoners(
          prisonerNumber = "G6628UE",
          firstName = "ALLIE",
          middleNames = "MARCISHA",
          lastName = "COSNER",
          releaseDate = LocalDate.parse("2017-03-31"),
          releaseType = "CRD",
          lastUpdatedDate = null,
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.FINANCE_AND_ID, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.HEALTH, status = Status.NOT_STARTED, lastDateChange = null),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = LocalDate.parse("2017-01-01"),
          paroleEligibilityDate = LocalDate.parse("2016-08-28"),
          releaseEligibilityDate = LocalDate.parse("2016-08-28"),
          releaseEligibilityType = "PED",
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
        Prisoners(
          prisonerNumber = "A8132DY",
          firstName = "GEORGE",
          middleNames = null,
          lastName = "HAMILTON",
          releaseDate = LocalDate.parse("2015-07-23"),
          releaseType = "CRD",
          lastUpdatedDate = null,
          status = listOf(
            PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.FINANCE_AND_ID, status = Status.NOT_STARTED, lastDateChange = null),
            PathwayStatus(pathway = Pathway.HEALTH, status = Status.NOT_STARTED, lastDateChange = null),
          ),
          pathwayStatus = null,
          homeDetentionCurfewEligibilityDate = LocalDate.parse("2015-05-09"),
          paroleEligibilityDate = null,
          releaseEligibilityDate = LocalDate.parse("2015-05-09"),
          releaseEligibilityType = "HDCED",
          releaseOnTemporaryLicenceDate = null,
          assessmentRequired = true,
          needs = listOf(),
          lastReport = null,
        ),
      ),
      pageSize = 10,
      page = 0,
      sortName = "releaseDate,DESC",
      totalElements = 10,
      last = true,
    )
    Assertions.assertEquals(expectedPrisonerList, prisonersList)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending - 2`() {
    mockDatabaseCalls()

    val prisonId = "MDI"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-2.json")
    whenever(prisonerSearchApiService.findPrisonersByPrisonId(prisonId)).thenReturn(mockedJsonResponse.content)

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
        false,
        "123",
        null,
      )

    Assertions.assertEquals(getExpectedPrisonersListReleaseDateDesc(), prisoners)
  }

  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Ascending`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPrisonerId = "A8339DY"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-1.json")
    whenever(prisonerSearchApiService.findPrisonersByPrisonId(prisonId)).thenReturn(mockedJsonResponse.content)

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
        false,
        "123",
        null,
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
    whenever(prisonerSearchApiService.findPrisonersByPrisonId(prisonId)).thenReturn(mockedJsonResponse.content)

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
        false,
        "123",
        null,
      )
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json for page size`() {
    mockDatabaseCalls()

    val prisonId = "MDI"
    val expectedPageSize = 3

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-1.json")
    whenever(prisonerSearchApiService.findPrisonersByPrisonId(prisonId)).thenReturn(mockedJsonResponse.content)

    val prisonersList =
      prisonerService.getPrisonersByPrisonId("", prisonId, 0, null, null, null, 0, 5, "name,ASC", false, false, "123", null)
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
    whenever(prisonerSearchApiService.findPrisonersByPrisonId(prisonId)).thenReturn(mockedJsonResponse.content)
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
        false,
        "123",
        null,
      )
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @Test
  fun `test get PrisonersList happy path full json for pathwayView`() {
    mockDatabaseCallsForPathwayView()

    val prisonId = "MDI"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-2.json")
    whenever(prisonerSearchApiService.findPrisonersByPrisonId(prisonId)).thenReturn(mockedJsonResponse.content)

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
      false,
      "123",
      null,
    )

    Assertions.assertEquals(getExpectedPrisonersPathwayView(), prisoners)
  }

  @Test
  fun `test get PrisonersList happy path full json for pathwayView and pathwayStatus`() {
    mockDatabaseCallsForPathwayView()

    val prisonId = "MDI"

    val mockedJsonResponse: PrisonersSearchList = readFileAsObject("testdata/prisoner-search-api/prisoner-search-2.json")
    whenever(prisonerSearchApiService.findPrisonersByPrisonId(prisonId)).thenReturn(mockedJsonResponse.content)

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
      false,
      "123",
      null,
    )

    Assertions.assertEquals(getExpectedPrisonersPathwayViewAndPathwayStatus(), prisoners)
  }

  private fun mockDatabaseCalls(mockFindByPrison: Boolean = true) {
    val mockPathwayStatusEntities = listOf(
      aPrisonerWithStatusResult(
        prisonerId = 1,
        nomsId = "G1458GV",
        pathway = Pathway.ACCOMMODATION,
        pathwayStatus = Status.NOT_STARTED,
      ),
      aPrisonerWithStatusResult(
        prisonerId = 1,
        nomsId = "G1458GV",
        pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
        pathwayStatus = Status.NOT_STARTED,
      ),
      aPrisonerWithStatusResult(
        prisonerId = 2,
        nomsId = "A8339DY",
        pathway = Pathway.ACCOMMODATION,
        pathwayStatus = Status.NOT_STARTED,
      ),
      aPrisonerWithStatusResult(
        prisonerId = 2,
        nomsId = "A8339DY",
        pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
        pathwayStatus = Status.NOT_STARTED,
      ),
      aPrisonerWithStatusResult(
        prisonerId = 3,
        nomsId = "A8229DY",
        pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
        pathwayStatus = Status.NOT_STARTED,
      ),
      aPrisonerWithStatusResult(
        prisonerId = 3,
        nomsId = "A8229DY",
        pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
        pathwayStatus = Status.NOT_STARTED,
      ),
    )
    if (mockFindByPrison) {
      whenever(pathwayStatusRepository.findByPrison(any())).thenReturn(mockPathwayStatusEntities)
    }
  }

  private fun aPrisonerWithStatusResult(prisonerId: Long, nomsId: String, pathway: Pathway, pathwayStatus: Status) = object : PrisonerWithStatusProjection {
    override val prisonerId = prisonerId
    override val nomsId = nomsId
    override val pathway = pathway
    override var pathwayStatus = pathwayStatus
    override var updatedDate = LocalDateTime.now()
  }

  private fun mockDatabaseCallsForPathwayView() {
    val pathway = Pathway.ACCOMMODATION

    val status1 = Status.NOT_STARTED
    val status4 = Status.SUPPORT_DECLINED
    val status5 = Status.DONE

    whenever(
      resettlementAssessmentRepository.findPrisonersWithAllAssessmentsInStatus(
        "MDI",
        ResettlementAssessmentType.BCST2,
        ResettlementAssessmentStatus.SUBMITTED,
        Pathway.entries.size,
      ),
    ).thenReturn(setOf(1, 2, 3))

    whenever(pathwayStatusRepository.findByPrison("MDI")).thenReturn(
      listOf(
        aPrisonerWithStatusResult(prisonerId = 1, nomsId = "A8229DY", pathway = pathway, pathwayStatus = status1),
        aPrisonerWithStatusResult(prisonerId = 2, nomsId = "G1458GV", pathway = pathway, pathwayStatus = status4),
        aPrisonerWithStatusResult(prisonerId = 3, nomsId = "A8339DY", pathway = pathway, pathwayStatus = status5),
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
        needs = listOf(),
        lastReport = null,
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
        needs = listOf(),
        lastReport = null,
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
        needs = listOf(),
        lastReport = null,
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
        needs = listOf(),
        lastReport = null,
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
        needs = listOf(),
        lastReport = null,
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
        needs = listOf(),
        lastReport = null,
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
        needs = listOf(),
        lastReport = null,
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
    val actualPrisoners = prisonerService.objectMapper(prisoners, null, null, "MDI", null, false, "123", null)
    Assertions.assertEquals(prisonersMapped, actualPrisoners)
  }

  @ParameterizedTest
  @MethodSource("test filtering by workerId")
  fun `Prisoner List- filter by workerId`(workerId: String?, expectedPrisoners: List<Prisoners>) {
    val prisoners = createPrisonerSearchList()
    mockPathwayStatusEntities()
    whenever(caseAllocationService.getAllAssignedResettlementWorkers("MDI")).thenReturn(createCaseAllocationList())

    val actualPrisoners = prisonerService.objectMapper(prisoners, null, null, "MDI", null, false, "123", workerId)
    Assertions.assertEquals(expectedPrisoners.size, actualPrisoners.size)
    Assertions.assertEquals(expectedPrisoners, actualPrisoners)
  }

  private fun `test filtering by workerId`() = Stream.of(
    Arguments.of(
      "1",
      List(2) { i ->
        Prisoners(
          prisonerNumber = prisonerNumbers.getOrElse(i) { "A1$i" },
          firstName = "John$i",
          lastName = "Smith$i",
          assignedWorkerFirstname = "firstName1",
          assignedWorkerLastname = "lastName1",
          assessmentRequired = true,
          lastUpdatedDate = LocalDate.now(),
          pathwayStatus = null,
          status = listOf(PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = LocalDate.now())),
          needs = listOf(),
          lastReport = null,
        )
      },
    ),
    Arguments.of(
      "none",
      List(3) { i ->
        val number = i + 3
        Prisoners(
          prisonerNumber = "A1$number",
          firstName = "John$number",
          lastName = "Smith$number",
          assignedWorkerFirstname = null,
          assignedWorkerLastname = null,
          assessmentRequired = true,
          lastUpdatedDate = null,
          pathwayStatus = null,
          status = enumValues<Pathway>().map { pathway -> PathwayStatus(pathway = pathway, status = Status.NOT_STARTED) },
          needs = listOf(),
          lastReport = null,
        )
      },
    ),
    Arguments.of(
      null,
      List(6) { i ->
        Prisoners(
          prisonerNumber = prisonerNumbers.getOrElse(i) { "A1$i" },
          firstName = "John$i",
          lastName = "Smith$i",
          assignedWorkerFirstname = if (i < 2) "firstName1" else if (i == 2) "firstName5" else null,
          assignedWorkerLastname = if (i < 2) "lastName1" else if (i == 2) "lastName5" else null,
          assessmentRequired = true,
          lastUpdatedDate = if (i < 3) LocalDate.now() else null,
          pathwayStatus = null,
          status = if (i < 3) {
            listOf(PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()))
          } else {
            enumValues<Pathway>().map { pathway -> PathwayStatus(pathway = pathway, status = Status.NOT_STARTED) }
          },
          needs = listOf(),
          lastReport = null,
        )
      },
    ),
    Arguments.of(
      "",
      List(6) { i ->
        Prisoners(
          prisonerNumber = prisonerNumbers.getOrElse(i) { "A1$i" },
          firstName = "John$i",
          lastName = "Smith$i",
          assignedWorkerFirstname = if (i < 2) "firstName1" else if (i == 2) "firstName5" else null,
          assignedWorkerLastname = if (i < 2) "lastName1" else if (i == 2) "lastName5" else null,
          assessmentRequired = true,
          lastUpdatedDate = if (i < 3) LocalDate.now() else null,
          pathwayStatus = null,
          status = if (i < 3) {
            listOf(PathwayStatus(pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, lastDateChange = LocalDate.now()))
          } else {
            enumValues<Pathway>().map { pathway -> PathwayStatus(pathway = pathway, status = Status.NOT_STARTED) }
          },
          needs = listOf(),
          lastReport = null,
        )
      },
    ),
  )

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
      needs = listOf(),
      lastReport = null,
    )

  private fun createPrisonerName(firstName: String, lastName: String) =
    Prisoners(
      prisonerNumber = "A123456",
      firstName = firstName,
      lastName = lastName,
      pathwayStatus = null,
      assessmentRequired = true,
      needs = listOf(),
      lastReport = null,
    )

  private fun createPrisonerNameAndNumber(prisonerNumber: String, firstName: String, lastName: String) =
    Prisoners(
      prisonerNumber = prisonerNumber,
      firstName = firstName,
      lastName = lastName,
      pathwayStatus = null,
      assessmentRequired = true,
      needs = listOf(),
      lastReport = null,
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
    needs = listOf(),
    lastReport = null,
  )

  private fun createPrisonerReleaseDate(releaseDate: LocalDate?) = Prisoners(
    prisonerNumber = "A123456",
    firstName = "PATRICK",
    lastName = "WICKENDEN",
    pathwayStatus = null,
    releaseDate = releaseDate,
    assessmentRequired = true,
    needs = listOf(),
    lastReport = null,
  )

  private fun createPrisonerReleaseOnTempLicenceDate(releaseOnTempLicenceDate: LocalDate?) = Prisoners(
    prisonerNumber = "A123456",
    firstName = "PATRICK",
    lastName = "WICKENDEN",
    pathwayStatus = null,
    releaseOnTemporaryLicenceDate = releaseOnTempLicenceDate,
    assessmentRequired = true,
    needs = listOf(),
    lastReport = null,
  )

  private fun createPrisonerPathwayStatus(pathwayStatus: Status) =
    Prisoners(
      prisonerNumber = "A123456",
      firstName = "BORIS",
      lastName = "FRANKLIN",
      pathwayStatus = pathwayStatus,
      assessmentRequired = true,
      needs = listOf(),
      lastReport = null,
    )

  private fun createPrisonerLastUpdatedDate(pathwayStatus: Status, lastUpdatedDate: LocalDate?) = Prisoners(
    prisonerNumber = "A123456",
    firstName = "OLIVER",
    lastName = "HAYES",
    pathwayStatus = pathwayStatus,
    lastUpdatedDate = lastUpdatedDate,
    assessmentRequired = true,
    needs = listOf(),
    lastReport = null,
  )

  private inline fun <reified T> readFileAsObject(filename: String): T = readStringAsObject(readFile(filename))
  private inline fun <reified T> readStringAsObject(string: String): T = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerKotlinModule().registerModule(JavaTimeModule()).readValue(string)

  @ParameterizedTest(name = "{0}")
  @MethodSource("test processReleaseDateFiltering data")
  fun `test processReleaseDateFiltering`(desc: String, days: Int, prisoners: MutableList<PrisonersSearch>, includePastReleaseDates: Boolean, expectedList: List<PrisonersSearch>) {
    mockkStatic(LocalDate::class)
    every { LocalDate.now() } returns LocalDate.parse("2024-08-05")

    prisonerService.processReleaseDateFiltering(days, prisoners, includePastReleaseDates)
    Assertions.assertEquals(expectedList.map { it.displayReleaseDate }, prisoners.map { it.displayReleaseDate })

    unmockkAll()
  }

  private fun `test processReleaseDateFiltering data`() = Stream.of(
    // Note - "now" is set to 2024-08-05 in this test.
    Arguments.of(
      "No prisoners to filter",
      0,
      createPrisonerListByReleaseDates(),
      true,
      createPrisonerListByReleaseDates(),
    ),
    Arguments.of(
      "No filtering and only null",
      0,
      createPrisonerListByReleaseDates(null, null, null),
      true,
      createPrisonerListByReleaseDates(null, null, null),
    ),
    Arguments.of(
      "Only null and includePastReleaseDates set to false",
      0,
      createPrisonerListByReleaseDates(null, null, null),
      false,
      createPrisonerListByReleaseDates(null, null, null),
    ),
    Arguments.of(
      "Only null, includePastReleaseDates set to false, and 30 day filter",
      30,
      createPrisonerListByReleaseDates(null, null, null),
      false,
      createPrisonerListByReleaseDates(),
    ),
    Arguments.of(
      "Only null, includePastReleaseDates set to true, and 30 day filter",
      30,
      createPrisonerListByReleaseDates(null, null, null),
      true,
      createPrisonerListByReleaseDates(),
    ),
    Arguments.of(
      "No filtering",
      0,
      createPrisonerListByReleaseDates("2016-08-30", "2019-03-20", "2022-12-12", "2022-12-12", "2022-12-12", "2024-08-04", "2024-08-05", "2024-08-06", "2026-03-28", "2099-12-31", null, null),
      true,
      createPrisonerListByReleaseDates("2016-08-30", "2019-03-20", "2022-12-12", "2022-12-12", "2022-12-12", "2024-08-04", "2024-08-05", "2024-08-06", "2026-03-28", "2099-12-31", null, null),
    ),
    Arguments.of(
      "IncludePastReleaseDates set to false",
      0,
      createPrisonerListByReleaseDates("2016-08-30", "2019-03-20", "2022-12-12", "2022-12-12", "2022-12-12", "2024-08-04", "2024-08-05", "2024-08-06", "2026-03-28", "2099-12-31", null, null),
      false,
      createPrisonerListByReleaseDates("2024-08-05", "2024-08-06", "2026-03-28", "2099-12-31", null, null),
    ),
    Arguments.of(
      "Filter by 90 days and IncludePastReleaseDates set to false",
      90,
      createPrisonerListByReleaseDates("2016-08-30", "2019-03-20", "2022-12-12", "2024-06-12", "2024-07-12", "2024-08-04", "2024-08-05", "2024-08-06", "2024-11-06", "2024-11-03", "2025-02-28", "2099-12-31", null, null),
      false,
      createPrisonerListByReleaseDates("2024-08-05", "2024-08-06", "2024-11-03"),
    ),
    Arguments.of(
      "Filter by 90 days and IncludePastReleaseDates set to true",
      90,
      createPrisonerListByReleaseDates("2016-08-30", "2019-03-20", "2022-12-12", "2024-06-12", "2024-07-12", "2024-08-04", "2024-08-05", "2024-08-06", "2024-11-06", "2024-11-03", "2025-02-28", "2099-12-31", null, null),
      true,
      createPrisonerListByReleaseDates("2016-08-30", "2019-03-20", "2022-12-12", "2024-06-12", "2024-07-12", "2024-08-04", "2024-08-05", "2024-08-06", "2024-11-03"),
    ),
  )

  private fun createPrisonerListByReleaseDates(vararg releaseDates: String?) = releaseDates.map {
    PrisonersSearch(
      prisonerNumber = "A123456",
      firstName = "OLIVER",
      lastName = "HAYES",
      prisonId = "ABC",
      prisonName = "ABC Prison",
      youthOffender = false,
      cellLocation = null,
      displayReleaseDate = if (it != null) LocalDate.parse(it) else null,
    )
  }

  @Test
  fun `test getPrisonerReleaseDateByNomsId`() {
    val nomsId = "123"
    whenever(prisonerSearchApiService.findPrisonerPersonalDetails(nomsId)).thenReturn(
      PrisonersSearch(
        prisonerNumber = "A123456",
        firstName = "SIMON",
        lastName = "BAMFORD",
        prisonId = "MDI",
        prisonName = "Midlands",
        confirmedReleaseDate = LocalDate.parse("2024-01-09"),
      ),
    )
    Assertions.assertEquals(LocalDate.parse("2024-01-09"), prisonerService.getPrisonerReleaseDateByNomsId(nomsId))
  }

  val prisonerNumbers = listOf("G1458GV", "G1458GV", "A8339DY")

  private fun createPrisonerSearchList() = List(6) { i ->
    PrisonersSearch(
      prisonerNumber = prisonerNumbers.getOrElse(i) { "A1$i" },
      firstName = "John$i",
      lastName = "Smith$i",
      prisonId = "MDI",
      prisonName = "Midlands",
      cellLocation = "2A",
    )
  }

  private fun mockPathwayStatusEntities() {
    val mockPathwayStatusEntities = listOf(
      aPrisonerWithStatusResult(
        prisonerId = 1,
        nomsId = "G1458GV",
        pathway = Pathway.ACCOMMODATION,
        pathwayStatus = Status.NOT_STARTED,
      ),
      aPrisonerWithStatusResult(
        prisonerId = 2,
        nomsId = "A8339DY",
        pathway = Pathway.ACCOMMODATION,
        pathwayStatus = Status.NOT_STARTED,
      ),
      aPrisonerWithStatusResult(
        prisonerId = 3,
        nomsId = "A8229DY",
        pathway = Pathway.ACCOMMODATION,
        pathwayStatus = Status.NOT_STARTED,
      ),
    )
    whenever(pathwayStatusRepository.findByPrison(any())).thenReturn(mockPathwayStatusEntities)
  }

  private fun createCaseAllocationList() = List(4) { i ->
    CaseAllocationEntity(
      prisonerId = i.toLong(),
      staffId = if (i < 2) 1 else 5,
      staffFirstname = "firstName${if (i < 2) 1 else 5}",
      staffLastname = "lastName${if (i < 2) 1 else 5}",
    )
  }
}
