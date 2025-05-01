package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.DuplicateDataFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplication
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplicationLog
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplicationResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationStatusLogEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.BankApplicationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.BankApplicationStatusLogRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.collections.emptyList

@ExtendWith(MockitoExtension::class)
class BankApplicationServiceTest {
  private lateinit var bankApplicationService: BankApplicationService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var bankApplicationRepository: BankApplicationRepository

  @Mock
  private lateinit var bankApplicationStatusLogRepository: BankApplicationStatusLogRepository
  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    bankApplicationService = BankApplicationService(prisonerRepository, bankApplicationRepository, bankApplicationStatusLogRepository)
  }

  @Test
  fun `test getBankApplicationById - returns bank application`() {
    val bankApplicationEntity = BankApplicationEntity(1, 2, emptySet(), fakeNow, fakeNow, status = "Pending", bankName = "Lloyds")
    whenever(bankApplicationRepository.findById(1)).thenReturn(Optional.of(bankApplicationEntity))

    val response = bankApplicationService.getBankApplicationById(1).get()

    Assertions.assertEquals(bankApplicationEntity, response)
  }

  @Test
  fun `test getBankApplicationByPrisoner - returns bank application`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val bankApplicationEntity = BankApplicationEntity(1, prisonerEntity.id(), emptySet(), fakeNow, fakeNow, status = "Pending", isDeleted = false, bankName = "Lloyds")
    val expectedResult = BankApplicationResponse(
      id = 1,
      prisoner = prisonerEntity,
      logs = emptyList(),
      applicationSubmittedDate = fakeNow,
      currentStatus = "Pending",
      bankResponseDate = null,
      isAddedToPersonalItems = null,
      addedToPersonalItemsDate = null,
      bankName = "Lloyds",
    )
    whenever(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    whenever(bankApplicationRepository.findByPrisonerIdAndIsDeleted(any(), any())).thenReturn(bankApplicationEntity)

    val response = bankApplicationService.getBankApplicationByNomsId(prisonerEntity.nomsId)

    Assertions.assertEquals(expectedResult, response)
  }

  @Test
  fun `test getBankApplicationByPrisoner - throws if not found`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    whenever(prisonerRepository.findByNomsId(prisonerEntity.nomsId)).thenReturn(prisonerEntity)
    whenever(bankApplicationRepository.findByPrisonerIdAndIsDeleted(any(), any())).thenReturn(null)

    assertThrows<ResourceNotFoundException> { bankApplicationService.getBankApplicationByNomsId(prisonerEntity.nomsId) }
  }

  @Test
  fun `test deleteBankApplication - updates isDeleted and deletionDate`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val bankApplicationEntity = BankApplicationEntity(1, prisonerEntity.id(), emptySet(), fakeNow, fakeNow, status = "Pending", bankName = "Lloyds")
    val expectedBankApplicationEntity = BankApplicationEntity(
      1,
      prisonerEntity.id(),
      emptySet(),
      fakeNow,
      fakeNow,
      status = "Pending",
      isDeleted = true,
      deletionDate = fakeNow,
      bankName = "Lloyds",
    )

    bankApplicationService.deleteBankApplication(bankApplicationEntity)

    Mockito.verify(bankApplicationRepository).save(expectedBankApplicationEntity)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test createBankApplication - creates bank application`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val bankApplication = BankApplication(applicationSubmittedDate = fakeNow, bankName = "Lloyds")
    val bankApplicationEntity = BankApplicationEntity(1, prisonerEntity.id(), setOf(BankApplicationStatusLogEntity(null, null, "Pending", fakeNow)), fakeNow, fakeNow, status = "Pending", isDeleted = false, bankName = "Lloyds")
    val logEntities = listOf(BankApplicationStatusLogEntity(1, bankApplicationEntity, "Pending", fakeNow))
    val expectedBankApplicationEntity = BankApplicationEntity(null, prisonerEntity.id(), emptySet(), fakeNow, fakeNow, status = "Pending", bankName = "Lloyds")
    val expectedLogEntity = BankApplicationStatusLogEntity(null, expectedBankApplicationEntity, "Pending", fakeNow)
    Mockito.`when`(prisonerRepository.findByNomsId(any())).thenReturn(prisonerEntity)
    Mockito.`when`(bankApplicationRepository.findByPrisonerIdAndIsDeleted(any(), any())).thenReturn(bankApplicationEntity)
    Mockito.`when`(bankApplicationStatusLogRepository.findByBankApplication(any())).thenReturn(logEntities)

    bankApplicationService.createBankApplication(bankApplication, prisonerEntity.nomsId, false)

    Mockito.verify(bankApplicationStatusLogRepository).save(expectedLogEntity)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test createBankApplication - creates bank application duplicate check`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val bankApplication = BankApplication(applicationSubmittedDate = fakeNow, bankName = "Lloyds")
    val bankApplicationEntity = BankApplicationEntity(1, prisonerEntity.id(), setOf(BankApplicationStatusLogEntity(null, null, "Pending", fakeNow)), fakeNow, fakeNow, status = "Pending", isDeleted = false, bankName = "Lloyds")
    val logEntities = listOf(BankApplicationStatusLogEntity(1, bankApplicationEntity, "Pending", fakeNow))
    val expectedBankApplicationEntity = BankApplicationEntity(null, prisonerEntity.id(), emptySet(), fakeNow, fakeNow, status = "Pending", bankName = "Lloyds")
    val expectedLogEntity = BankApplicationStatusLogEntity(null, expectedBankApplicationEntity, "Pending", fakeNow)
    Mockito.`when`(prisonerRepository.findByNomsId(any())).thenReturn(prisonerEntity)
    Mockito.`when`(bankApplicationRepository.findByPrisonerIdAndIsDeleted(any(), any())).thenReturn(bankApplicationEntity)
    Mockito.`when`(bankApplicationStatusLogRepository.findByBankApplication(any())).thenReturn(logEntities)
    bankApplicationService.createBankApplication(bankApplication, prisonerEntity.nomsId, false)
    Mockito.verify(bankApplicationStatusLogRepository).save(expectedLogEntity)
    assertThrows<DuplicateDataFoundException> { bankApplicationService.createBankApplication(bankApplication, prisonerEntity.nomsId, true) }
    Mockito.verify(bankApplicationRepository, Mockito.never()).save(Mockito.any())
    unmockkStatic(LocalDateTime::class)
  }

  @Nested
  inner class GetBankApplicationsByPrisonerAndCreationDate {

    private val prisoner = PrisonerEntity(1, "acb", testDate, "xyz")
    private val toDate = LocalDate.of(2025, 4, 11)
    private val fromDate = toDate.minusDays(7)

    @Test
    fun `should search between the start of the start date and the end of the end date`() {
      Mockito.`when`(bankApplicationRepository.findByPrisonerIdAndCreationDateBetween(any(), any(), any())).thenReturn(emptyList())

      bankApplicationService.getBankApplicationsByPrisonerAndCreationDate(prisoner, fromDate, toDate)

      Mockito.verify(bankApplicationRepository).findByPrisonerIdAndCreationDateBetween(prisoner.id(), fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX))
    }

    @Test
    fun `should return an empty list if there are no db records found`() {
      Mockito.`when`(bankApplicationRepository.findByPrisonerIdAndCreationDateBetween(any(), any(), any())).thenReturn(emptyList())

      val response = bankApplicationService.getBankApplicationsByPrisonerAndCreationDate(prisoner, fromDate, toDate)

      Assertions.assertEquals(emptyList<BankApplicationResponse>(), response)
    }

    @Test
    fun `should transform db records into a list of BankApplicationResponse`() {
      val bankApplicationEntity = BankApplicationEntity(1, prisoner.id(), setOf(BankApplicationStatusLogEntity(null, null, "Pending", fakeNow)), fakeNow, fakeNow, status = "Pending", isDeleted = false, bankName = "Lloyds")
      val logEntities = listOf(BankApplicationStatusLogEntity(1, bankApplicationEntity, "Pending", fakeNow))

      Mockito.`when`(bankApplicationRepository.findByPrisonerIdAndCreationDateBetween(any(), any(), any())).thenReturn(listOf(bankApplicationEntity))
      Mockito.`when`(bankApplicationStatusLogRepository.findByBankApplication(any())).thenReturn(logEntities)

      val actual = bankApplicationService.getBankApplicationsByPrisonerAndCreationDate(prisoner, fromDate, toDate)

      val expected = listOf(
        BankApplicationResponse(
          id = 1,
          prisoner = prisoner,
          logs = listOf(
            BankApplicationLog(
              id = 1,
              status = "Pending",
              changeDate = fakeNow,
            ),
          ),
          applicationSubmittedDate = fakeNow,
          currentStatus = "Pending",
          bankResponseDate = null,
          isAddedToPersonalItems = null,
          addedToPersonalItemsDate = null,
          bankName = "Lloyds",
        ),
      )

      Assertions.assertEquals(expected, actual)
    }
  }
}
