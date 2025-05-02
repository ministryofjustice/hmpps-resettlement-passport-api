package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocationCountResponseImp
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocationPostResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.manageusersapi.ManageUser
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearchList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ManageUsersApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class CaseAllocationServiceTest {
  private lateinit var caseAllocationService: CaseAllocationService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var caseAllocationRepository: CaseAllocationRepository

  @Mock
  private lateinit var manageUserApiService: ManageUsersApiService

  @Mock
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @Mock
  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    caseAllocationService = CaseAllocationService(prisonerRepository, caseAllocationRepository, manageUserApiService, prisonerSearchApiService, pathwayAndStatusService, telemetryClient)
  }

  @Test
  fun `test createCaseAllocation - creates and returns caseAllocation`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken("auth", "sub") } returns "USERNAME"

    val manageUserList = emptyList<ManageUser>().toMutableList()
    val manageUser = ManageUser(4321, "PSO Firstname", "PSO Lastname")
    manageUserList.add(manageUser)

    whenever(manageUserApiService.getManageUsersData("MDI", "PSFR_RESETTLEMENT_WORKER")).thenReturn(manageUserList)
    val prisonerEntity = PrisonerEntity(1, "123", testDate, "MDI")
    whenever(pathwayAndStatusService.getOrCreatePrisoner("123", "MDI")).thenReturn(prisonerEntity)
    Mockito.`when`(caseAllocationRepository.findByPrisonerIdAndIsDeleted(prisonerEntity.id(), false)).thenReturn(null)

    val caseList = emptyList<CaseAllocationPostResponse?>().toMutableList()

    val caseAllocationPost = CaseAllocation(
      nomsIds = arrayOf("123"),
      staffId = 4321,
      prisonId = "MDI",
    )

    val caseAllocationEntity = CaseAllocationEntity(
      prisonerId = prisonerEntity.id(),
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
      creationDate = fakeNow,
      isDeleted = false,
      deletionDate = null,
    )
    val caseAllocationPostResponse = CaseAllocationPostResponse(
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
      nomsId = "123",
    )
    val caseAllocationEntity2 = CaseAllocationEntity(
      prisonerId = prisonerEntity.id(),
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
      creationDate = fakeNow,
      isDeleted = false,
      deletionDate = null,
    )
    val caseAllocationList = emptyList<CaseAllocationEntity>().toMutableList()
    caseAllocationList.add(caseAllocationEntity)
    caseAllocationList.add(caseAllocationEntity2)
    Mockito.`when`(caseAllocationRepository.save(any())).thenReturn(caseAllocationEntity)
    val result = caseAllocationService.assignCase(caseAllocationPost, "auth")
    Mockito.verify(caseAllocationRepository).save(caseAllocationEntity)
    caseList.add(caseAllocationPostResponse)
    Assertions.assertEquals(caseList, result)
    Mockito.verify(telemetryClient).trackEvent("PSFR_CaseAllocation", mapOf("prisonId" to "MDI", "prisonerId" to "123", "allocatedToStaffId" to "4321", "allocatedByUsername" to "USERNAME"), null)
    unmockkAll()
  }

  @Test
  fun `test removeCaseAllocation - unassign the allocation`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken("auth", "sub") } returns "USERNAME"

    val caseList = emptyList<CaseAllocationEntity?>().toMutableList()
    val prisonerEntity = PrisonerEntity(1, "123", testDate, "xyz")
    val caseAllocationEntity = CaseAllocationEntity(
      prisonerId = prisonerEntity.id(),
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
      creationDate = fakeNow,
      isDeleted = false,
      deletionDate = null,
    )

    val expectedCaseAllocationEntity = CaseAllocationEntity(
      prisonerId = prisonerEntity.id(),
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
      creationDate = fakeNow,
      isDeleted = true,
      deletionDate = fakeNow,
    )
    caseList.add(expectedCaseAllocationEntity)
    val caseAllocation = CaseAllocation(
      nomsIds = arrayOf("123"),
    )
    Mockito.`when`(prisonerRepository.findByNomsId(prisonerEntity.nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(caseAllocationRepository.findByPrisonerIdAndIsDeleted(prisonerEntity.id(), false)).thenReturn(caseAllocationEntity)
    val result = caseAllocationService.unAssignCase(caseAllocation, "auth")

    Mockito.verify(caseAllocationRepository).save(expectedCaseAllocationEntity)
    Assertions.assertEquals(caseList, result)
    Mockito.verify(telemetryClient).trackEvent(
      "PSFR_CaseUnallocation",
      mapOf(
        "prisonId" to prisonerEntity.prisonId,
        "prisonerId" to prisonerEntity.nomsId,
        "unallocatedByUsername" to "USERNAME",
      ),
      null,
    )
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test getAllCaseAllocationByStaffId - returns case allocations`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val prisonerEntity1 = PrisonerEntity(2, "acb", testDate, "xyz")
    val caseAllocationEntity = CaseAllocationEntity(
      prisonerId = prisonerEntity.id(),
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
      creationDate = fakeNow,
      isDeleted = false,
      deletionDate = null,
    )
    val caseAllocationEntity2 = CaseAllocationEntity(
      prisonerId = prisonerEntity1.id(),
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
      creationDate = fakeNow,
      isDeleted = false,
      deletionDate = null,
    )

    val caseAllocationList = emptyList<CaseAllocationEntity>().toMutableList()
    caseAllocationList.add(caseAllocationEntity)
    caseAllocationList.add(caseAllocationEntity2)

    Mockito.`when`(caseAllocationRepository.findByStaffIdAndIsDeleted(4321, false)).thenReturn(caseAllocationList)
    val result = caseAllocationService.getAllCaseAllocationByStaffId(4321)
    Assertions.assertEquals(caseAllocationList, result)
  }

  @Test
  fun `test getAllCaseAllocationCount - returns case allocations count`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val prisonerEntity1 = PrisonerEntity(2, "acb", testDate, "xyz")
    val prisonerEntity2 = PrisonerEntity(3, "acb", testDate, "xyz")
    val prisonId = "MDI"
    val caseAllocationEntity = CaseAllocationEntity(
      prisonerId = prisonerEntity.id(),
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
      creationDate = fakeNow,
      isDeleted = false,
      deletionDate = null,
    )
    val caseAllocationEntity2 = CaseAllocationEntity(
      prisonerId = prisonerEntity1.id(),
      staffId = 4321,
      staffFirstname = "PSO1 Firstname",
      staffLastname = "PSO1 Lastname",
      creationDate = fakeNow.plusHours(1),
      isDeleted = false,
      deletionDate = null,
    )
    val caseAllocationEntity3 = CaseAllocationEntity(
      prisonerId = prisonerEntity2.id(),
      staffId = 4444,
      staffFirstname = "PSO2 Firstname",
      staffLastname = "PSO2 Lastname",
      creationDate = fakeNow,
      isDeleted = false,
      deletionDate = null,
    )
    val caseAllocationList = emptyList<CaseAllocationEntity>().toMutableList()
    caseAllocationList.add(caseAllocationEntity)
    caseAllocationList.add(caseAllocationEntity2)
    caseAllocationList.add(caseAllocationEntity3)

    // Mockito.`when`(caseAllocationRepository.findByStaffIdAndIsDeleted(4321, false)).thenReturn(caseAllocationList)
    val caseAllocationCountTestResponse1 = CaseAllocationCountResponseImp(
      4321,
      "PSO1 Firstname",
      "PSO1 Lastname",
      2,
    )
    val caseAllocationCountTestResponse2 = CaseAllocationCountResponseImp(
      4444,
      "PSO2 Firstname",
      "PSO2 Lastname",
      1,
    )

    val caseAllocationCountTestResponseList = emptyList<CaseAllocationCountResponseImp>().toMutableList()
    caseAllocationCountTestResponseList.add(caseAllocationCountTestResponse1)
    caseAllocationCountTestResponseList.add(caseAllocationCountTestResponse2)
    Mockito.`when`(caseAllocationRepository.findCaseCountByPrisonId(prisonId))
      .thenReturn(caseAllocationCountTestResponseList)
    Mockito.`when`(caseAllocationRepository.findTotalCaseCountByPrisonId(prisonId)).thenReturn(3)
    val mockedJsonResponse: PrisonersSearchList =
      readFileAsObject("testdata/prisoner-search-api/prisoner-search-1.json")
    whenever(prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "")).thenReturn(mockedJsonResponse.content)
    val result = caseAllocationService.getCasesAllocationCount(prisonId)
    Assertions.assertEquals(caseAllocationCountTestResponseList[0].staffId, result.assignedList[0]?.staffId)
    Assertions.assertEquals(7, result.unassignedCount)
  }

  @Test
  fun `test getCaseAllocationHistoryByPrisonerId should return data from repository`() {
    val data = listOf(
      CaseAllocationEntity(
        prisonerId = 1,
        staffId = 4321,
        staffFirstname = "PSO Firstname",
        staffLastname = "PSO Lastname",
        creationDate = fakeNow,
        isDeleted = false,
        deletionDate = null,
      ),
    )
    Mockito.`when`(caseAllocationRepository.findByPrisonerIdAndCreationDateBetween(any(), any(), any()))
      .thenReturn(data)

    val result = caseAllocationService.getCaseAllocationHistoryByPrisonerId(1, LocalDateTime.now(), LocalDateTime.now())

    Assertions.assertEquals(data, result)
  }

  private inline fun <reified T> readFileAsObject(filename: String): T = readStringAsObject(readFile(filename))
  private inline fun <reified T> readStringAsObject(string: String): T = jacksonObjectMapper().configure(
    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
    false,
  ).registerKotlinModule().registerModule(JavaTimeModule()).readValue(string)
}
