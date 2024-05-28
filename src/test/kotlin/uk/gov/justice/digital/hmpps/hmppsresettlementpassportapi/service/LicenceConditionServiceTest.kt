package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.LicenceConditionChangeAuditEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.LicenceConditionsChangeAuditRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CvlApiService
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class LicenceConditionServiceTest {
  private lateinit var licenceConditionService: LicenceConditionService

  @Mock
  private lateinit var cvlApiService: CvlApiService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var licenceConditionsChangeAuditRepository: LicenceConditionsChangeAuditRepository

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    licenceConditionService = LicenceConditionService(cvlApiService, licenceConditionsChangeAuditRepository, prisonerRepository)
  }

  @Test
  fun `test verifyCompareAndSave first time - returns compare status`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    Mockito.`when`(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    Mockito.`when`(licenceConditionsChangeAuditRepository.findByPrisoner(prisonerEntity)).thenReturn(null)
    val response = licenceConditionService.compareAndSave("licenceConditions", "acb")
    Assertions.assertEquals(true, response)
  }

  @Test
  fun `test verifyCompareAndSave not first time but no data change - returns compare status`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val licenceConditionChangeAuditEntity = LicenceConditionChangeAuditEntity(1, prisonerEntity, toMD5("licenceConditions"), fakeNow)
    Mockito.`when`(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    Mockito.`when`(licenceConditionsChangeAuditRepository.findByPrisoner(prisonerEntity)).thenReturn(licenceConditionChangeAuditEntity)
    val response = licenceConditionService.compareAndSave("licenceConditions", "acb")
    Assertions.assertEquals(false, response)
  }

  @Test
  fun `test verifyCompareAndSave not first time but data changed- returns compare status`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val licenceConditionChangeAuditEntity = LicenceConditionChangeAuditEntity(1, prisonerEntity, toMD5("licenceConditions"), fakeNow)
    Mockito.`when`(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    Mockito.`when`(licenceConditionsChangeAuditRepository.findByPrisoner(prisonerEntity)).thenReturn(licenceConditionChangeAuditEntity)
    val response = licenceConditionService.compareAndSave("licenceConditions changed", "acb")
    Assertions.assertEquals(true, response)
  }
}
