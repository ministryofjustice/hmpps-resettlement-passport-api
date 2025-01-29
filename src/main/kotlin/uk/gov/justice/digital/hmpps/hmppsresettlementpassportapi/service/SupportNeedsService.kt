package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayNeedsSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeed
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerSupportNeedWithNomsIdAndLatestUpdate
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedUpdateRepository
import java.time.LocalDateTime

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
        val prisonerSupportNeedWithLatestUpdate = prisonerSupportNeeds.associateWith { if (it.latestUpdateId != null) prisonerSupportNeedUpdateRepository.findById(it.latestUpdateId!!).get() else null }
          .map { (psn, update) ->
            PrisonerSupportNeedWithNomsIdAndLatestUpdate(
              prisonerSupportNeedId = psn.id!!,
              nomsId = "NOT_SET",
              pathway = psn.supportNeed.pathway,
              prisonerSupportNeedCreatedDate = psn.createdDate,
              excludeFromCount = psn.supportNeed.excludeFromCount,
              latestUpdateId = update?.id,
              latestUpdateStatus = update?.status,
              latestUpdateCreatedDate = update?.createdDate,
              isPrison = update?.isPrison,
              isProbation = update?.isProbation,
            )
          }
        return Pathway.entries.map {
          SupportNeedSummary(
            pathway = it,
            reviewed = isPathwayReviewed(it, prisonerSupportNeedWithLatestUpdate),
            isPrisonResponsible = isPrisonResponsible(it, prisonerSupportNeedWithLatestUpdate),
            isProbationResponsible = isProbationResponsible(it, prisonerSupportNeedWithLatestUpdate),
            notStarted = getCountForStatus(it, SupportNeedStatus.NOT_STARTED, prisonerSupportNeedWithLatestUpdate),
            inProgress = getCountForStatus(it, SupportNeedStatus.IN_PROGRESS, prisonerSupportNeedWithLatestUpdate),
            met = getCountForStatus(it, SupportNeedStatus.MET, prisonerSupportNeedWithLatestUpdate),
            declined = getCountForStatus(it, SupportNeedStatus.DECLINED, prisonerSupportNeedWithLatestUpdate),
            lastUpdated = getLastUpdatedForPathway(it, prisonerSupportNeedWithLatestUpdate),
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
      isPrisonResponsible = false,
      isProbationResponsible = false,
      notStarted = 0,
      inProgress = 0,
      met = 0,
      declined = 0,
      lastUpdated = null,
    )
  }

  fun isPathwayReviewed(pathway: Pathway, prisonerSupportNeeds: List<PrisonerSupportNeedWithNomsIdAndLatestUpdate>) =
    prisonerSupportNeeds.any { it.pathway == pathway }

  fun getCountForStatus(pathway: Pathway, status: SupportNeedStatus, prisonerSupportNeeds: List<PrisonerSupportNeedWithNomsIdAndLatestUpdate>) =
    prisonerSupportNeeds.count { it.pathway == pathway && it.latestUpdateStatus == status && !it.excludeFromCount }

  fun getLastUpdatedForPathway(pathway: Pathway, prisonerSupportNeeds: List<PrisonerSupportNeedWithNomsIdAndLatestUpdate>) =
    prisonerSupportNeeds.filter { it.pathway == pathway }
      .map { if (it.latestUpdateId != null) it.latestUpdateCreatedDate?.toLocalDate() else it.prisonerSupportNeedCreatedDate.toLocalDate() }
      .sortedByDescending { it }
      .firstOrNull()

  fun isPrisonResponsible(pathway: Pathway, prisonerSupportNeedToLatestUpdateMap: List<PrisonerSupportNeedWithNomsIdAndLatestUpdate>) =
    prisonerSupportNeedToLatestUpdateMap.filter { it.pathway == pathway }
      .any { it.isPrison == true }

  fun isProbationResponsible(pathway: Pathway, prisonerSupportNeedToLatestUpdateMap: List<PrisonerSupportNeedWithNomsIdAndLatestUpdate>) =
    prisonerSupportNeedToLatestUpdateMap.filter { it.pathway == pathway }
      .any { it.isProbation == true }

  fun getNeedsSummaryByNomsId(nomsId: String): SupportNeedSummaryResponse {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Cannot find prisoner $nomsId")
    return SupportNeedSummaryResponse(getNeedsSummary(prisoner.id))
  }

  fun getNeedsSummaryToNomsIdMapByPrisonId(prisonId: String): Map<String, List<SupportNeedSummary>> {
    // Get all prisoner support needs and updates from database related prisoners in the prison
    val prisonerSupportNeedsFromDatabase = prisonerSupportNeedRepository.getPrisonerSupportNeedsByPrisonId(prisonId)

    // Group by the prisoner then convert into a support needs summary
    // Note - return objects from the query and cast here to avoid JPA performance issues
    val nomsIdToPrisonerSupportNeedsMap = prisonerSupportNeedsFromDatabase.map {
      PrisonerSupportNeedWithNomsIdAndLatestUpdate(
        prisonerSupportNeedId = it[0] as Long,
        nomsId = it[1] as String,
        pathway = it[2] as Pathway,
        prisonerSupportNeedCreatedDate = it[3] as LocalDateTime,
        excludeFromCount = it[4] as Boolean,
        latestUpdateId = it[5] as Long?,
        latestUpdateStatus = it[6] as SupportNeedStatus?,
        latestUpdateCreatedDate = it[7] as LocalDateTime?,
        isPrison = it[8] as Boolean?,
        isProbation = it[9] as Boolean?,
      )
    }.groupBy { it.nomsId }

    val nomsIdToSupportNeedSummaryMap = nomsIdToPrisonerSupportNeedsMap.entries.associate {
      it.key to Pathway.entries.map { pathway ->
        SupportNeedSummary(
          pathway = pathway,
          reviewed = isPathwayReviewed(pathway, it.value),
          notStarted = getCountForStatus(pathway, SupportNeedStatus.NOT_STARTED, it.value),
          inProgress = getCountForStatus(pathway, SupportNeedStatus.IN_PROGRESS, it.value),
          met = getCountForStatus(pathway, SupportNeedStatus.MET, it.value),
          declined = getCountForStatus(pathway, SupportNeedStatus.DECLINED, it.value),
          lastUpdated = getLastUpdatedForPathway(pathway, it.value),
          isPrisonResponsible = isPrisonResponsible(pathway, it.value),
          isProbationResponsible = isProbationResponsible(pathway, it.value),
        )
      }
    }

    return nomsIdToSupportNeedSummaryMap
  }

  fun getPathwayNeedsSummaryByNomsId(nomsId: String, pathway: Pathway): PathwayNeedsSummary {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Cannot find prisoner $nomsId")
    val prisonerSupportNeeds = prisonerSupportNeedRepository.findAllByPrisonerIdAndSupportNeedPathwayAndDeletedIsFalse(prisoner.id!!, pathway)
    val prisonerSupportNeedsToUpdateMap = prisonerSupportNeeds.associateWith { prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(it.id!!) }
    val needs = prisonerSupportNeedsToUpdateMap.filter { !it.key.supportNeed.excludeFromCount && it.value.isNotEmpty() }.map { (psn, updates) ->
      PrisonerNeed(
        id = psn.id!!,
        title = if (!psn.supportNeed.allowOtherDetail) psn.supportNeed.title else psn.otherDetail ?: psn.supportNeed.title,
        isPrisonResponsible = updates.first().isPrison,
        isProbationResponsible = updates.first().isProbation,
        status = updates.first().status,
        numberOfUpdates = updates.size,
        lastUpdated = updates.first().createdDate.toLocalDate(),
      )
    }
    return PathwayNeedsSummary(prisonerNeeds = needs)
  }
}
