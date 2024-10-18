package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatusCode
import org.springframework.transaction.support.TransactionOperations
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PathwayAndStatusServiceTest {

  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Mock
  private lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    pathwayAndStatusService = PathwayAndStatusService(
      pathwayStatusRepository = pathwayStatusRepository,
      prisonerRepository = prisonerRepository,
      resettlementPassportDeliusApiService = resettlementPassportDeliusApiService,
      transactionOperations = TransactionOperations.withoutTransaction(),
    )
  }

  @Test
  fun `test update pathway status`() {
    // Mock calls to LocalDateTime.now() so we can test the updatedDate is being updated
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val nomsId = "abc"
    val crn = "crn1"
    val prisonId = "xyz"
    val pathway = Pathway.ACCOMMODATION
    val newStatus = Status.IN_PROGRESS
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, crn, prisonId)
    val oldStatus = Status.NOT_STARTED
    val pathwayStatusEntity = PathwayStatusEntity(1, prisonerEntity.id(), pathway, oldStatus, testDate)

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(pathwayStatusRepository.findByPathwayAndPrisonerId(pathway, prisonerEntity.id())).thenReturn(pathwayStatusEntity)

    val expectedPathwayStatusEntity = PathwayStatusEntity(1, prisonerEntity.id(), pathway, newStatus, fakeNow)

    val response = pathwayAndStatusService.updatePathwayStatus(nomsId, PathwayAndStatus(Pathway.ACCOMMODATION, Status.IN_PROGRESS))
    Assertions.assertEquals(HttpStatusCode.valueOf(200), response.statusCode)
    Mockito.verify(pathwayStatusRepository).save(expectedPathwayStatusEntity)

    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test update pathway status - prisoner not found`() {
    val nomsId = "abc"
    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(null)

    assertThrows<ResourceNotFoundException> { pathwayAndStatusService.updatePathwayStatus(nomsId, PathwayAndStatus(Pathway.ACCOMMODATION, Status.IN_PROGRESS)) }
    Mockito.verify(pathwayStatusRepository, Mockito.never()).save(Mockito.any())
  }

  @Test
  fun `test update pathway status - pathway status not found`() {
    val nomsId = "abc"
    val crn = "crn1"
    val prisonId = "xyz1"
    val pathway = Pathway.ACCOMMODATION
    val newStatus = Status.IN_PROGRESS
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, crn, prisonId)

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(pathwayStatusRepository.findByPathwayAndPrisonerId(pathway, prisonerEntity.id())).thenReturn(null)

    assertThrows<ResourceNotFoundException> { pathwayAndStatusService.updatePathwayStatus(nomsId, PathwayAndStatus(pathway, newStatus)) }
    Mockito.verify(pathwayStatusRepository, Mockito.never()).save(Mockito.any())
  }
}
