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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayNeedsSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeed
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
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
      SupportNeedSummary(pathway = Pathway.ACCOMMODATION, reviewed = true, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = LocalDate.parse("2023-09-12")),
      SupportNeedSummary(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, reviewed = true, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = LocalDate.parse("2023-09-12")),
      SupportNeedSummary(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, reviewed = true, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = LocalDate.parse("2023-09-12")),
      SupportNeedSummary(pathway = Pathway.DRUGS_AND_ALCOHOL, reviewed = true, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = LocalDate.parse("2023-09-12")),
      SupportNeedSummary(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, reviewed = true, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = LocalDate.parse("2023-09-12")),
      SupportNeedSummary(pathway = Pathway.FINANCE_AND_ID, reviewed = true, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = LocalDate.parse("2023-09-12")),
      SupportNeedSummary(pathway = Pathway.HEALTH, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
    )

    Assertions.assertEquals(expectedNeedsSummary, supportNeedsService.getNeedsSummary(prisonerId))
  }

  @Test
  fun `test getNeedsSummary - with update records in database`() {
    val prisonerId = 1L
    whenever(prisonerSupportNeedRepository.findAllByPrisonerIdAndDeletedIsFalse(prisonerId)).thenReturn(getPrisonerSupportNeedsWithLatestUpdates())
    whenever(prisonerSupportNeedUpdateRepository.findById(1)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 1, status = SupportNeedStatus.MET, createdDateDay = "12", isPrisonResponsible = false, isProbationResponsible = false)))
    whenever(prisonerSupportNeedUpdateRepository.findById(2)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 2, status = SupportNeedStatus.NOT_STARTED, createdDateDay = "11", isPrisonResponsible = true, isProbationResponsible = false)))
    whenever(prisonerSupportNeedUpdateRepository.findById(3)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 3, status = SupportNeedStatus.DECLINED, createdDateDay = "13", isPrisonResponsible = false, isProbationResponsible = false)))
    whenever(prisonerSupportNeedUpdateRepository.findById(4)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 4, status = SupportNeedStatus.DECLINED, createdDateDay = "10", isPrisonResponsible = true, isProbationResponsible = true)))
    whenever(prisonerSupportNeedUpdateRepository.findById(5)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 5, status = SupportNeedStatus.NOT_STARTED, createdDateDay = "14", isPrisonResponsible = false, isProbationResponsible = true)))
    whenever(prisonerSupportNeedUpdateRepository.findById(6)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 6, status = SupportNeedStatus.MET, createdDateDay = "08", isPrisonResponsible = false, isProbationResponsible = false)))
    whenever(prisonerSupportNeedUpdateRepository.findById(7)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 7, status = SupportNeedStatus.DECLINED, createdDateDay = "10", isPrisonResponsible = false, isProbationResponsible = true)))
    whenever(prisonerSupportNeedUpdateRepository.findById(8)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 8, status = SupportNeedStatus.IN_PROGRESS, createdDateDay = "23", isPrisonResponsible = true, isProbationResponsible = true)))
    whenever(prisonerSupportNeedUpdateRepository.findById(9)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 9, status = SupportNeedStatus.MET, createdDateDay = "01", isPrisonResponsible = true, isProbationResponsible = false)))
    whenever(prisonerSupportNeedUpdateRepository.findById(10)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 10, status = SupportNeedStatus.DECLINED, createdDateDay = "09", isPrisonResponsible = false, isProbationResponsible = true)))
    whenever(prisonerSupportNeedUpdateRepository.findById(11)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 11, status = SupportNeedStatus.MET, createdDateDay = "15", isPrisonResponsible = false, isProbationResponsible = false)))
    whenever(prisonerSupportNeedUpdateRepository.findById(12)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 12, status = SupportNeedStatus.IN_PROGRESS, createdDateDay = "03", isPrisonResponsible = false, isProbationResponsible = false)))
    whenever(prisonerSupportNeedUpdateRepository.findById(13)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 13, status = SupportNeedStatus.NOT_STARTED, createdDateDay = "19", isPrisonResponsible = true, isProbationResponsible = true)))
    whenever(prisonerSupportNeedUpdateRepository.findById(14)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 14, status = SupportNeedStatus.MET, createdDateDay = "17", isPrisonResponsible = true, isProbationResponsible = false)))
    whenever(prisonerSupportNeedUpdateRepository.findById(15)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 15, status = SupportNeedStatus.MET, createdDateDay = "22", isPrisonResponsible = false, isProbationResponsible = true)))
    whenever(prisonerSupportNeedUpdateRepository.findById(16)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 16, status = SupportNeedStatus.IN_PROGRESS, createdDateDay = "20", isPrisonResponsible = true, isProbationResponsible = false)))
    whenever(prisonerSupportNeedUpdateRepository.findById(17)).thenReturn(Optional.of(getPrisonerSupportNeedUpdate(n = 17, status = SupportNeedStatus.DECLINED, createdDateDay = "07", isPrisonResponsible = false, isProbationResponsible = false)))

    val expectedNeedsSummary = listOf(
      SupportNeedSummary(pathway = Pathway.ACCOMMODATION, reviewed = true, isPrisonResponsible = true, isProbationResponsible = false, notStarted = 1, inProgress = 0, met = 1, declined = 1, lastUpdated = LocalDate.parse("2023-09-13")),
      SupportNeedSummary(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, reviewed = true, isPrisonResponsible = true, isProbationResponsible = true, notStarted = 1, inProgress = 0, met = 0, declined = 1, lastUpdated = LocalDate.parse("2023-09-14")),
      SupportNeedSummary(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, reviewed = true, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 1, declined = 0, lastUpdated = LocalDate.parse("2023-09-08")),
      SupportNeedSummary(pathway = Pathway.DRUGS_AND_ALCOHOL, reviewed = true, isPrisonResponsible = false, isProbationResponsible = true, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = LocalDate.parse("2023-09-10")),
      SupportNeedSummary(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, reviewed = true, isPrisonResponsible = true, isProbationResponsible = true, notStarted = 0, inProgress = 1, met = 2, declined = 1, lastUpdated = LocalDate.parse("2023-09-23")),
      SupportNeedSummary(pathway = Pathway.FINANCE_AND_ID, reviewed = true, isPrisonResponsible = true, isProbationResponsible = true, notStarted = 1, inProgress = 2, met = 1, declined = 1, lastUpdated = LocalDate.parse("2023-09-22")),
      SupportNeedSummary(pathway = Pathway.HEALTH, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
    )

    Assertions.assertEquals(expectedNeedsSummary, supportNeedsService.getNeedsSummary(prisonerId))
  }

  private fun expectedDefaultNeedsSummary() = listOf(
    SupportNeedSummary(pathway = Pathway.ACCOMMODATION, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
    SupportNeedSummary(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
    SupportNeedSummary(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
    SupportNeedSummary(pathway = Pathway.DRUGS_AND_ALCOHOL, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
    SupportNeedSummary(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
    SupportNeedSummary(pathway = Pathway.FINANCE_AND_ID, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
    SupportNeedSummary(pathway = Pathway.HEALTH, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
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

  private fun getSupportNeed(n: Int, pathway: Pathway, excludeFromCount: Boolean = false, allowOtherDetail: Boolean = false) = SupportNeedEntity(
    id = n.toLong(),
    pathway = pathway,
    section = "Section $n",
    title = "Title $n",
    hidden = false,
    excludeFromCount = excludeFromCount,
    allowOtherDetail = allowOtherDetail,
    createdDate = LocalDateTime.parse("2023-09-12T12:09:00"),
  )

  private fun getPrisonerSupportNeedUpdate(n: Int, status: SupportNeedStatus, createdDateDay: String, isPrisonResponsible: Boolean, isProbationResponsible: Boolean) = PrisonerSupportNeedUpdateEntity(
    id = n.toLong(),
    prisonerSupportNeedId = n.toLong(),
    createdBy = "Someone",
    createdDate = LocalDateTime.parse("2023-09-${createdDateDay}T10:10:00"),
    updateText = "Something",
    status = status,
    isPrison = isPrisonResponsible,
    isProbation = isProbationResponsible,
  )

  @Test
  fun `test getNeedsSummaryToNomsIdMapByPrisonId`() {
    val prisonId = "MDI"
    whenever(prisonerSupportNeedRepository.getPrisonerSupportNeedsByPrisonId(prisonId)).thenReturn(
      listOf(
        arrayOf(1L, "A1", Pathway.ACCOMMODATION, LocalDateTime.parse("2025-09-10T12:00:01"), false, 12L, SupportNeedStatus.NOT_STARTED, LocalDateTime.parse("2025-09-11T12:00:01"), false, false),
        arrayOf(1L, "A1", Pathway.ACCOMMODATION, LocalDateTime.parse("2025-09-12T12:00:01"), false, null, null, null, null, null),
        arrayOf(1L, "A1", Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, LocalDateTime.parse("2025-09-13T12:00:01"), false, 13L, SupportNeedStatus.MET, LocalDateTime.parse("2025-09-16T12:00:01"), true, true),
        arrayOf(1L, "A1", Pathway.HEALTH, LocalDateTime.parse("2025-09-13T12:00:01"), false, 14L, SupportNeedStatus.NOT_STARTED, LocalDateTime.parse("2025-09-09T12:00:01"), true, false),
        arrayOf(1L, "A1", Pathway.HEALTH, LocalDateTime.parse("2025-09-14T12:00:01"), false, 15L, SupportNeedStatus.IN_PROGRESS, LocalDateTime.parse("2025-09-16T12:00:01"), false, false),
        arrayOf(2L, "A2", Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, LocalDateTime.parse("2025-09-15T12:00:01"), false, 16L, SupportNeedStatus.MET, LocalDateTime.parse("2025-09-17T12:00:01"), false, false),
        arrayOf(2L, "A2", Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, LocalDateTime.parse("2025-09-15T12:00:01"), false, 17L, SupportNeedStatus.IN_PROGRESS, LocalDateTime.parse("2025-09-19T12:00:01"), false, true),
        arrayOf(2L, "A2", Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, LocalDateTime.parse("2025-09-16T12:00:01"), false, 18L, SupportNeedStatus.NOT_STARTED, LocalDateTime.parse("2025-09-20T12:00:01"), false, false),
        arrayOf(2L, "A2", Pathway.DRUGS_AND_ALCOHOL, LocalDateTime.parse("2025-09-16T12:00:01"), false, 19L, SupportNeedStatus.DECLINED, LocalDateTime.parse("2025-09-21T12:00:01"), false, true),
        arrayOf(2L, "A2", Pathway.DRUGS_AND_ALCOHOL, LocalDateTime.parse("2025-09-18T12:00:01"), false, 21L, SupportNeedStatus.IN_PROGRESS, LocalDateTime.parse("2025-09-10T12:00:01"), true, false),
        arrayOf(3L, "A3", Pathway.EDUCATION_SKILLS_AND_WORK, LocalDateTime.parse("2025-09-11T12:00:01"), false, 22L, SupportNeedStatus.MET, LocalDateTime.parse("2025-09-12T12:00:01"), false, false),
        arrayOf(3L, "A3", Pathway.FINANCE_AND_ID, LocalDateTime.parse("2025-09-21T12:00:01"), false, 25L, SupportNeedStatus.DECLINED, LocalDateTime.parse("2025-09-30T12:00:01"), true, false),
      ),
    )

    val expectedSupportNeedsSummaryMap = mapOf(
      "A1" to listOf(
        SupportNeedSummary(pathway = Pathway.ACCOMMODATION, reviewed = true, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 1, inProgress = 0, met = 0, declined = 0, lastUpdated = LocalDate.parse("2025-09-12")),
        SupportNeedSummary(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, reviewed = true, isPrisonResponsible = true, isProbationResponsible = true, notStarted = 0, inProgress = 0, met = 1, declined = 0, lastUpdated = LocalDate.parse("2025-09-16")),
        SupportNeedSummary(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.DRUGS_AND_ALCOHOL, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.FINANCE_AND_ID, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.HEALTH, reviewed = true, isPrisonResponsible = true, isProbationResponsible = false, notStarted = 1, inProgress = 1, met = 0, declined = 0, lastUpdated = LocalDate.parse("2025-09-16")),
      ),
      "A2" to listOf(
        SupportNeedSummary(pathway = Pathway.ACCOMMODATION, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, reviewed = true, isPrisonResponsible = false, isProbationResponsible = true, notStarted = 1, inProgress = 1, met = 1, declined = 0, lastUpdated = LocalDate.parse("2025-09-20")),
        SupportNeedSummary(pathway = Pathway.DRUGS_AND_ALCOHOL, reviewed = true, isPrisonResponsible = true, isProbationResponsible = true, notStarted = 0, inProgress = 1, met = 0, declined = 1, lastUpdated = LocalDate.parse("2025-09-21")),
        SupportNeedSummary(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.FINANCE_AND_ID, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.HEALTH, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
      ),
      "A3" to listOf(
        SupportNeedSummary(pathway = Pathway.ACCOMMODATION, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.DRUGS_AND_ALCOHOL, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
        SupportNeedSummary(pathway = Pathway.EDUCATION_SKILLS_AND_WORK, reviewed = true, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 1, declined = 0, lastUpdated = LocalDate.parse("2025-09-12")),
        SupportNeedSummary(pathway = Pathway.FINANCE_AND_ID, reviewed = true, isPrisonResponsible = true, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 1, lastUpdated = LocalDate.parse("2025-09-30")),
        SupportNeedSummary(pathway = Pathway.HEALTH, reviewed = false, isPrisonResponsible = false, isProbationResponsible = false, notStarted = 0, inProgress = 0, met = 0, declined = 0, lastUpdated = null),
      ),
    )

    Assertions.assertEquals(expectedSupportNeedsSummaryMap, supportNeedsService.getNeedsSummaryToNomsIdMapByPrisonId(prisonId))
  }

  @Test
  fun `test getPathwayNeedsSummaryByNomsId - happy path`() {
    val nomsId = "A123"
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))
    whenever(prisonerSupportNeedRepository.findAllByPrisonerIdAndSupportNeedPathwayAndDeletedIsFalse(1, Pathway.ACCOMMODATION)).thenReturn(
      listOf(
        PrisonerSupportNeedEntity(id = 1, prisonerId = 1, supportNeed = getSupportNeed(n = 1, pathway = Pathway.ACCOMMODATION), otherDetail = null, createdBy = "A user", createdDate = LocalDateTime.parse("2025-01-29T12:09:34")),
        PrisonerSupportNeedEntity(id = 2, prisonerId = 1, supportNeed = getSupportNeed(n = 2, pathway = Pathway.ACCOMMODATION), otherDetail = null, createdBy = "A user", createdDate = LocalDateTime.parse("2025-01-29T12:09:34")),
        PrisonerSupportNeedEntity(id = 3, prisonerId = 1, supportNeed = getSupportNeed(n = 3, pathway = Pathway.ACCOMMODATION, excludeFromCount = true), otherDetail = null, createdBy = "A user", createdDate = LocalDateTime.parse("2025-01-29T12:09:34")),
        PrisonerSupportNeedEntity(id = 4, prisonerId = 1, supportNeed = getSupportNeed(n = 4, pathway = Pathway.ACCOMMODATION, allowOtherDetail = true), otherDetail = "Other support need 1", createdBy = "A user", createdDate = LocalDateTime.parse("2025-01-29T12:09:34")),
        PrisonerSupportNeedEntity(id = 5, prisonerId = 1, supportNeed = getSupportNeed(n = 5, pathway = Pathway.ACCOMMODATION, allowOtherDetail = true), otherDetail = "Other support need 2", createdBy = "A user", createdDate = LocalDateTime.parse("2025-01-29T12:09:34")),
      ),
    )
    whenever(prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(1)).thenReturn(
      listOf(
        PrisonerSupportNeedUpdateEntity(id = 1, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-12T12:09:34"), updateText = "This is some update text 1", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 2, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-11T12:09:34"), updateText = "This is some update text 2", status = SupportNeedStatus.MET, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 3, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-10T12:09:34"), updateText = "This is some update text 3", status = SupportNeedStatus.NOT_STARTED, isPrison = true, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 4, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-09T12:09:34"), updateText = "This is some update text 4", status = SupportNeedStatus.IN_PROGRESS, isPrison = false, isProbation = false),
      ),
    )
    whenever(prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(2)).thenReturn(emptyList())
    whenever(prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(3)).thenReturn(emptyList())
    whenever(prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(4)).thenReturn(
      listOf(
        PrisonerSupportNeedUpdateEntity(id = 5, prisonerSupportNeedId = 4, createdBy = "User B", createdDate = LocalDateTime.parse("2024-12-12T12:09:34"), updateText = "This is some update text 1", status = SupportNeedStatus.NOT_STARTED, isPrison = false, isProbation = false),
        PrisonerSupportNeedUpdateEntity(id = 6, prisonerSupportNeedId = 4, createdBy = "User B", createdDate = LocalDateTime.parse("2024-12-11T12:09:34"), updateText = "This is some update text 2", status = SupportNeedStatus.MET, isPrison = false, isProbation = true),
      ),
    )
    whenever(prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(5)).thenReturn(
      listOf(
        PrisonerSupportNeedUpdateEntity(id = 7, prisonerSupportNeedId = 5, createdBy = "User B", createdDate = LocalDateTime.parse("2024-12-15T12:09:34"), updateText = "This is some update text 1", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 8, prisonerSupportNeedId = 5, createdBy = "User C", createdDate = LocalDateTime.parse("2024-12-14T12:09:34"), updateText = "This is some update text 2", status = SupportNeedStatus.MET, isPrison = false, isProbation = true),
      ),
    )

    val expectedPathwayNeedsSummary = PathwayNeedsSummary(
      prisonerNeeds = listOf(
        PrisonerNeed(id = 1, title = "Title 1", isPrisonResponsible = false, isProbationResponsible = true, status = SupportNeedStatus.DECLINED, numberOfUpdates = 4, lastUpdated = LocalDate.parse("2024-12-12")),
        PrisonerNeed(id = 4, title = "Other support need 1", isPrisonResponsible = false, isProbationResponsible = false, status = SupportNeedStatus.NOT_STARTED, numberOfUpdates = 2, lastUpdated = LocalDate.parse("2024-12-12")),
        PrisonerNeed(id = 5, title = "Other support need 2", isPrisonResponsible = true, isProbationResponsible = true, status = SupportNeedStatus.IN_PROGRESS, numberOfUpdates = 2, lastUpdated = LocalDate.parse("2024-12-15")),
      ),
    )

    Assertions.assertEquals(expectedPathwayNeedsSummary, supportNeedsService.getPathwayNeedsSummaryByNomsId(nomsId, Pathway.ACCOMMODATION))
  }

  @Test
  fun `test getPathwayNeedsSummaryByNomsId - no results`() {
    val nomsId = "A123"
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))
    whenever(prisonerSupportNeedRepository.findAllByPrisonerIdAndSupportNeedPathwayAndDeletedIsFalse(1, Pathway.ACCOMMODATION)).thenReturn(emptyList())

    val expectedPathwayNeedsSummary = PathwayNeedsSummary(prisonerNeeds = emptyList())
    Assertions.assertEquals(expectedPathwayNeedsSummary, supportNeedsService.getPathwayNeedsSummaryByNomsId(nomsId, Pathway.ACCOMMODATION))
  }
}
