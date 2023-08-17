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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class PathwayApiServiceTest {

  private lateinit var pathwayApiService: PathwayApiService

  @Mock
  private lateinit var pathwayStatusRepository: PathwayStatusRepository
  @Mock
  private lateinit var prisonerRepository: PrisonerRepository
  @Mock
  private lateinit var pathwayRepository: PathwayRepository
  @Mock
  private lateinit var statusRepository: StatusRepository

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    pathwayApiService = PathwayApiService(pathwayStatusRepository, prisonerRepository, pathwayRepository, statusRepository)
  }

  @Test
  fun `test update pathway status`() {

    // Mock calls to LocalDateTime.now() so we can test the creationDate is being updated
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val nomsId = "abc"
    val pathwayEntity = PathwayEntity(1, "Accommodation", true, testDate)
    val newStatusEntity = StatusEntity(2, "In Progress", true, testDate)
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate)
    val oldStatusEntity = StatusEntity(1, "Not Started", true, testDate)
    val pathwayStatusEntity = PathwayStatusEntity(1, prisonerEntity, pathwayEntity, oldStatusEntity, testDate)

    Mockito.`when`(pathwayRepository.findById(Pathway.ACCOMMODATION.id)).thenReturn(Optional.of(pathwayEntity))
    Mockito.`when`(statusRepository.findById(Status.IN_PROGRESS.id)).thenReturn(Optional.of(newStatusEntity))
    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(pathwayStatusRepository.findByPathwayAndPrisoner(pathwayEntity, prisonerEntity)).thenReturn(pathwayStatusEntity)

    val expectedPathwayStatusEntity = PathwayStatusEntity(1, prisonerEntity, pathwayEntity, newStatusEntity, fakeNow)

    val response = pathwayApiService.updatePathwayStatus(nomsId, PathwayAndStatus(Pathway.ACCOMMODATION, Status.IN_PROGRESS))
    Assertions.assertEquals(HttpStatusCode.valueOf(200), response.statusCode)
    Mockito.verify(pathwayStatusRepository).save(expectedPathwayStatusEntity)

    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test update pathway status - prisoner not found`() {
    val nomsId = "abc"
    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(null)

    assertThrows<ResourceNotFoundException> { pathwayApiService.updatePathwayStatus(nomsId, PathwayAndStatus(Pathway.ACCOMMODATION, Status.IN_PROGRESS)) }
    Mockito.verify(pathwayStatusRepository, Mockito.never()).save(Mockito.any())
  }

  @Test
  fun `test update pathway status - pathway status not found`() {
    val nomsId = "abc"
    val pathwayEntity = PathwayEntity(1, "Accommodation", true, testDate)
    val newStatusEntity = StatusEntity(2, "In Progress", true, testDate)
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate)

    Mockito.`when`(pathwayRepository.findById(Pathway.ACCOMMODATION.id)).thenReturn(Optional.of(pathwayEntity))
    Mockito.`when`(statusRepository.findById(Status.IN_PROGRESS.id)).thenReturn(Optional.of(newStatusEntity))
    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(pathwayStatusRepository.findByPathwayAndPrisoner(pathwayEntity, prisonerEntity)).thenReturn(null)

    assertThrows<ResourceNotFoundException> { pathwayApiService.updatePathwayStatus(nomsId, PathwayAndStatus(Pathway.ACCOMMODATION, Status.IN_PROGRESS)) }
    Mockito.verify(pathwayStatusRepository, Mockito.never()).save(Mockito.any())
  }
}