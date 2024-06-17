package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditionsMetadata
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
  private lateinit var cvlService: CvlService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var licenceConditionsChangeAuditRepository: LicenceConditionsChangeAuditRepository

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    licenceConditionService =
      LicenceConditionService(cvlApiService, licenceConditionsChangeAuditRepository, prisonerRepository, cvlService)
  }

  @Test
  fun `test verifyCompareAndSave first time - returns compare status`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    whenever(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    whenever(licenceConditionsChangeAuditRepository.findFirstByPrisonerIdOrderByCreationDateDesc(1)).thenReturn(null)

    val response = licenceConditionService.compareAndSave(LicenceConditions(1), "acb")

    assertThat(response).isEqualTo(LicenceConditionsMetadata(true, 1))
  }

  @Test
  fun `test verifyCompareAndSave not first time but no data change - returns compare status`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val licenceConditionChangeAuditEntity = LicenceConditionChangeAuditEntity(
      id = 1,
      prisonerId = prisonerEntity.id!!,
      licenceConditions = LicenceConditions(1),
      creationDate = fakeNow,
      seen = true,
    )
    whenever(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    whenever(licenceConditionsChangeAuditRepository.findFirstByPrisonerIdOrderByCreationDateDesc(1)).thenReturn(
      licenceConditionChangeAuditEntity,
    )

    val response = licenceConditionService.compareAndSave(LicenceConditions(1), "acb")

    assertThat(response).isEqualTo(LicenceConditionsMetadata(false, 1))
  }

  @Test
  fun `test verifyCompareAndSave not first time but data changed- returns compare status`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val licenceConditionChangeAuditEntity =
      LicenceConditionChangeAuditEntity(
        id = 1,
        prisonerId = prisonerEntity.id!!,
        licenceConditions = LicenceConditions(1),
        creationDate = fakeNow,
      )
    whenever(prisonerRepository.findByNomsId("acb")).thenReturn(prisonerEntity)
    whenever(licenceConditionsChangeAuditRepository.findFirstByPrisonerIdOrderByCreationDateDesc(1)).thenReturn(
      licenceConditionChangeAuditEntity,
    )

    val response = licenceConditionService.compareAndSave(LicenceConditions(2), "acb")

    assertThat(response).isEqualTo(LicenceConditionsMetadata(true, 2))
  }
}
