package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class CaseAllocationServiceTest {
  private lateinit var caseAllocationService: CaseAllocationService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var caseAllocationRepository: CaseAllocationRepository

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    caseAllocationService = CaseAllocationService(prisonerRepository, caseAllocationRepository)
  }

  @Test
  fun `test createCaseAllocation - creates and returns caseAllocation`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val caseList = emptyList<CaseAllocationEntity?>().toMutableList()
    val prisonerEntity = PrisonerEntity(1, "123", testDate, "crn", "xyz")
    val caseAllocationPost = CaseAllocation(
      nomsIds = arrayOf("123"),
      staffId = 4321,
      staffFirstName = "PSO Firstname",
      staffLastName = "PSO Lastname",
    )
    Mockito.`when`(prisonerRepository.findByNomsId(prisonerEntity.nomsId)).thenReturn(prisonerEntity)

    val caseAllocationEntity = CaseAllocationEntity(
      prisonerId = prisonerEntity.id(),
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
      creationDate = fakeNow,
      isDeleted = false,
      deletionDate = null,
    )
    caseList.add(caseAllocationEntity)
    Mockito.`when`(caseAllocationRepository.findByPrisonerIdAndIsDeleted(prisonerEntity.id(), false)).thenReturn(null)
    Mockito.`when`(caseAllocationRepository.save(any())).thenReturn(caseAllocationEntity)
    val result = caseAllocationService.assignCase(caseAllocationPost)
    Mockito.verify(caseAllocationRepository).save(caseAllocationEntity)
    Assertions.assertEquals(caseList, result)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test removeCaseAllocation - unassign the allocation`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val caseList = emptyList<CaseAllocationEntity?>().toMutableList()
    val prisonerEntity = PrisonerEntity(1, "123", testDate, "crn", "xyz")
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
    val result = caseAllocationService.unAssignCase(caseAllocation)

    Mockito.verify(caseAllocationRepository).save(expectedCaseAllocationEntity)
    Assertions.assertEquals(caseList, result)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test getAllCaseAllocationByStaffId - returns case allocations`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz")
    val prisonerEntity1 = PrisonerEntity(2, "acb", testDate, "crn", "xyz")
//    Mockito.`when`(prisonerRepository.findByNomsId(prisonerEntity.nomsId)).thenReturn(prisonerEntity)
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
}
