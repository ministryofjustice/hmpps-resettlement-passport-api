package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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

@OptIn(ExperimentalCoroutinesApi::class)
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
  fun `test getIdApplicationByNomsId - returns id application`() = runTest {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn")
    Mockito.`when`(prisonerRepository.findByNomsId(prisonerEntity.nomsId)).thenReturn(prisonerEntity)
    val idApplicationEntity = IdApplicationEntity(
      null,
      prisonerEntity,
      IdTypeEntity(1, "Drivers Licence"),
      fakeNow,
      fakeNow,
      isPriorityApplication = false,
      BigDecimal(10.00),
    )
    Mockito.`when`(idApplicationRepository.findByPrisonerAndIsDeleted(prisonerEntity)).thenReturn(idApplicationEntity)
    val result = idApplicationService.getIdApplicationByNomsId(prisonerEntity.nomsId)
    Assertions.assertEquals(idApplicationEntity, result)
  }

  @Test
  fun `test createIdApplication - creates and returns idAppliaction`() = runTest {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn")
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
      id = null,
      prisoner = prisonerEntity,
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
  fun `test updateIdApplication - updates and returns idAppliaction`() = runTest {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn")
    val idTypeEntity = IdTypeEntity(1, "Drivers Licence")
    val idApplicationPatchDTO = IdApplicationPatch(
      status = "Approved",
      dateIdReceived = fakeNow,
      isAddedToPersonalItems = false,
    )

    val idApplicationEntity = IdApplicationEntity(
      id = null,
      prisoner = prisonerEntity,
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
      id = null,
      prisoner = prisonerEntity,
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
  fun `test deleteIdApplication - sets is deleted and deletion date`() = runTest {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn")
    val idTypeEntity = IdTypeEntity(1, "Drivers Licence")
    val idApplicationEntity = IdApplicationEntity(
      id = null,
      prisoner = prisonerEntity,
      idType = idTypeEntity,
      creationDate = fakeNow,
      applicationSubmittedDate = fakeNow,
      isPriorityApplication = false,
      costOfApplication = BigDecimal(10.00),
      deletionDate = null,
      isDeleted = false,
    )
    val expectedDeleteCall = IdApplicationEntity(
      id = null,
      prisoner = prisonerEntity,
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
}