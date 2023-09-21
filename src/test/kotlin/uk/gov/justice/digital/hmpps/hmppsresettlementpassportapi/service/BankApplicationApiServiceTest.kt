package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.bankapplicatonapi.BankApplicationDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationStatusLogEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.BankApplicationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime
import java.util.Optional

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
class BankApplicationApiServiceTest {
  private lateinit var bankApplicationApiService: BankApplicationApiService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var bankApplicationRepository: BankApplicationRepository
  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    bankApplicationApiService = BankApplicationApiService(prisonerRepository, bankApplicationRepository)
  }

  @Test
  fun `test getBankApplicationById - returns bank application`() = runTest {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn")
    val bankApplicationEntity = BankApplicationEntity(1, prisonerEntity, emptySet(), fakeNow, fakeNow, status = "Pending")
    Mockito.`when`(bankApplicationRepository.findById(1)).thenReturn(Optional.of(bankApplicationEntity))

    val response = bankApplicationApiService.getBankApplicationById(1).get()

    Assertions.assertEquals(bankApplicationEntity, response)
  }

  @Test
  fun `test getBankApplicationByPrisoner - returns bank application`() = runTest {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn")
    val bankApplicationEntity = BankApplicationEntity(1, prisonerEntity, emptySet(), fakeNow, fakeNow, status = "Pending")
    Mockito.`when`(bankApplicationRepository.findByPrisonerAndIsDeleted(prisonerEntity)).thenReturn(bankApplicationEntity)

    val response = bankApplicationApiService.getBankApplicationByPrisoner(prisonerEntity)

    Assertions.assertEquals(bankApplicationEntity, response)
  }

  @Test
  fun `test getBankApplicationByPrisoner - throws if not found`() = runTest {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn")
    Mockito.`when`(bankApplicationRepository.findByPrisonerAndIsDeleted(any(), any())).thenReturn(null)

    assertThrows<ResourceNotFoundException> { bankApplicationApiService.getBankApplicationByPrisoner(prisonerEntity) }
  }

  @Test
  fun `test deleteBankApplication - updates isDeleted and deletionDate`() = runTest {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn")
    val bankApplicationEntity = BankApplicationEntity(1, prisonerEntity, emptySet(), fakeNow, fakeNow, status = "Pending")
    val expectedBankApplicationEntity = BankApplicationEntity(1, prisonerEntity, emptySet(), fakeNow, fakeNow, status = "Pending", isDeleted = true, deletionDate = fakeNow )

    bankApplicationApiService.deleteBankApplication(bankApplicationEntity)

    Mockito.verify(bankApplicationRepository).save(expectedBankApplicationEntity)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test createBankApplication - creates bank application`() = runTest {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn")
    val bankApplicationDTO = BankApplicationDTO(applicationSubmittedDate = fakeNow )

    val expectedBankApplicationEntity = BankApplicationEntity(null, prisonerEntity, setOf(BankApplicationStatusLogEntity(null, null, "Pending", changedAtDate = fakeNow)), fakeNow, fakeNow, status = "Pending")

    bankApplicationApiService.createBankApplication(bankApplicationDTO, prisonerEntity)

    Mockito.verify(bankApplicationRepository).save(expectedBankApplicationEntity)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test updateBankApplication - updates bank application`() = runTest {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn")
    val bankApplicationDTO = BankApplicationDTO(status = "Account Opened", bankResponseDate = fakeNow)
    val bankApplicationEntity = BankApplicationEntity(null, prisonerEntity, setOf(BankApplicationStatusLogEntity(null, null, "Pending", changedAtDate = fakeNow)), fakeNow, fakeNow, status = "Pending")
    val expectedBankApplicationEntity = BankApplicationEntity(
      null,
      prisonerEntity,
      setOf(BankApplicationStatusLogEntity(null, null, "Pending", changedAtDate = fakeNow),
        BankApplicationStatusLogEntity(null, null, "Account Opened", changedAtDate = fakeNow)),
      fakeNow,
      fakeNow,
      status = "Account Opened",
      bankResponseDate = fakeNow)

    bankApplicationApiService.updateBankApplication(bankApplicationEntity, bankApplicationDTO)

    Mockito.verify(bankApplicationRepository).save(expectedBankApplicationEntity)
    unmockkStatic(LocalDateTime::class)
  }
}