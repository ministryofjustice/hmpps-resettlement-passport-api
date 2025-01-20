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

  fun getNeedsSummaryByNomsId(nomsId: String): SupportNeedSummaryResponse {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Cannot find prisoner $nomsId")
    return SupportNeedSummaryResponse(getNeedsSummary(prisoner.id))
  }
}
