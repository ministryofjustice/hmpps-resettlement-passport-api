package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.SupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.SupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class AdminServiceTest {
  private lateinit var adminService: AdminService

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @Mock
  private lateinit var caseAllocationService: CaseAllocationService

  @Mock
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @Mock
  private lateinit var prisonerService: PrisonerService

  @Mock
  private lateinit var supportNeedRepository: SupportNeedRepository

  companion object {

    private val fakeNow = LocalDateTime.parse("2024-07-02T12:12:12")

    @JvmStatic
    @BeforeAll
    fun beforeAll() {
      mockkStatic(LocalDateTime::class)
      every { LocalDateTime.now() } returns fakeNow
    }

    @JvmStatic
    @AfterAll
    fun afterAll() {
      unmockkAll()
    }
  }

  @BeforeEach
  fun beforeEach() {
    adminService = AdminService(telemetryClient, caseAllocationService, prisonerSearchApiService, prisonerService, supportNeedRepository)
  }

  @Test
  fun `test sendMetricsToAppInsights`() {
    whenever(prisonerService.getPrisonList()).thenReturn(listOf("AAI", "BBI", "CCI", "DDI"))

    whenever(caseAllocationService.getNumberOfAssignedPrisoners("AAI")).thenReturn(5)
    whenever(caseAllocationService.getNumberOfAssignedPrisoners("BBI")).thenReturn(10)
    whenever(caseAllocationService.getNumberOfAssignedPrisoners("CCI")).thenReturn(7)
    whenever(caseAllocationService.getNumberOfAssignedPrisoners("DDI")).thenReturn(0)

    whenever(prisonerSearchApiService.findPrisonersBySearchTerm("AAI", "")).thenReturn(getMocks(10))
    whenever(prisonerSearchApiService.findPrisonersBySearchTerm("BBI", "")).thenReturn(getMocks(35))
    whenever(prisonerSearchApiService.findPrisonersBySearchTerm("CCI", "")).thenReturn(getMocks(102))
    whenever(prisonerSearchApiService.findPrisonersBySearchTerm("DDI", "")).thenThrow(RuntimeException("Something went wrong"))

    adminService.sendMetricsToAppInsights()

    verify(telemetryClient).trackMetric("case_allocation_assigned_prisoners_percentage", 50.0, null, null, null, null, mapOf("prisonId" to "AAI", "numberOfPrisonersAssigned" to "5", "totalNumberOfPrisoners" to "10"))
    verify(telemetryClient).trackMetric("case_allocation_assigned_prisoners_percentage", 28.57142857142857, null, null, null, null, mapOf("prisonId" to "BBI", "numberOfPrisonersAssigned" to "10", "totalNumberOfPrisoners" to "35"))
    verify(telemetryClient).trackMetric("case_allocation_assigned_prisoners_percentage", 6.862745098039216, null, null, null, null, mapOf("prisonId" to "CCI", "numberOfPrisonersAssigned" to "7", "totalNumberOfPrisoners" to "102"))
    verifyNoMoreInteractions(telemetryClient)
  }

  private fun getMocks(num: Int): List<PrisonersSearch> {
    val list = mutableListOf<PrisonersSearch>()
    repeat(num) {
      list.add(Mockito.mock(PrisonersSearch::class.java))
    }
    return list
  }
}
