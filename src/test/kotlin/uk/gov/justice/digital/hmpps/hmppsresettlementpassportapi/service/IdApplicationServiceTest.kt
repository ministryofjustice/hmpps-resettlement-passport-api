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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.IdApplicationPatch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.IdApplicationPost
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdTypeEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.IdApplicationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.IdTypeRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class IdApplicationServiceTest {
  private lateinit var idApplicationService: IdApplicationService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var idApplicationRepository: IdApplicationRepository

  @Mock
  private lateinit var idTypeRepository: IdTypeRepository
  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    idApplicationService = IdApplicationService(idApplicationRepository, prisonerRepository, idTypeRepository)
  }

  @Test
  fun `test createIdApplication - creates and returns idApplication`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val idTypeEntity = IdTypeEntity(1, "Drivers Licence")
    val idApplicationPost = IdApplicationPost(
      idType = "Drivers licence",
      applicationSubmittedDate = fakeNow,
      costOfApplication = BigDecimal(10.00),
      isPriorityApplication = false,
      driversLicenceType = "Full",
      driversLicenceApplicationMadeAt = "Post office",
    )
    Mockito.`when`(prisonerRepository.findByNomsId(prisonerEntity.nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(idTypeRepository.findByName("Drivers licence")).thenReturn(idTypeEntity)

    val idApplicationEntity = IdApplicationEntity(
      prisonerId = prisonerEntity.id(),
      idType = idTypeEntity,
      creationDate = fakeNow,
      applicationSubmittedDate = fakeNow,
      isPriorityApplication = false,
      costOfApplication = BigDecimal(10.00),
      refundAmount = null,
      haveGro = null,
      isUkNationalBornOverseas = null,
      countryBornIn = null,
      caseNumber = null,
      courtDetails = null,
      driversLicenceType = "Full",
      driversLicenceApplicationMadeAt = "Post office",
      isAddedToPersonalItems = null,
      addedToPersonalItemsDate = null,
      status = "pending",
      statusUpdateDate = null,
      isDeleted = false,
      deletionDate = null,
      dateIdReceived = null,
    )
    Mockito.`when`(idApplicationRepository.save(any())).thenReturn(idApplicationEntity)
    val result = idApplicationService.createIdApplication(idApplicationPost, prisonerEntity.nomsId)
    Mockito.verify(idApplicationRepository).save(idApplicationEntity)
    Assertions.assertEquals(idApplicationEntity, result)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test updateIdApplication - updates and returns idAppliaction`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val idTypeEntity = IdTypeEntity(1, "Drivers Licence")
    val idApplicationPatchDTO = IdApplicationPatch(
      status = "Approved",
      dateIdReceived = fakeNow,
      isAddedToPersonalItems = false,
    )

    val idApplicationEntity = IdApplicationEntity(
      prisonerId = prisonerEntity.id(),
      idType = idTypeEntity,
      creationDate = fakeNow,
      applicationSubmittedDate = fakeNow,
      isPriorityApplication = false,
      costOfApplication = BigDecimal(10.00),
      refundAmount = null,
      haveGro = null,
      isUkNationalBornOverseas = null,
      countryBornIn = null,
      caseNumber = null,
      courtDetails = null,
      driversLicenceType = "Full",
      driversLicenceApplicationMadeAt = "Post office",
      isAddedToPersonalItems = null,
      addedToPersonalItemsDate = null,
      status = "pending",
      statusUpdateDate = null,
      isDeleted = false,
      deletionDate = null,
      dateIdReceived = null,
    )
    val expectedIdAssessment = IdApplicationEntity(
      prisonerId = prisonerEntity.id(),
      idType = idTypeEntity,
      creationDate = fakeNow,
      applicationSubmittedDate = fakeNow,
      isPriorityApplication = false,
      costOfApplication = BigDecimal(10.00),
      refundAmount = null,
      haveGro = null,
      isUkNationalBornOverseas = null,
      countryBornIn = null,
      caseNumber = null,
      courtDetails = null,
      driversLicenceType = "Full",
      driversLicenceApplicationMadeAt = "Post office",
      isAddedToPersonalItems = false,
      addedToPersonalItemsDate = null,
      status = "Approved",
      statusUpdateDate = fakeNow,
      isDeleted = false,
      deletionDate = null,
      dateIdReceived = fakeNow,
    )

    Mockito.`when`(idApplicationRepository.save(any())).thenReturn(expectedIdAssessment)
    val result = idApplicationService.updateIdApplication(idApplicationEntity, idApplicationPatchDTO)
    Mockito.verify(idApplicationRepository).save(expectedIdAssessment)
    Assertions.assertEquals(expectedIdAssessment, result)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test deleteIdApplication - sets is deleted and deletion date`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val idTypeEntity = IdTypeEntity(1, "Drivers Licence")
    val idApplicationEntity = IdApplicationEntity(
      prisonerId = prisonerEntity.id(),
      idType = idTypeEntity,
      creationDate = fakeNow,
      applicationSubmittedDate = fakeNow,
      isPriorityApplication = false,
      costOfApplication = BigDecimal(10.00),
      deletionDate = null,
      isDeleted = false,
    )
    val expectedDeleteCall = IdApplicationEntity(
      prisonerId = prisonerEntity.id(),
      idType = idTypeEntity,
      creationDate = fakeNow,
      applicationSubmittedDate = fakeNow,
      isPriorityApplication = false,
      costOfApplication = BigDecimal(10.00),
      deletionDate = fakeNow,
      isDeleted = true,
    )

    idApplicationService.deleteIdApplication(idApplicationEntity)

    Mockito.verify(idApplicationRepository).save(expectedDeleteCall)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test getAllIdApplicationByNomsId - returns id application`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    Mockito.`when`(prisonerRepository.findByNomsId(prisonerEntity.nomsId)).thenReturn(prisonerEntity)
    val idApplicationEntity1 = IdApplicationEntity(
      prisonerId = prisonerEntity.id(),
      idType = IdTypeEntity(1, "Birth Certificate"),
      creationDate = fakeNow,
      applicationSubmittedDate = fakeNow,
      isPriorityApplication = false,
      costOfApplication = BigDecimal(10.00),
    )
    val idApplicationEntity2 = IdApplicationEntity(
      prisonerId = prisonerEntity.id(),
      idType = IdTypeEntity(1, "Marriage Certificate"),
      creationDate = fakeNow,
      applicationSubmittedDate = fakeNow,
      isPriorityApplication = false,
      costOfApplication = BigDecimal(10.00),
    )
    val idApplicationEntityList = emptyList<IdApplicationEntity>().toMutableList()
    idApplicationEntityList.add(idApplicationEntity1)
    idApplicationEntityList.add(idApplicationEntity2)

    Mockito.`when`(idApplicationRepository.findByPrisonerIdAndIsDeleted(prisonerEntity.id())).thenReturn(idApplicationEntityList)
    val result = idApplicationService.getAllIdApplicationsByNomsId(prisonerEntity.nomsId)
    Assertions.assertEquals(idApplicationEntityList, result)
  }

  @Test
  fun `test getIdApplicationByPrisonerIdAndCreationDate should return from repository`() {
    val idApplication = IdApplicationEntity(
      prisonerId = 1,
      idType = IdTypeEntity(1, "Birth Certificate"),
      creationDate = fakeNow,
      applicationSubmittedDate = fakeNow,
      isPriorityApplication = false,
      costOfApplication = BigDecimal(10.00),
    )

    Mockito.`when`(idApplicationRepository.findByPrisonerIdAndCreationDateBetween(any(), any(), any())).thenReturn(listOf(idApplication))

    val response = idApplicationService.getIdApplicationByPrisonerIdAndCreationDate(1, LocalDateTime.now(), LocalDateTime.now())
    Assertions.assertEquals(listOf(idApplication), response)
  }
}
