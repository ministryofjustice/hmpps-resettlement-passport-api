package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ArnApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
class RiskServiceTest {

  private lateinit var riskService: RiskService

  @Mock
  private lateinit var arnApiService: ArnApiService

  @Mock
  private lateinit var resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService

  @BeforeEach
  fun beforeEach() {
    riskService = RiskService(arnApiService, resettlementPassportDeliusApiService)
  }

  @Test
  fun `get risk scores - test missing crn from database`() = runTest {
    val nomsId = "ABC1234"
    Mockito.`when`(resettlementPassportDeliusApiService.findCrn(nomsId)).thenReturn(null)
    assertThrows<ResourceNotFoundException> { riskService.getRiskScoresByNomsId(nomsId) }
  }

  @Test
  fun `get RoSH data - test missing crn from database`() = runTest {
    val nomsId = "ABC1234"
    Mockito.`when`(resettlementPassportDeliusApiService.findCrn(nomsId)).thenReturn(null)
    assertThrows<ResourceNotFoundException> { riskService.getRoshDataByNomsId(nomsId) }
  }
}