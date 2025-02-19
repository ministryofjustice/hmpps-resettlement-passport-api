package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class SupportNeedsLegacyProfileServiceTest {
  private lateinit var supportNeedsLegacyProfileService: SupportNeedsLegacyProfileService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  fun beforeEach() {
    supportNeedsLegacyProfileService = SupportNeedsLegacyProfileService(prisonerRepository)
  }

  @Test
  fun `test setSupportNeedsLegacyFlag - happy path - set to true`() {
    val nomsId = "ABC1234"
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(1, nomsId, LocalDateTime.parse("2025-02-19T12:00:01"), "MDI", false))

    supportNeedsLegacyProfileService.setSupportNeedsLegacyFlag(nomsId, true)
    verify(prisonerRepository).save(PrisonerEntity(1, nomsId, LocalDateTime.parse("2025-02-19T12:00:01"), "MDI", true))
  }

  @Test
  fun `test setSupportNeedsLegacyFlag - happy path - set to false`() {
    val nomsId = "ABC1234"
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(1, nomsId, LocalDateTime.parse("2025-02-19T12:00:01"), "MDI", true))

    supportNeedsLegacyProfileService.setSupportNeedsLegacyFlag(nomsId, false)
    verify(prisonerRepository).save(PrisonerEntity(1, nomsId, LocalDateTime.parse("2025-02-19T12:00:01"), "MDI", false))
  }

  @Test
  fun `test setSupportNeedsLegacyFlag - error - prisoner not found`() {
    val nomsId = "ABC1234"
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(null)

    assertThrows<ResourceNotFoundException> {
      supportNeedsLegacyProfileService.setSupportNeedsLegacyFlag(
        nomsId,
        false,
      )
    }
    verifyNoMoreInteractions(prisonerRepository)
  }
}
