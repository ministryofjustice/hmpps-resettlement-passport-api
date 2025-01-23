package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedUpdateRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedWithNomsIdAndLatestUpdateProjection

@Service
class SupportNeedsService(
  private val prisonerSupportNeedRepository: PrisonerSupportNeedRepository,
  private val prisonerSupportNeedUpdateRepository: PrisonerSupportNeedUpdateRepository,
  private val prisonerRepository: PrisonerRepository,
) {

  fun getNeedsSummary(prisonerId: Long?): List<SupportNeedSummary> {
    if (prisonerId != null) {
      val prisonerSupportNeeds = prisonerSupportNeedRepository.findAllByPrisonerIdAndDeletedIsFalse(prisonerId)
      if (prisonerSupportNeeds.isNotEmpty()) {
        val prisonerSupportNeedToLatestUpdateMap = prisonerSupportNeeds.associateWith { prisonerSupportNeedUpdateRepository.findFirstByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(it.id!!) }
        return Pathway.entries.map {
          SupportNeedSummary(
            pathway = it,
            reviewed = isPathwayReviewed(it, prisonerSupportNeedToLatestUpdateMap),
            notStarted = getCountForStatus(it, SupportNeedStatus.NOT_STARTED, prisonerSupportNeedToLatestUpdateMap),
            inProgress = getCountForStatus(it, SupportNeedStatus.IN_PROGRESS, prisonerSupportNeedToLatestUpdateMap),
            met = getCountForStatus(it, SupportNeedStatus.MET, prisonerSupportNeedToLatestUpdateMap),
            declined = getCountForStatus(it, SupportNeedStatus.DECLINED, prisonerSupportNeedToLatestUpdateMap),
            lastUpdated = getLastUpdatedForPathway(it, prisonerSupportNeedToLatestUpdateMap),
          )
        }
      }
    }
    return getDefaultNeedsSummary()
  }

  fun getDefaultNeedsSummary() = Pathway.entries.map {
    SupportNeedSummary(
      pathway = it,
      reviewed = false,
      notStarted = 0,
      inProgress = 0,
      met = 0,
      declined = 0,
      lastUpdated = null,
    )
  }

  fun isPathwayReviewed(pathway: Pathway, prisonerSupportNeedToLatestUpdateMap: Map<PrisonerSupportNeedEntity, PrisonerSupportNeedUpdateEntity?>) =
    prisonerSupportNeedToLatestUpdateMap.filter { it.key.supportNeed.pathway == pathway }.isNotEmpty()

  fun getCountForStatus(pathway: Pathway, status: SupportNeedStatus, prisonerSupportNeedToLatestUpdateMap: Map<PrisonerSupportNeedEntity, PrisonerSupportNeedUpdateEntity?>) =
    prisonerSupportNeedToLatestUpdateMap.filter { it.key.supportNeed.pathway == pathway }
      .filter { !it.key.supportNeed.excludeFromCount && it.value?.status == status }
      .count()

  fun getLastUpdatedForPathway(pathway: Pathway, prisonerSupportNeedToLatestUpdateMap: Map<PrisonerSupportNeedEntity, PrisonerSupportNeedUpdateEntity?>) =
    prisonerSupportNeedToLatestUpdateMap.filter { it.key.supportNeed.pathway == pathway }
      .map { if (it.value != null) it.value?.createdDate?.toLocalDate() else it.key.createdDate.toLocalDate() }
      .sortedByDescending { it }
      .firstOrNull()

  fun isPathwayReviewed(pathway: Pathway, prisonerSupportNeeds: List<PrisonerSupportNeedWithNomsIdAndLatestUpdateProjection>) =
    prisonerSupportNeeds.any { it.pathway == pathway }

  fun getCountForStatus(pathway: Pathway, status: SupportNeedStatus, prisonerSupportNeeds: List<PrisonerSupportNeedWithNomsIdAndLatestUpdateProjection>) =
    prisonerSupportNeeds.count { it.pathway == pathway && it.latestUpdateStatus == status }

  fun getLastUpdatedForPathway(pathway: Pathway, prisonerSupportNeeds: List<PrisonerSupportNeedWithNomsIdAndLatestUpdateProjection>) =
    prisonerSupportNeeds.filter { it.pathway == pathway }
      .map { if (it.latestUpdateId != null) it.latestUpdateCreatedDate?.toLocalDate() else it.prisonerSupportNeedCreatedDate.toLocalDate() }
      .sortedByDescending { it }
      .firstOrNull()

  fun getNeedsSummaryByNomsId(nomsId: String): SupportNeedSummaryResponse {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Cannot find prisoner $nomsId")
    return SupportNeedSummaryResponse(getNeedsSummary(prisoner.id))
  }

  fun getNeedsSummaryToNomsIdMapByPrisonId(prisonId: String): Map<String, List<SupportNeedSummary>> {
    // Get all prisoner support needs and updates from database related prisoners in the prison
    val prisonerSupportNeedsFromDatabase = prisonerSupportNeedRepository.getPrisonerSupportNeedsByPrisonId(prisonId)

    // Need to group by the prisoner then convert into a support needs summary
    val nomsIdToPrisonerSupportNeedsMap = prisonerSupportNeedsFromDatabase.groupBy { it.nomsId }
    return nomsIdToPrisonerSupportNeedsMap.entries.associate { it.key to Pathway.entries.map { pathway ->
      SupportNeedSummary(
        pathway = pathway,
        reviewed = isPathwayReviewed(pathway, it.value),
        notStarted = getCountForStatus(pathway, SupportNeedStatus.NOT_STARTED, it.value),
        inProgress = getCountForStatus(pathway, SupportNeedStatus.IN_PROGRESS, it.value),
        met = getCountForStatus(pathway, SupportNeedStatus.MET, it.value),
        declined = getCountForStatus(pathway, SupportNeedStatus.DECLINED, it.value),
        lastUpdated = getLastUpdatedForPathway(pathway, it.value),
      )
    }}
  }

}
