package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.SupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedUpdateRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class SupportNeedsServiceTest {
  private lateinit var supportNeedsService: SupportNeedsService

  @Mock
  private lateinit var prisonerSupportNeedRepository: PrisonerSupportNeedRepository

  @Mock
  private lateinit var prisonerSupportNeedUpdateRepository: PrisonerSupportNeedUpdateRepository

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  fun beforeEach() {
    supportNeedsService = SupportNeedsService(prisonerSupportNeedRepository, prisonerSupportNeedUpdateRepository, prisonerRepository)
  }

  @Test
  fun `test getNeedsSummary - prisonerId is null`() {
    Assertions.assertEquals(expectedDefaultNeedsSummary(), supportNeedsService.getNeedsSummary(null))
  }

  @Test
  fun `test getNeedsSummary - no records in database`() {
    val prisonerId = 1L
    whenever(prisonerSupportNeedRepository.findAllByPrisonerIdAndDeletedIsFalse(prisonerId)).thenReturn(listOf())
    Assertions.assertEquals(expectedDefaultNeedsSummary(), supportNeedsService.getNeedsSummary(prisonerId))
  }

  @Test
  fun `test getNeedsSummary - no associated update records in database`() {
    val prisonerId = 1L
    whenever(prisonerSupportNeedRepository.findAllByPrisonerIdAndDeletedIsFalse(prisonerId)).thenReturn(getPrisonerSupportNeeds())

    val expectedNeedsSummary = listOf(
      SupportNeedSummary(Pathway.ACCOMMODATION, true, 0, 0, 0, 0, LocalDate.parse("2023-09-12")),
      SupportNeedSummary(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, true, 0, 0, 0, 0, LocalDate.parse("2023-09-12")),
      SupportNeedSummary(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, true, 0, 0, 0, 0, LocalDate.parse("2023-09-12")),
      SupportNeedSummary(Pathway.DRUGS_AND_ALCOHOL, true, 0, 0, 0, 0, LocalDate.parse("2023-09-12")),
      SupportNeedSummary(Pathway.EDUCATION_SKILLS_AND_WORK, true, 0, 0, 0, 0, LocalDate.parse("2023-09-12")),
      SupportNeedSummary(Pathway.FINANCE_AND_ID, true, 0, 0, 0, 0, LocalDate.parse("2023-09-12")),
      SupportNeedSummary(Pathway.HEALTH, false, 0, 0, 0, 0, null),
    )

    Assertions.assertEquals(expectedNeedsSummary, supportNeedsService.getNeedsSummary(prisonerId))
  }

  @Test
  fun `test getNeedsSummary - with update records in database`() {
    val prisonerId = 1L
    whenever(prisonerSupportNeedRepository.findAllByPrisonerIdAndDeletedIsFalse(prisonerId)).thenReturn(getPrisonerSupportNeedsWithLatestUpdates())
    whenever(prisonerSupportNeedUpdateRepository.findById(1)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(1, SupportNeedStatus.MET, "12")))
    whenever(prisonerSupportNeedUpdateRepository.findById(2)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(2, SupportNeedStatus.NOT_STARTED, "11")))
    whenever(prisonerSupportNeedUpdateRepository.findById(3)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(3, SupportNeedStatus.DECLINED, "13")))
    whenever(prisonerSupportNeedUpdateRepository.findById(4)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(4, SupportNeedStatus.DECLINED, "10")))
    whenever(prisonerSupportNeedUpdateRepository.findById(5)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(5, SupportNeedStatus.NOT_STARTED, "14")))
    whenever(prisonerSupportNeedUpdateRepository.findById(6)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(6, SupportNeedStatus.MET, "08")))
    whenever(prisonerSupportNeedUpdateRepository.findById(7)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(7, SupportNeedStatus.DECLINED, "10")))
    whenever(prisonerSupportNeedUpdateRepository.findById(8)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(8, SupportNeedStatus.IN_PROGRESS, "23")))
    whenever(prisonerSupportNeedUpdateRepository.findById(9)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(9, SupportNeedStatus.MET, "01")))
    whenever(prisonerSupportNeedUpdateRepository.findById(10)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(10, SupportNeedStatus.DECLINED, "09")))
    whenever(prisonerSupportNeedUpdateRepository.findById(11)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(11, SupportNeedStatus.MET, "15")))
    whenever(prisonerSupportNeedUpdateRepository.findById(12)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(12, SupportNeedStatus.IN_PROGRESS, "03")))
    whenever(prisonerSupportNeedUpdateRepository.findById(13)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(13, SupportNeedStatus.NOT_STARTED, "19")))
    whenever(prisonerSupportNeedUpdateRepository.findById(14)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(14, SupportNeedStatus.MET, "17")))
    whenever(prisonerSupportNeedUpdateRepository.findById(15)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(15, SupportNeedStatus.MET, "22")))
    whenever(prisonerSupportNeedUpdateRepository.findById(16)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(16, SupportNeedStatus.IN_PROGRESS, "20")))
    whenever(prisonerSupportNeedUpdateRepository.findById(17)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(17, SupportNeedStatus.DECLINED, "07")))

    val expectedNeedsSummary = listOf(
      SupportNeedSummary(pathway = Pathway.ACCOMMODATION, reviewed = true, notStarted = 1, inProgress = 0, met = 1, declined = 1, lastUpdated = LocalDate.parse("2023-09-13")),
      SupportNeedSummary(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, reviewed = true, notStarted = 1, inProgress = 0, met = 0, declined = 1, lastUpdated = LocalDate.parse("2023-09-14")),
      SupportNeedSummary(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, reviewed = true, notStarted = 0, inProgress = 0, met = 1, declined = 0, lastUpdated = LocalDate.parse("2023-09-08")),
      SupportNeedSummary(pathway = Pathway.DRUGS_AND_ALCOHOL, reviewed = true, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = LocalDate.parse("2023-09-10")),
      SupportNeedSummary(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, reviewed = true, notStarted = 0, inProgress = 1, met = 2, declined = 1, lastUpdated = LocalDate.parse("2023-09-23")),
      SupportNeedSummary(pathway = Pathway.FINANCE_AND_ID, reviewed = true, notStarted = 1, inProgress = 2, met = 1, declined = 1, lastUpdated = LocalDate.parse("2023-09-22")),
      SupportNeedSummary(pathway = Pathway.HEALTH, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
    )

    Assertions.assertEquals(expectedNeedsSummary, supportNeedsService.getNeedsSummary(prisonerId))
  }

  private fun expectedDefaultNeedsSummary() = listOf(
    SupportNeedSummary(Pathway.ACCOMMODATION, false, 0, 0, 0, 0, null),
    SupportNeedSummary(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, false, 0, 0, 0, 0, null),
    SupportNeedSummary(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, false, 0, 0, 0, 0, null),
    SupportNeedSummary(Pathway.DRUGS_AND_ALCOHOL, false, 0, 0, 0, 0, null),
    SupportNeedSummary(Pathway.EDUCATION_SKILLS_AND_WORK, false, 0, 0, 0, 0, null),
    SupportNeedSummary(Pathway.FINANCE_AND_ID, false, 0, 0, 0, 0, null),
    SupportNeedSummary(Pathway.HEALTH, false, 0, 0, 0, 0, null),
  )

  private fun getPrisonerSupportNeeds() = listOf(
    getPrisonerSupportNeed(n = 1, pathway = Pathway.ACCOMMODATION),
    getPrisonerSupportNeed(n = 2, pathway = Pathway.ACCOMMODATION),
    getPrisonerSupportNeed(n = 3, pathway = Pathway.ACCOMMODATION),
    getPrisonerSupportNeed(n = 4, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR),
    getPrisonerSupportNeed(n = 5, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR),
    getPrisonerSupportNeed(n = 6, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY),
    getPrisonerSupportNeed(n = 7, pathway = Pathway.DRUGS_AND_ALCOHOL, excludeFromCount = true),
    getPrisonerSupportNeed(n = 8, pathway = Pathway.EDUCATION_SKILLS_AND_WORK),
    getPrisonerSupportNeed(n = 9, pathway = Pathway.EDUCATION_SKILLS_AND_WORK),
    getPrisonerSupportNeed(n = 10, pathway = Pathway.EDUCATION_SKILLS_AND_WORK),
    getPrisonerSupportNeed(n = 11, pathway = Pathway.EDUCATION_SKILLS_AND_WORK),
    getPrisonerSupportNeed(n = 12, pathway = Pathway.FINANCE_AND_ID),
    getPrisonerSupportNeed(n = 13, pathway = Pathway.FINANCE_AND_ID),
    getPrisonerSupportNeed(n = 14, pathway = Pathway.FINANCE_AND_ID),
    getPrisonerSupportNeed(n = 15, pathway = Pathway.FINANCE_AND_ID, excludeFromCount = true),
    getPrisonerSupportNeed(n = 16, pathway = Pathway.FINANCE_AND_ID),
    getPrisonerSupportNeed(n = 17, pathway = Pathway.FINANCE_AND_ID),
  )

  private fun getPrisonerSupportNeedsWithLatestUpdates() = listOf(
    getPrisonerSupportNeed(n = 1, pathway = Pathway.ACCOMMODATION, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 2, pathway = Pathway.ACCOMMODATION, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 3, pathway = Pathway.ACCOMMODATION, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 4, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 5, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 6, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 7, pathway = Pathway.DRUGS_AND_ALCOHOL, excludeFromCount = true, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 8, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 9, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 10, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 11, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 12, pathway = Pathway.FINANCE_AND_ID, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 13, pathway = Pathway.FINANCE_AND_ID, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 14, pathway = Pathway.FINANCE_AND_ID, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 15, pathway = Pathway.FINANCE_AND_ID, excludeFromCount = true, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 16, pathway = Pathway.FINANCE_AND_ID, includeLatestUpdate = true),
    getPrisonerSupportNeed(n = 17, pathway = Pathway.FINANCE_AND_ID, includeLatestUpdate = true),
  )

  private fun getPrisonerSupportNeed(n: Int, pathway: Pathway, excludeFromCount: Boolean = false, includeLatestUpdate: Boolean = false) =
    PrisonerSupportNeedEntity(
      id = n.toLong(),
      prisonerId = 1,
      supportNeed = getSupportNeed(n, pathway, excludeFromCount),
      otherDetail = null,
      createdBy = "Someone",
      createdDate = LocalDateTime.parse("2023-09-12T12:10:00"),
      latestUpdateId = if (includeLatestUpdate) n.toLong() else null,
    )

  private fun getSupportNeed(n: Int, pathway: Pathway, excludeFromCount: Boolean = false) = SupportNeedEntity(
    id = n.toLong(),
    pathway = pathway,
    section = "Section $n",
    title = "Title $n",
    hidden = false,
    excludeFromCount = excludeFromCount,
    allowOtherDetail = false,
    createdDate = LocalDateTime.parse("2023-09-12T12:09:00"),
  )

  private fun getPrisonerSupportNeedUpdate(n: Int, status: SupportNeedStatus, createdDateDay: String) = PrisonerSupportNeedUpdateEntity(
    id = n.toLong(),
    prisonerSupportNeedId = n.toLong(),
    createdBy = "Someone",
    createdDate = LocalDateTime.parse("2023-09-${createdDateDay}T10:10:00"),
    updateText = "Something",
    status = status,
    isPrison = true,
    isProbation = false,
  )

  @Test
  fun `test getNeedsSummaryToNomsIdMapByPrisonId`() {
    val prisonId = "MDI"
    whenever(prisonerSupportNeedRepository.getPrisonerSupportNeedsByPrisonId(prisonId)).thenReturn(
      listOf(
        arrayOf(1L, "A1", Pathway.ACCOMMODATION, LocalDateTime.parse("2025-09-10T12:00:01"), 12L, SupportNeedStatus.NOT_STARTED, LocalDateTime.parse("2025-09-11T12:00:01")),
        arrayOf(1L, "A1", Pathway.ACCOMMODATION, LocalDateTime.parse("2025-09-12T12:00:01"), null, null, null),
        arrayOf(1L, "A1", Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, LocalDateTime.parse("2025-09-13T12:00:01"), 13L, SupportNeedStatus.MET, LocalDateTime.parse("2025-09-16T12:00:01")),
        arrayOf(1L, "A1", Pathway.HEALTH, LocalDateTime.parse("2025-09-13T12:00:01"), 14L, SupportNeedStatus.NOT_STARTED, LocalDateTime.parse("2025-09-09T12:00:01")),
        arrayOf(1L, "A1", Pathway.HEALTH, LocalDateTime.parse("2025-09-14T12:00:01"), 15L, SupportNeedStatus.IN_PROGRESS, LocalDateTime.parse("2025-09-16T12:00:01")),
        arrayOf(2L, "A2", Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, LocalDateTime.parse("2025-09-15T12:00:01"), 16L, SupportNeedStatus.MET, LocalDateTime.parse("2025-09-17T12:00:01")),
        arrayOf(2L, "A2", Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, LocalDateTime.parse("2025-09-15T12:00:01"), 17L, SupportNeedStatus.IN_PROGRESS, LocalDateTime.parse("2025-09-19T12:00:01")),
        arrayOf(2L, "A2", Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, LocalDateTime.parse("2025-09-16T12:00:01"), 18L, SupportNeedStatus.NOT_STARTED, LocalDateTime.parse("2025-09-20T12:00:01")),
        arrayOf(2L, "A2", Pathway.DRUGS_AND_ALCOHOL, LocalDateTime.parse("2025-09-16T12:00:01"), 19L, SupportNeedStatus.DECLINED, LocalDateTime.parse("2025-09-21T12:00:01")),
        arrayOf(2L, "A2", Pathway.DRUGS_AND_ALCOHOL, LocalDateTime.parse("2025-09-18T12:00:01"), 21L, SupportNeedStatus.IN_PROGRESS, LocalDateTime.parse("2025-09-10T12:00:01")),
        arrayOf(3L, "A3", Pathway.EDUCATION_SKILLS_AND_WORK, LocalDateTime.parse("2025-09-11T12:00:01"), 22L, SupportNeedStatus.MET, LocalDateTime.parse("2025-09-12T12:00:01")),
        arrayOf(3L, "A3", Pathway.FINANCE_AND_ID, LocalDateTime.parse("2025-09-21T12:00:01"), 25L, SupportNeedStatus.DECLINED, LocalDateTime.parse("2025-09-30T12:00:01")),
      ),
    )

    val expectedSupportNeedsSummaryMap = mapOf(
      "A1" to listOf(SupportNeedSummary(pathway = Pathway.ACCOMMODATION, reviewed = true, notStarted = 1, inProgress = 0, met = 0, declined = 0, lastUpdated = LocalDate.parse("2025-09-12")), SupportNeedSummary(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, reviewed = true, notStarted = 0, inProgress = 0, met = 1, declined = 0, lastUpdated = LocalDate.parse("2025-09-16")), SupportNeedSummary(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.DRUGS_AND_ALCOHOL, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.FINANCE_AND_ID, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.HEALTH, reviewed = true, notStarted = 1, inProgress = 1, met = 0, declined = 0, lastUpdated = LocalDate.parse("2025-09-16"))),
      "A2" to listOf(SupportNeedSummary(pathway = Pathway.ACCOMMODATION, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, reviewed = true, notStarted = 1, inProgress = 1, met = 1, declined = 0, lastUpdated = LocalDate.parse("2025-09-20")), SupportNeedSummary(pathway = Pathway.DRUGS_AND_ALCOHOL, reviewed = true, notStarted = 0, inProgress = 1, met = 0, declined = 1, lastUpdated = LocalDate.parse("2025-09-21")), SupportNeedSummary(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.FINANCE_AND_ID, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.HEALTH, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null)),
      "A3" to listOf(SupportNeedSummary(pathway = Pathway.ACCOMMODATION, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.DRUGS_AND_ALCOHOL, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null), SupportNeedSummary(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, reviewed = true, notStarted = 0, inProgress = 0, met = 1, declined = 0, lastUpdated = LocalDate.parse("2025-09-12")), SupportNeedSummary(pathway = Pathway.FINANCE_AND_ID, reviewed = true, notStarted = 0, inProgress = 0, met = 0, declined = 1, lastUpdated = LocalDate.parse("2025-09-30")), SupportNeedSummary(pathway = Pathway.HEALTH, reviewed = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null)),
    )

    Assertions.assertEquals(expectedSupportNeedsSummaryMap, supportNeedsService.getNeedsSummaryToNomsIdMapByPrisonId(prisonId))
  }
}
