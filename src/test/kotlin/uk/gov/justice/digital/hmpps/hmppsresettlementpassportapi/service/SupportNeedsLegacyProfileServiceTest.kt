package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.SupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.SupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.AdminServiceTest.Companion
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class SupportNeedsLegacyProfileServiceTest {
  private lateinit var supportNeedsLegacyProfileService: SupportNeedsLegacyProfileService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var supportNeedRepository: SupportNeedRepository

  @Mock
  private lateinit var prisonerSupportNeedRepository: PrisonerSupportNeedRepository

  @BeforeEach
  fun beforeEach() {
    supportNeedsLegacyProfileService = SupportNeedsLegacyProfileService(prisonerRepository, supportNeedRepository, prisonerSupportNeedRepository)
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

  @Test
  fun `test setSupportNeedsLegacyProfile`() {
    supportNeedsLegacyProfileService.setSupportNeedsLegacyProfile()
    verify(prisonerRepository).updateProfileResetLegacyProfileFlags()
  }

  @Test
  fun `test addLegacySupportNeeds`() {
    val fakeNow = LocalDateTime.parse("2024-07-02T12:12:12")
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    whenever(supportNeedRepository.findAllBySectionAndDeletedIsFalse("Legacy support need")).thenReturn(listOf(
      SupportNeedEntity(id = 101, pathway = Pathway.ACCOMMODATION, section = "Legacy support need", title = "Accommodation", hidden = true, excludeFromCount = false, allowOtherDetail = true, createdDate = LocalDateTime.parse("2025-02-28T12:00:00")),
      SupportNeedEntity(id = 102, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, section = "Legacy support need", title = "Attitudes, thinking and behaviour", hidden = true, excludeFromCount = false, allowOtherDetail = true, createdDate = LocalDateTime.parse("2025-02-28T12:00:00")),
      SupportNeedEntity(id = 103, pathway = Pathway.HEALTH, section = "Legacy support need", title = "Health", hidden = true, excludeFromCount = false, allowOtherDetail = true, createdDate = LocalDateTime.parse("2025-02-28T12:00:00")),
    ))

    whenever(prisonerRepository.findAllBySupportNeedsLegacyProfileIsTrue()).thenReturn(listOf(
      PrisonerEntity(id = 1, nomsId = "A1", prisonId = "ABC", supportNeedsLegacyProfile = true),
      PrisonerEntity(id = 2, nomsId = "A2", prisonId = "ABC", supportNeedsLegacyProfile = true),
      PrisonerEntity(id = 3, nomsId = "A3", prisonId = "ABC", supportNeedsLegacyProfile = true),
    ))

    whenever(prisonerSupportNeedRepository.countPrisonerSupportNeedEntityByPrisonerIdAndDeletedIsFalse(1)).thenReturn(0)
    whenever(prisonerSupportNeedRepository.countPrisonerSupportNeedEntityByPrisonerIdAndDeletedIsFalse(2)).thenReturn(0)
    whenever(prisonerSupportNeedRepository.countPrisonerSupportNeedEntityByPrisonerIdAndDeletedIsFalse(3)).thenReturn(7)

    supportNeedsLegacyProfileService.addLegacySupportNeeds()
    
    verify(prisonerSupportNeedRepository).saveAll(listOf(
      PrisonerSupportNeedEntity(prisonerId = 1, supportNeed = SupportNeedEntity(id = 101, pathway = Pathway.ACCOMMODATION, section = "Legacy support need", title = "Accommodation", hidden = true, excludeFromCount = false, allowOtherDetail = true, createdDate = LocalDateTime.parse("2025-02-28T12:00"), deleted = false, deletedDate = null), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(prisonerId = 1, supportNeed = SupportNeedEntity(id = 102, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, section = "Legacy support need", title = "Attitudes, thinking and behaviour", hidden = true, excludeFromCount = false, allowOtherDetail = true, createdDate = LocalDateTime.parse("2025-02-28T12:00"), deleted = false, deletedDate = null), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(prisonerId = 1, supportNeed = SupportNeedEntity(id = 103, pathway = Pathway.HEALTH, section = "Legacy support need", title = "Health", hidden = true, excludeFromCount = false, allowOtherDetail = true, createdDate = LocalDateTime.parse("2025-02-28T12:00"), deleted = false, deletedDate = null), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
    ))

    verify(prisonerSupportNeedRepository).saveAll(listOf(
      PrisonerSupportNeedEntity(prisonerId = 2, supportNeed = SupportNeedEntity(id = 101, pathway = Pathway.ACCOMMODATION, section = "Legacy support need", title = "Accommodation", hidden = true, excludeFromCount = false, allowOtherDetail = true, createdDate = LocalDateTime.parse("2025-02-28T12:00"), deleted = false, deletedDate = null), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(prisonerId = 2, supportNeed = SupportNeedEntity(id = 102, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, section = "Legacy support need", title = "Attitudes, thinking and behaviour", hidden = true, excludeFromCount = false, allowOtherDetail = true, createdDate = LocalDateTime.parse("2025-02-28T12:00"), deleted = false, deletedDate = null), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(prisonerId = 2, supportNeed = SupportNeedEntity(id = 103, pathway = Pathway.HEALTH, section = "Legacy support need", title = "Health", hidden = true, excludeFromCount = false, allowOtherDetail = true, createdDate = LocalDateTime.parse("2025-02-28T12:00"), deleted = false, deletedDate = null), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
    ))

    unmockkAll()
  }
}
