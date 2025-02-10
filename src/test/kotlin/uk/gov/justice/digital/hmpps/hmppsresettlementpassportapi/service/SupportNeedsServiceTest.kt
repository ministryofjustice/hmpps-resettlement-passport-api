package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayNeedsSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeed
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedIdAndTitle
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedWithUpdates
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedsRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeed
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedUpdate
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedUpdates
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeeds
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedsUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.SupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedUpdateRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.SupportNeedRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream

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

  @Mock
  private lateinit var supportNeedRepository: SupportNeedRepository

  @BeforeEach
  fun beforeEach() {
    supportNeedsService = SupportNeedsService(prisonerSupportNeedRepository, prisonerSupportNeedUpdateRepository, prisonerRepository, supportNeedRepository)
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

  private fun getPrisonerSupportNeed(n: Int, pathway: Pathway, excludeFromCount: Boolean = false, includeLatestUpdate: Boolean = false) = PrisonerSupportNeedEntity(
    id = n.toLong(),
    prisonerId = 1,
    supportNeed = getSupportNeed(n, pathway, excludeFromCount),
    otherDetail = null,
    createdBy = "Someone",
    createdDate = LocalDateTime.parse("2023-09-12T12:10:00"),
    latestUpdateId = if (includeLatestUpdate) n.toLong() else null,
  )

  private fun getSupportNeed(n: Int, pathway: Pathway, excludeFromCount: Boolean = false, allowOtherDetail: Boolean = false, hidden: Boolean = false) = SupportNeedEntity(
    id = n.toLong(),
    pathway = pathway,
    section = "Section $n",
    title = "Title $n",
    hidden = hidden,
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

  @ParameterizedTest
  @MethodSource("test getTitleFromPrisonerSupportNeed data")
  fun `test getTitleFromPrisonerSupportNeed`(prisonerSupportNeed: PrisonerSupportNeedEntity, expectedTitle: String) {
    Assertions.assertEquals(expectedTitle, supportNeedsService.getTitleFromPrisonerSupportNeed(prisonerSupportNeed))
  }

  private fun `test getTitleFromPrisonerSupportNeed data`() = Stream.of(
    Arguments.of(
      PrisonerSupportNeedEntity(
        id = 1,
        prisonerId = 1,
        supportNeed = SupportNeedEntity(id = 1, pathway = Pathway.ACCOMMODATION, section = "A section", title = "Title 1", hidden = false, excludeFromCount = false, allowOtherDetail = false, LocalDateTime.now()),
        otherDetail = null,
        createdBy = "User A",
        createdDate = LocalDateTime.now(),
      ),
      "Title 1",
    ),
    Arguments.of(
      PrisonerSupportNeedEntity(
        id = 2,
        prisonerId = 1,
        supportNeed = SupportNeedEntity(id = 1, pathway = Pathway.ACCOMMODATION, section = "A section", title = "Other", hidden = false, excludeFromCount = false, allowOtherDetail = true, LocalDateTime.now()),
        otherDetail = "Other need",
        createdBy = "User A",
        createdDate = LocalDateTime.now(),
      ),
      "Other need",
    ),
    Arguments.of(
      PrisonerSupportNeedEntity(
        id = 3,
        prisonerId = 1,
        supportNeed = SupportNeedEntity(id = 1, pathway = Pathway.ACCOMMODATION, section = "A section", title = "Other", hidden = false, excludeFromCount = false, allowOtherDetail = true, LocalDateTime.now()),
        otherDetail = null,
        createdBy = "User A",
        createdDate = LocalDateTime.now(),
      ),
      "Other",
    ),
  )

  @ParameterizedTest
  @MethodSource("test getPathwayUpdatesByNomsId data")
  fun `test getPathwayUpdatesByNomsId`(page: Int, size: Int, sort: String, prisonerSupportNeedsId: Long?, expectedResult: SupportNeedUpdates) {
    val nomsId = "A123"
    val pathway = Pathway.ACCOMMODATION

    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))
    whenever(prisonerSupportNeedRepository.findAllByPrisonerIdAndSupportNeedPathwayAndDeletedIsFalse(1, pathway)).thenReturn(
      listOf(
        getPrisonerSupportNeed(1, pathway),
        getPrisonerSupportNeed(2, pathway),
        getPrisonerSupportNeed(3, pathway),
        getPrisonerSupportNeed(4, pathway),
        getPrisonerSupportNeed(5, pathway),
      ),
    )
    whenever(prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdInAndDeletedIsFalse(listOf(1, 2, 3, 4, 5))).thenReturn(
      listOf(
        PrisonerSupportNeedUpdateEntity(id = 1, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-10T12:09:34"), updateText = "This is some update text 1", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 2, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-15T12:09:34"), updateText = "This is some update text 2", status = SupportNeedStatus.MET, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 3, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-13T12:09:34"), updateText = "This is some update text 3", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false),
        PrisonerSupportNeedUpdateEntity(id = 4, prisonerSupportNeedId = 2, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-18T12:09:34"), updateText = "This is some update text 4", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 5, prisonerSupportNeedId = 2, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-17T12:09:34"), updateText = "This is some update text 5", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 6, prisonerSupportNeedId = 2, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-11T12:09:34"), updateText = "This is some update text 6", status = SupportNeedStatus.NOT_STARTED, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 7, prisonerSupportNeedId = 2, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-19T12:09:34"), updateText = "This is some update text 7", status = SupportNeedStatus.NOT_STARTED, isPrison = false, isProbation = false),
        PrisonerSupportNeedUpdateEntity(id = 8, prisonerSupportNeedId = 3, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-12T12:09:34"), updateText = "This is some update text 8", status = SupportNeedStatus.MET, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 9, prisonerSupportNeedId = 3, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-20T12:09:34"), updateText = "This is some update text 9", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 10, prisonerSupportNeedId = 4, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-21T12:09:34"), updateText = "This is some update text 10", status = SupportNeedStatus.MET, isPrison = true, isProbation = false),
        PrisonerSupportNeedUpdateEntity(id = 11, prisonerSupportNeedId = 4, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-16T12:09:34"), updateText = "This is some update text 11", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = true),
      ),
    )
    Assertions.assertEquals(expectedResult, supportNeedsService.getPathwayUpdatesByNomsId(nomsId, pathway, page, size, sort, prisonerSupportNeedsId))
  }

  private fun `test getPathwayUpdatesByNomsId data`() = Stream.of(
    Arguments.of(
      0,
      11,
      "createdDate,DESC",
      null,
      getExpectedSupportNeedUpdates(listOf(10, 9, 7, 4, 5, 11, 2, 3, 8, 6, 1), 0, 11, "createdDate,DESC", true),
    ),
    Arguments.of(
      0,
      11,
      "createdDate,DESC",
      1L,
      getExpectedSupportNeedUpdates(listOf(2, 3, 1), 0, 11, "createdDate,DESC", true, 3),
    ),
    Arguments.of(
      0,
      11,
      "createdDate,DESC",
      17L,
      getExpectedSupportNeedUpdates(emptyList(), 0, 11, "createdDate,DESC", true, 0),
    ),
    Arguments.of(
      0,
      20,
      "createdDate,DESC",
      null,
      getExpectedSupportNeedUpdates(listOf(10, 9, 7, 4, 5, 11, 2, 3, 8, 6, 1), 0, 20, "createdDate,DESC", true),
    ),
    Arguments.of(
      0,
      5,
      "createdDate,DESC",
      null,
      getExpectedSupportNeedUpdates(listOf(10, 9, 7, 4, 5), 0, 5, "createdDate,DESC", false),
    ),
    Arguments.of(
      1,
      5,
      "createdDate,DESC",
      null,
      getExpectedSupportNeedUpdates(listOf(11, 2, 3, 8, 6), 1, 5, "createdDate,DESC", false),
    ),
    Arguments.of(
      2,
      5,
      "createdDate,DESC",
      null,
      getExpectedSupportNeedUpdates(listOf(1), 2, 5, "createdDate,DESC", true),
    ),
    Arguments.of(
      0,
      5,
      "createdDate,ASC",
      null,
      getExpectedSupportNeedUpdates(listOf(1, 6, 8, 3, 2), 0, 5, "createdDate,ASC", false),
    ),
    Arguments.of(
      1,
      5,
      "createdDate,ASC",
      null,
      getExpectedSupportNeedUpdates(listOf(11, 5, 4, 7, 9), 1, 5, "createdDate,ASC", false),
    ),
    Arguments.of(
      2,
      5,
      "createdDate,ASC",
      null,
      getExpectedSupportNeedUpdates(listOf(10), 2, 5, "createdDate,ASC", true),
    ),
  )

  private fun getExpectedSupportNeedUpdates(ids: List<Long>, page: Int, size: Int, sort: String, last: Boolean, totalElements: Int = 11) = SupportNeedUpdates(
    updates = getExpectedSupportNeedUpdateUpdates(ids),
    allPrisonerNeeds = getExpectedAllPrisonerNeeds(),
    size = size,
    page = page,
    sortName = sort,
    totalElements = totalElements,
    last = last,
  )

  private fun getExpectedAllPrisonerNeeds() = listOf(PrisonerNeedIdAndTitle(id = 1, title = "Title 1"), PrisonerNeedIdAndTitle(id = 2, title = "Title 2"), PrisonerNeedIdAndTitle(id = 3, title = "Title 3"), PrisonerNeedIdAndTitle(id = 4, title = "Title 4"), PrisonerNeedIdAndTitle(id = 5, title = "Title 5"))

  private fun getExpectedSupportNeedUpdateUpdates(ids: List<Long>): List<SupportNeedUpdate> {
    val completeList = listOf(
      SupportNeedUpdate(id = 1, title = "Title 1", status = SupportNeedStatus.DECLINED, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 1", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-10T12:09:34")),
      SupportNeedUpdate(id = 2, title = "Title 1", status = SupportNeedStatus.MET, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 2", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-15T12:09:34")),
      SupportNeedUpdate(id = 3, title = "Title 1", status = SupportNeedStatus.IN_PROGRESS, isPrisonResponsible = true, isProbationResponsible = false, text = "This is some update text 3", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-13T12:09:34")),
      SupportNeedUpdate(id = 4, title = "Title 2", status = SupportNeedStatus.DECLINED, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 4", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-18T12:09:34")),
      SupportNeedUpdate(id = 5, title = "Title 2", status = SupportNeedStatus.DECLINED, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 5", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-17T12:09:34")),
      SupportNeedUpdate(id = 6, title = "Title 2", status = SupportNeedStatus.NOT_STARTED, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 6", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-11T12:09:34")),
      SupportNeedUpdate(id = 7, title = "Title 2", status = SupportNeedStatus.NOT_STARTED, isPrisonResponsible = false, isProbationResponsible = false, text = "This is some update text 7", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-19T12:09:34")),
      SupportNeedUpdate(id = 8, title = "Title 3", status = SupportNeedStatus.MET, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 8", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-12T12:09:34")),
      SupportNeedUpdate(id = 9, title = "Title 3", status = SupportNeedStatus.DECLINED, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 9", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-20T12:09:34")),
      SupportNeedUpdate(id = 10, title = "Title 4", status = SupportNeedStatus.MET, isPrisonResponsible = true, isProbationResponsible = false, text = "This is some update text 10", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-21T12:09:34")),
      SupportNeedUpdate(id = 11, title = "Title 4", status = SupportNeedStatus.IN_PROGRESS, isPrisonResponsible = true, isProbationResponsible = true, text = "This is some update text 11", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-16T12:09:34")),
    )
    return ids.map { id -> completeList.first { it.id == id } }
  }

  @Test
  fun `test getPathwayUpdatesByNomsId - no results from database`() {
    val nomsId = "A123"
    val pathway = Pathway.ACCOMMODATION

    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))
    whenever(prisonerSupportNeedRepository.findAllByPrisonerIdAndSupportNeedPathwayAndDeletedIsFalse(1, pathway)).thenReturn(emptyList())

    val expectedResult = SupportNeedUpdates(
      updates = emptyList(),
      allPrisonerNeeds = emptyList(),
      size = 5,
      page = 0,
      sortName = "createdDate,DESC",
      totalElements = 0,
      last = true,
    )
    Assertions.assertEquals(expectedResult, supportNeedsService.getPathwayUpdatesByNomsId(nomsId, pathway, 0, 5, "createdDate,DESC", null))
  }

  @Test
  fun `test getPathwayUpdatesByNomsId - invalid sort`() {
    val nomsId = "A123"
    val pathway = Pathway.ACCOMMODATION
    assertThrows<ServerWebInputException> { supportNeedsService.getPathwayUpdatesByNomsId(nomsId, pathway, 0, 5, "otherColumn,DESC", null) }
  }

  @Test
  fun `test getPathwayNeedsByNomsId`() {
    val nomsId = "A123"
    val pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR

    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))
    whenever(supportNeedRepository.findByPathwayAndDeletedIsFalse(pathway)).thenReturn(
      listOf(
        getSupportNeed(n = 1, pathway = pathway),
        getSupportNeed(n = 2, pathway = pathway),
        getSupportNeed(n = 3, pathway = pathway),
        getSupportNeed(n = 4, pathway = pathway, allowOtherDetail = true),
        getSupportNeed(n = 5, pathway = pathway, excludeFromCount = true),
        getSupportNeed(n = 6, pathway = pathway, hidden = true),
      ),
    )
    whenever(prisonerSupportNeedRepository.findAllByPrisonerIdAndSupportNeedPathwayAndDeletedIsFalse(1, pathway)).thenReturn(
      listOf(
        PrisonerSupportNeedEntity(
          id = 11,
          prisonerId = 1,
          supportNeed = getSupportNeed(n = 1, pathway = pathway),
          otherDetail = null,
          createdBy = "Someone",
          createdDate = LocalDateTime.parse("2023-09-12T12:10:00"),
          latestUpdateId = null,
        ),
        PrisonerSupportNeedEntity(
          id = 12,
          prisonerId = 1,
          supportNeed = getSupportNeed(n = 2, pathway = pathway),
          otherDetail = null,
          createdBy = "Someone",
          createdDate = LocalDateTime.parse("2023-09-12T12:10:00"),
          latestUpdateId = null,
        ),
        PrisonerSupportNeedEntity(
          id = 14,
          prisonerId = 1,
          supportNeed = getSupportNeed(n = 4, pathway = pathway, allowOtherDetail = true),
          otherDetail = "This is other 1",
          createdBy = "Someone",
          createdDate = LocalDateTime.parse("2023-09-12T12:10:00"),
          latestUpdateId = null,
        ),
        PrisonerSupportNeedEntity(
          id = 15,
          prisonerId = 1,
          supportNeed = getSupportNeed(n = 4, pathway = pathway, allowOtherDetail = true),
          otherDetail = "This is other 2",
          createdBy = "Someone",
          createdDate = LocalDateTime.parse("2023-09-12T12:10:00"),
          latestUpdateId = null,
        ),
      ),
    )

    val expectedResult = SupportNeeds(
      supportNeeds = listOf(
        SupportNeed(id = 1, title = "Title 1", category = "Section 1", allowUserDesc = false, isOther = false, isUpdatable = true, existingPrisonerSupportNeedId = 11),
        SupportNeed(id = 2, title = "Title 2", category = "Section 2", allowUserDesc = false, isOther = false, isUpdatable = true, existingPrisonerSupportNeedId = 12),
        SupportNeed(id = 3, title = "Title 3", category = "Section 3", allowUserDesc = false, isOther = false, isUpdatable = true, existingPrisonerSupportNeedId = null),
        SupportNeed(id = 4, title = "Title 4", category = "Section 4", allowUserDesc = true, isOther = false, isUpdatable = true, existingPrisonerSupportNeedId = null),
        SupportNeed(id = 5, title = "Title 5", category = "Section 5", allowUserDesc = false, isOther = false, isUpdatable = false, existingPrisonerSupportNeedId = null),
        SupportNeed(id = 4, title = "This is other 1", category = "Section 4", allowUserDesc = false, isOther = true, isUpdatable = true, existingPrisonerSupportNeedId = 14),
        SupportNeed(id = 4, title = "This is other 2", category = "Section 4", allowUserDesc = false, isOther = true, isUpdatable = true, existingPrisonerSupportNeedId = 15),
      ),
    )

    Assertions.assertEquals(expectedResult, supportNeedsService.getPathwayNeedsByNomsId(nomsId, pathway))
  }

  @Test
  fun `test getPrisonerNeedById - no prisoner`() {
    val nomsId = "A123"
    val exception = assertThrows<ResourceNotFoundException> { supportNeedsService.getPrisonerNeedById(nomsId, 3) }
    Assertions.assertEquals("Cannot find prisoner A123", exception.message)
  }

  @Test
  fun `test getPrisonerNeedById - no prisoner support need`() {
    val nomsId = "A123"
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))
    val exception = assertThrows<ResourceNotFoundException> { supportNeedsService.getPrisonerNeedById(nomsId, 3) }
    Assertions.assertEquals("Cannot find prisoner support need 3", exception.message)
  }

  @Test
  fun `test getPrisonerNeedById - prisoner and support need mismatch`() {
    val nomsId = "A123"
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))
    whenever(prisonerSupportNeedRepository.findByIdAndDeletedIsFalse(3)).thenReturn(PrisonerSupportNeedEntity(id = 2, prisonerId = 2, supportNeed = getSupportNeed(1, Pathway.HEALTH), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.now()))

    val exception = assertThrows<ResourceNotFoundException> { supportNeedsService.getPrisonerNeedById(nomsId, 3) }
    Assertions.assertEquals("Prisoner support need 3 is not associated with prisoner A123", exception.message)
  }

  @Test
  fun `test getPrisonerNeedById - no updates available`() {
    val nomsId = "A123"
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))
    whenever(prisonerSupportNeedRepository.findByIdAndDeletedIsFalse(3)).thenReturn(PrisonerSupportNeedEntity(id = 2, prisonerId = 1, supportNeed = getSupportNeed(1, Pathway.HEALTH), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.now()))
    whenever(prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(3)).thenReturn(emptyList())

    val exception = assertThrows<ServerWebInputException> { supportNeedsService.getPrisonerNeedById(nomsId, 3) }
    Assertions.assertEquals("400 BAD_REQUEST \"Cannot get prisoner support need as there are no updates available\"", exception.message)
  }

  @Test
  fun `test getPrisonerNeedById - happy path`() {
    val nomsId = "A123"
    val prisonerSupportNeedId = 3L

    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))
    whenever(prisonerSupportNeedRepository.findByIdAndDeletedIsFalse(prisonerSupportNeedId)).thenReturn(PrisonerSupportNeedEntity(id = 2, prisonerId = 1, supportNeed = getSupportNeed(1, Pathway.HEALTH), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.now()))
    whenever(prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(prisonerSupportNeedId)).thenReturn(
      listOf(
        PrisonerSupportNeedUpdateEntity(id = 4, prisonerSupportNeedId = 2, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-18T12:09:34"), updateText = "This is some update text 4", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 5, prisonerSupportNeedId = 2, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-17T12:09:34"), updateText = "This is some update text 5", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 2, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-15T12:09:34"), updateText = "This is some update text 2", status = SupportNeedStatus.MET, isPrison = false, isProbation = true),
        PrisonerSupportNeedUpdateEntity(id = 3, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-13T12:09:34"), updateText = "This is some update text 3", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false),
        PrisonerSupportNeedUpdateEntity(id = 1, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-12-10T12:09:34"), updateText = "This is some update text 1", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true),
      ),
    )

    val expectedPrisonerNeeds = PrisonerNeedWithUpdates(
      title = "Title 1",
      isPrisonResponsible = false,
      isProbationResponsible = true,
      status = SupportNeedStatus.DECLINED,
      previousUpdates = listOf(
        SupportNeedUpdate(id = 4, title = "Title 1", status = SupportNeedStatus.DECLINED, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 4", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-18T12:09:34")),
        SupportNeedUpdate(id = 5, title = "Title 1", status = SupportNeedStatus.DECLINED, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 5", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-17T12:09:34")),
        SupportNeedUpdate(id = 2, title = "Title 1", status = SupportNeedStatus.MET, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 2", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-15T12:09:34")),
        SupportNeedUpdate(id = 3, title = "Title 1", status = SupportNeedStatus.IN_PROGRESS, isPrisonResponsible = true, isProbationResponsible = false, text = "This is some update text 3", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-13T12:09:34")),
        SupportNeedUpdate(id = 1, title = "Title 1", status = SupportNeedStatus.DECLINED, isPrisonResponsible = false, isProbationResponsible = true, text = "This is some update text 1", createdBy = "User A", createdAt = LocalDateTime.parse("2024-12-10T12:09:34")),
      ),
    )

    Assertions.assertEquals(expectedPrisonerNeeds, supportNeedsService.getPrisonerNeedById(nomsId, prisonerSupportNeedId))
  }

  @Test
  fun `test postSupportNeeds - happy path`() {
    val nomsId = "A123"
    val prisonerNeedsRequest = PrisonerNeedsRequest(
      needs = listOf(
        PrisonerNeedRequest(
          needId = 8,
          prisonerSupportNeedId = null,
          otherDesc = null,
          text = "This is an update 1",
          status = SupportNeedStatus.NOT_STARTED,
          isPrisonResponsible = true,
          isProbationResponsible = false,
        ),
      ),
    )
    val auth = "auth"
    val fakeNow = LocalDateTime.parse("2025-01-31T12:00:01")

    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))

    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken(auth, "name") } returns "A User"

    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    whenever(prisonerSupportNeedRepository.findFirstBySupportNeedIdAndOtherDetailAndDeletedIsFalseOrderByCreatedDateDesc(8, null)).thenReturn(null)
    whenever(supportNeedRepository.findByIdAndDeletedIsFalse(8)).thenReturn(getSupportNeed(8, Pathway.HEALTH))
    whenever(prisonerSupportNeedRepository.save(PrisonerSupportNeedEntity(prisonerId = 1, supportNeed = getSupportNeed(8, Pathway.HEALTH), otherDetail = null, createdBy = "A User", createdDate = fakeNow))).thenReturn(PrisonerSupportNeedEntity(id = 9, prisonerId = 1, supportNeed = getSupportNeed(8, Pathway.HEALTH), otherDetail = null, createdBy = "A User", createdDate = fakeNow))
    whenever(prisonerSupportNeedUpdateRepository.save(PrisonerSupportNeedUpdateEntity(prisonerSupportNeedId = 9, createdBy = "A User", createdDate = fakeNow, updateText = "This is an update 1", status = SupportNeedStatus.NOT_STARTED, isPrison = true, isProbation = false)))
      .thenReturn(PrisonerSupportNeedUpdateEntity(id = 12, prisonerSupportNeedId = 9, createdBy = "A User", createdDate = fakeNow, updateText = "This is an update 1", status = SupportNeedStatus.NOT_STARTED, isPrison = true, isProbation = false))

    supportNeedsService.postSupportNeeds(nomsId, prisonerNeedsRequest, auth)

    verify(prisonerSupportNeedRepository).save(PrisonerSupportNeedEntity(id = 9, prisonerId = 1, supportNeed = getSupportNeed(8, Pathway.HEALTH), otherDetail = null, createdBy = "A User", createdDate = fakeNow, latestUpdateId = 12))

    unmockkAll()
  }

  @Test
  fun `test patchSupportNeeds - happy path`() {
    val nomsId = "A123"
    val prisonerNeedId = 1234L
    val supportNeedsUpdateRequest = SupportNeedsUpdateRequest(
      text = "Some support need text",
      isPrisonResponsible = true,
      isProbationResponsible = false,
      status = SupportNeedStatus.IN_PROGRESS,
    )

    val auth = "auth"
    val fakeNow = LocalDateTime.parse("2025-01-31T12:00:01")

    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken(auth, "name") } returns "A User"

    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.parse("2025-01-28T12:09:34"), prisonId = "MDI"))
    whenever(prisonerSupportNeedRepository.findByIdAndDeletedIsFalse(prisonerNeedId)).thenReturn(PrisonerSupportNeedEntity(prisonerId = 1, supportNeed = getSupportNeed(8, Pathway.HEALTH), otherDetail = null, createdBy = "A User", createdDate = fakeNow))
    whenever(prisonerSupportNeedUpdateRepository.save(PrisonerSupportNeedUpdateEntity(prisonerSupportNeedId = 1234L, createdBy = "A User", createdDate = fakeNow, updateText = "Some support need text", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false))).thenReturn(PrisonerSupportNeedUpdateEntity(id = 567, prisonerSupportNeedId = 123L, createdBy = "A User", createdDate = fakeNow, updateText = "Some support need text", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false))

    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    supportNeedsService.patchPrisonerNeedById(nomsId, prisonerNeedId, supportNeedsUpdateRequest, auth)

    verify(prisonerSupportNeedRepository).save(PrisonerSupportNeedEntity(prisonerId = 1, latestUpdateId = 567, supportNeed = getSupportNeed(8, Pathway.HEALTH), otherDetail = null, createdBy = "A User", createdDate = fakeNow))

    unmockkAll()
  }

  @Test
  fun `test patchSupportNeeds - no prisoner`() {
    val nomsId = "A123"
    val prisonerNeedId = 1234L
    val supportNeedsUpdateRequest = SupportNeedsUpdateRequest(
      text = "Some support need text",
      isPrisonResponsible = true,
      isProbationResponsible = false,
      status = SupportNeedStatus.IN_PROGRESS,
    )
    val auth = "auth"

    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(null)

    val exception = assertThrows<ResourceNotFoundException> {
      supportNeedsService.patchPrisonerNeedById(nomsId, prisonerNeedId, supportNeedsUpdateRequest, auth)
    }

    Assertions.assertEquals("Cannot find prisoner $nomsId", exception.message)
  }

  @Test
  fun `test patchSupportNeeds - no prisoner support need`() {
    val nomsId = "A123"
    val prisonerNeedId = 1234L
    val supportNeedsUpdateRequest = SupportNeedsUpdateRequest(
      text = "Some support need text",
      isPrisonResponsible = true,
      isProbationResponsible = false,
      status = SupportNeedStatus.IN_PROGRESS,
    )
    val auth = "auth"
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken(auth, "name") } returns "A User"

    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(
      PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.now(), prisonId = "MDI"),
    )
    whenever(prisonerSupportNeedRepository.findByIdAndDeletedIsFalse(prisonerNeedId)).thenReturn(null)

    val exception = assertThrows<ResourceNotFoundException> {
      supportNeedsService.patchPrisonerNeedById(nomsId, prisonerNeedId, supportNeedsUpdateRequest, auth)
    }

    Assertions.assertEquals("Cannot find prisoner support need $prisonerNeedId", exception.message)
  }

  @Test
  fun `test patchSupportNeeds - prisoner and support need mismatch`() {
    val nomsId = "A123"
    val prisonerNeedId = 1234L
    val supportNeedsUpdateRequest = SupportNeedsUpdateRequest(
      text = "Some support need text",
      isPrisonResponsible = true,
      isProbationResponsible = false,
      status = SupportNeedStatus.IN_PROGRESS,
    )
    val auth = "auth"
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken(auth, "name") } returns "A User"

    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(
      PrisonerEntity(id = 1, nomsId = nomsId, creationDate = LocalDateTime.now(), prisonId = "MDI"),
    )
    whenever(prisonerSupportNeedRepository.findByIdAndDeletedIsFalse(prisonerNeedId)).thenReturn(
      PrisonerSupportNeedEntity(
        prisonerId = 2, // Doesn't match prisoner ID
        supportNeed = getSupportNeed(8, Pathway.HEALTH),
        otherDetail = null,
        createdBy = "A User",
        createdDate = LocalDateTime.now(),
      ),
    )

    val exception = assertThrows<ResourceNotFoundException> {
      supportNeedsService.patchPrisonerNeedById(nomsId, prisonerNeedId, supportNeedsUpdateRequest, auth)
    }

    Assertions.assertEquals("Cannot find prisoner support need on prisoner $nomsId", exception.message)
  }
}
