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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedUpdateRepository
import java.time.LocalDate
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class SupportNeedsServiceTest {
  private lateinit var supportNeedsService: SupportNeedsService

  @Mock
  private lateinit var prisonerSupportNeedRepository: PrisonerSupportNeedRepository

  @Mock
  private lateinit var prisonerSupportNeedUpdateRepository: PrisonerSupportNeedUpdateRepository

  @BeforeEach
  fun beforeEach() {
    supportNeedsService = SupportNeedsService(prisonerSupportNeedRepository, prisonerSupportNeedUpdateRepository)
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
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(1)).thenReturn(null)

    val expectedNeedsSummary = listOf(
      SupportNeedSummary(Pathway.ACCOMMODATION, true, 0, 0, 0, 0, null),
      SupportNeedSummary(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, true, 0, 0, 0, 0, null),
      SupportNeedSummary(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, true, 0, 0, 0, 0, null),
      SupportNeedSummary(Pathway.DRUGS_AND_ALCOHOL, true, 0, 0, 0, 0, null),
      SupportNeedSummary(Pathway.EDUCATION_SKILLS_AND_WORK, true, 0, 0, 0, 0, null),
      SupportNeedSummary(Pathway.FINANCE_AND_ID, true, 0, 0, 0, 0, null),
      SupportNeedSummary(Pathway.HEALTH, false, 0, 0, 0, 0, null),
    )

    Assertions.assertEquals(expectedNeedsSummary, supportNeedsService.getNeedsSummary(prisonerId))
  }

  @Test
  fun `test getNeedsSummary - with update records in database`() {
    val prisonerId = 1L
    whenever(prisonerSupportNeedRepository.findAllByPrisonerIdAndDeletedIsFalse(prisonerId)).thenReturn(getPrisonerSupportNeeds())
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(1)).thenReturn(getPrisonerSupportNeedUpdate(1, SupportNeedStatus.MET, "12"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(2)).thenReturn(getPrisonerSupportNeedUpdate(2, SupportNeedStatus.NOT_STARTED, "11"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(3)).thenReturn(getPrisonerSupportNeedUpdate(3, SupportNeedStatus.DECLINED, "13"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(4)).thenReturn(getPrisonerSupportNeedUpdate(4, SupportNeedStatus.DECLINED, "10"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(5)).thenReturn(getPrisonerSupportNeedUpdate(5, SupportNeedStatus.NOT_STARTED, "14"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(6)).thenReturn(getPrisonerSupportNeedUpdate(6, SupportNeedStatus.MET, "08"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(7)).thenReturn(getPrisonerSupportNeedUpdate(7, SupportNeedStatus.DECLINED, "10"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(8)).thenReturn(getPrisonerSupportNeedUpdate(8, SupportNeedStatus.IN_PROGRESS, "23"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(9)).thenReturn(getPrisonerSupportNeedUpdate(9, SupportNeedStatus.MET, "01"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(10)).thenReturn(getPrisonerSupportNeedUpdate(10, SupportNeedStatus.DECLINED, "09"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(11)).thenReturn(getPrisonerSupportNeedUpdate(11, SupportNeedStatus.MET, "15"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(12)).thenReturn(getPrisonerSupportNeedUpdate(12, SupportNeedStatus.IN_PROGRESS, "03"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(13)).thenReturn(getPrisonerSupportNeedUpdate(13, SupportNeedStatus.NOT_STARTED, "19"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(14)).thenReturn(getPrisonerSupportNeedUpdate(14, SupportNeedStatus.MET, "17"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(15)).thenReturn(getPrisonerSupportNeedUpdate(15, SupportNeedStatus.MET, "22"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(16)).thenReturn(getPrisonerSupportNeedUpdate(16, SupportNeedStatus.IN_PROGRESS, "20"))
    whenever(prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(17)).thenReturn(getPrisonerSupportNeedUpdate(17, SupportNeedStatus.DECLINED, "07"))

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
    getPrisonerSupportNeed(1, Pathway.ACCOMMODATION),
    getPrisonerSupportNeed(2, Pathway.ACCOMMODATION),
    getPrisonerSupportNeed(3, Pathway.ACCOMMODATION),
    getPrisonerSupportNeed(4, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR),
    getPrisonerSupportNeed(5, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR),
    getPrisonerSupportNeed(6, Pathway.CHILDREN_FAMILIES_AND_COMMUNITY),
    getPrisonerSupportNeed(7, Pathway.DRUGS_AND_ALCOHOL, true),
    getPrisonerSupportNeed(8, Pathway.EDUCATION_SKILLS_AND_WORK),
    getPrisonerSupportNeed(9, Pathway.EDUCATION_SKILLS_AND_WORK),
    getPrisonerSupportNeed(10, Pathway.EDUCATION_SKILLS_AND_WORK),
    getPrisonerSupportNeed(11, Pathway.EDUCATION_SKILLS_AND_WORK),
    getPrisonerSupportNeed(12, Pathway.FINANCE_AND_ID),
    getPrisonerSupportNeed(13, Pathway.FINANCE_AND_ID),
    getPrisonerSupportNeed(14, Pathway.FINANCE_AND_ID),
    getPrisonerSupportNeed(15, Pathway.FINANCE_AND_ID, true),
    getPrisonerSupportNeed(16, Pathway.FINANCE_AND_ID),
    getPrisonerSupportNeed(17, Pathway.FINANCE_AND_ID),
  )

  private fun getPrisonerSupportNeed(n: Int, pathway: Pathway, excludeFromCount: Boolean = false) =
    PrisonerSupportNeedEntity(
      id = n.toLong(),
      prisonerId = 1,
      supportNeed = getSupportNeed(n, pathway, excludeFromCount),
      otherDetail = null,
      createdBy = "Someone",
      createdDate = LocalDateTime.parse("2023-09-12T12:10:00"),
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
}
