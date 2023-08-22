package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.google.common.io.Resources
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

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
    offenderSearchApiService = OffenderSearchApiService(pathwayRepository, prisonerRepository, pathwayStatusRepository, webClient, webClient, pathwayApiService)
    mockDatabaseCalls()
  }

  @AfterEach
  fun afterEach() {
    mockWebServer.shutdown()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6933GF"

    val mockedJsonResponse = Resources.getResource("testdata/prisoners/prisoner-offender-search.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId(false, "", prisonId, 0, 0, 10, "releaseDate,DESC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Ascending`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6933GF"

    val mockedJsonResponse = Resources.getResource("testdata/prisoners/prisoner-offender-search.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId(false, "", prisonId, 0, 0, 10, "releaseDate,ASC")
    Assertions.assertEquals(
      expectedPrisonerId,
      prisonersList.content?.get((prisonersList.content!!.toList().size - 1))?.prisonerNumber
        ?: 0,
    )
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get PrisonersList happy path full json with sort firstName Ascending`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6628UE"

    val mockedJsonResponse = Resources.getResource("testdata/prisoners/prisoner-offender-search.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId(false, "", prisonId, 0, 0, 10, "firstName,ASC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get PrisonersList happy path full json for page size`() = runTest {
    val prisonId = "MDI"
    val expectedPageSize = 5

    val mockedJsonResponse = Resources.getResource("testdata/prisoners/prisoner-offender-search.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId(false, "", prisonId, 0, 0, 5, "firstName,ASC")
    Assertions.assertEquals(expectedPageSize, prisonersList.pageSize)
    prisonersList.content?.toList()?.let { Assertions.assertEquals(expectedPageSize, it.size) }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get PrisonersList happy path full json with date Range data and sort by release date desc`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6933GF"

    val mockedJsonResponse = Resources.getResource("testdata/prisoners/prisoner-offender-search.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId(true, "", prisonId, 1095, 0, 10, "releaseDate,DESC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
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
}
