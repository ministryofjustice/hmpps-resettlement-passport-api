package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayNeedsSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeed
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedIdAndTitle
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedWithUpdates
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedsRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerSupportNeedWithNomsIdAndLatestUpdate
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeed
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedUpdate
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedUpdates
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeeds
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedsUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.SupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedUpdateRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.SupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CaseNotesApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import java.time.LocalDateTime

@Service
class SupportNeedsService(
  private val prisonerSupportNeedRepository: PrisonerSupportNeedRepository,
  private val prisonerSupportNeedUpdateRepository: PrisonerSupportNeedUpdateRepository,
  private val prisonerRepository: PrisonerRepository,
  private val supportNeedRepository: SupportNeedRepository,
  private val caseNotesApiService: CaseNotesApiService,
  private val deliusContactService: DeliusContactService,
  private val telemetryClient: TelemetryClient,
  private val prisonerSearchApiService: PrisonerSearchApiService,
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

  fun isPathwayReviewed(pathway: Pathway, prisonerSupportNeeds: List<PrisonerSupportNeedWithNomsIdAndLatestUpdate>) = prisonerSupportNeeds.any { it.pathway == pathway }

  fun getCountForStatus(pathway: Pathway, status: SupportNeedStatus, prisonerSupportNeeds: List<PrisonerSupportNeedWithNomsIdAndLatestUpdate>) = prisonerSupportNeeds.count { it.pathway == pathway && it.latestUpdateStatus == status && !it.excludeFromCount }

  fun getLastUpdatedForPathway(pathway: Pathway, prisonerSupportNeeds: List<PrisonerSupportNeedWithNomsIdAndLatestUpdate>) = prisonerSupportNeeds.filter { it.pathway == pathway }
    .map { if (it.latestUpdateId != null) it.latestUpdateCreatedDate?.toLocalDate() else it.prisonerSupportNeedCreatedDate.toLocalDate() }
    .sortedByDescending { it }
    .firstOrNull()

  fun isPrisonResponsible(pathway: Pathway, prisonerSupportNeedToLatestUpdateMap: List<PrisonerSupportNeedWithNomsIdAndLatestUpdate>) = prisonerSupportNeedToLatestUpdateMap.filter { it.pathway == pathway }
    .any { it.isPrison == true }

  fun isProbationResponsible(pathway: Pathway, prisonerSupportNeedToLatestUpdateMap: List<PrisonerSupportNeedWithNomsIdAndLatestUpdate>) = prisonerSupportNeedToLatestUpdateMap.filter { it.pathway == pathway }
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
    val needs = prisonerSupportNeedsToUpdateMap.filter { !it.key.supportNeed.excludeFromCount }.map { (psn, updates) ->
      PrisonerNeed(
        id = psn.id!!,
        title = getTitleFromPrisonerSupportNeed(psn),
        isPrisonResponsible = updates.firstOrNull()?.isPrison,
        isProbationResponsible = updates.firstOrNull()?.isProbation,
        status = updates.firstOrNull()?.status,
        numberOfUpdates = updates.size,
        lastUpdated = updates.firstOrNull()?.createdDate?.toLocalDate() ?: psn.createdDate.toLocalDate(),
      )
    }.sortedBy { it.id }
    return PathwayNeedsSummary(prisonerNeeds = needs)
  }

  fun getPathwayUpdatesByNomsId(
    nomsId: String,
    pathway: Pathway,
    page: Int,
    size: Int,
    sort: String,
    prisonerSupportNeedId: Long?,
  ): SupportNeedUpdates {
    // Validate sort - must be either "createdDate,DESC" or "createdDate,ASC"
    if (sort !in listOf("createdDate,DESC", "createdDate,ASC")) {
      throw ServerWebInputException("Sort must be either \"createdDate,DESC\" or \"createdDate,ASC\"")
    }

    val sortDirection = sort.split(',')[1]

    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Cannot find prisoner $nomsId")

    // Get prisoner_support_need_updates from database
    val prisonerSupportNeeds = prisonerSupportNeedRepository.findAllByPrisonerIdAndSupportNeedPathway(prisoner.id!!, pathway)
    val allPrisonerNeedsMapped = mapToPrisonerNeedIdAndTitle(prisonerSupportNeeds)
    val mappedPrisonerSupportNeedUpdates = mapToSupportNeedUpdate(prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdInAndDeletedIsFalse(prisonerSupportNeeds.mapNotNull { it.id }), allPrisonerNeedsMapped)

    // Get old-style pathway case notes from external API
    val caseNoteType = convertPathwayToCaseNoteType(pathway)
    val mappedPathwayCaseNotesFromApi = mapFromCaseNoteToSupportNeedUpdate(caseNotesApiService.getCaseNotesByNomsId(nomsId, 0, caseNoteType, 0), pathway)

    // Get all old-style case notes from Delius users from database
    val mappedDeliusUserCaseNotes = mapFromCaseNoteToSupportNeedUpdate(deliusContactService.getCaseNotesByNomsId(nomsId, caseNoteType), pathway)

    // Combine case notes from different sources and apply filtering
    // Note that we can only filter by the prisonerSupportNeedId
    val combinedMappedUpdates = (mappedPrisonerSupportNeedUpdates + mappedPathwayCaseNotesFromApi + mappedDeliusUserCaseNotes)
      .filter { if (prisonerSupportNeedId != null) it.prisonerNeedId == prisonerSupportNeedId else true }

    // Apply sorting
    val sortedCombinedMappedUpdates = if (sortDirection == "ASC") combinedMappedUpdates.sortedBy { it.createdAt } else combinedMappedUpdates.sortedByDescending { it.createdAt }

    val startIndex = page * size
    val maxEndIndex = (page + 1) * size
    val endIndex = if (sortedCombinedMappedUpdates.size < maxEndIndex) sortedCombinedMappedUpdates.size else maxEndIndex
    val updatePage = sortedCombinedMappedUpdates.subList(startIndex, endIndex)
    val last = sortedCombinedMappedUpdates.size == endIndex

    return SupportNeedUpdates(
      updates = updatePage,
      allPrisonerNeeds = mapToPrisonerNeedIdAndTitle(prisonerSupportNeeds.filter { !it.deleted }),
      size = size,
      page = page,
      sortName = sort,
      totalElements = sortedCombinedMappedUpdates.size,
      last = last,
    )
  }

  fun mapToPrisonerNeedIdAndTitle(prisonerSupportNeeds: List<PrisonerSupportNeedEntity>) = prisonerSupportNeeds.map { PrisonerNeedIdAndTitle(id = it.id!!, title = getTitleFromPrisonerSupportNeed(it)) }

  fun mapToSupportNeedUpdate(prisonerSupportNeedUpdates: List<PrisonerSupportNeedUpdateEntity>, allPrisonerNeeds: List<PrisonerNeedIdAndTitle>) = prisonerSupportNeedUpdates.map { psnu ->
    SupportNeedUpdate(
      id = psnu.id!!,
      prisonerNeedId = psnu.prisonerSupportNeedId,
      title = allPrisonerNeeds.first { psnu.prisonerSupportNeedId == it.id }.title,
      status = psnu.status,
      isPrisonResponsible = psnu.isPrison,
      isProbationResponsible = psnu.isProbation,
      text = psnu.updateText,
      createdBy = psnu.createdBy,
      createdAt = psnu.createdDate,
    )
  }

  fun getTitleFromPrisonerSupportNeed(prisonerSupportNeed: PrisonerSupportNeedEntity) = if (!prisonerSupportNeed.supportNeed.allowOtherDetail) prisonerSupportNeed.supportNeed.title else prisonerSupportNeed.otherDetail ?: prisonerSupportNeed.supportNeed.title

  fun getPathwayNeedsByNomsId(nomsId: String, pathway: Pathway): SupportNeeds {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Cannot find prisoner $nomsId")

    val supportNeedsFromDatabase = supportNeedRepository.findByPathwayAndDeletedIsFalse(pathway)
    val prisonerSupportNeedsFromDatabase = prisonerSupportNeedRepository.findAllByPrisonerIdAndSupportNeedPathwayAndDeletedIsFalse(prisoner.id!!, pathway)

    // Need to return each non-hidden support need that isn't already added to the prisoner
    // If only the "No support needs identified" option was chosen then return this pre-checked other do not return this at all.
    val supportNeeds = supportNeedsFromDatabase.mapNotNull { sn ->
      val linkedPrisonerSupportNeeds = prisonerSupportNeedsFromDatabase.filter { it.supportNeed == sn }
      if (isSupportNeedAvailable(sn, linkedPrisonerSupportNeeds, prisonerSupportNeedsFromDatabase)) {
        SupportNeed(
          id = sn.id,
          title = sn.title,
          category = sn.section,
          allowUserDesc = sn.allowOtherDetail,
          isUpdatable = !sn.excludeFromCount,
          isPreSelected = isSupportNeedPreSelected(sn, linkedPrisonerSupportNeeds, prisonerSupportNeedsFromDatabase),
          existingPrisonerSupportNeedId = getExistingPrisonerSupportNeedId(sn, prisonerSupportNeedsFromDatabase),
        )
      } else {
        null
      }
    }

    return SupportNeeds(supportNeeds = supportNeeds)
  }

  fun isSupportNeedAvailable(supportNeedEntity: SupportNeedEntity, linkedPrisonerSupportNeeds: List<PrisonerSupportNeedEntity>, allPrisonerSupportNeeds: List<PrisonerSupportNeedEntity>): Boolean {
    // If this is a "No support needs identified" i.e. excludeFromCount=true, then it should only be included if no other support needs in section are already selected
    return if (supportNeedEntity.excludeFromCount) {
      !allPrisonerSupportNeeds.any { it.supportNeed.section == supportNeedEntity.section && !it.supportNeed.excludeFromCount }
    } else {
      // Should be included if the need hasn't already been selected or is the "other" support need (as this should always be returned)
      // Also exclude any hidden support needs
      (linkedPrisonerSupportNeeds.isEmpty() || supportNeedEntity.allowOtherDetail) && !supportNeedEntity.hidden
    }
  }

  fun isSupportNeedPreSelected(supportNeedEntity: SupportNeedEntity, linkedPrisonerSupportNeeds: List<PrisonerSupportNeedEntity>, allPrisonerSupportNeeds: List<PrisonerSupportNeedEntity>) = supportNeedEntity.excludeFromCount && linkedPrisonerSupportNeeds.isNotEmpty() && allPrisonerSupportNeeds.none { it.supportNeed.section == supportNeedEntity.section && !it.supportNeed.excludeFromCount }

  fun getExistingPrisonerSupportNeedId(supportNeedEntity: SupportNeedEntity, allPrisonerSupportNeeds: List<PrisonerSupportNeedEntity>) = allPrisonerSupportNeeds.filter { !supportNeedEntity.allowOtherDetail && it.supportNeed.id == supportNeedEntity.id }.maxByOrNull { it.createdDate }?.id

  fun getPrisonerNeedById(nomsId: String, prisonerSupportNeedId: Long): PrisonerNeedWithUpdates {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Cannot find prisoner $nomsId")
    val prisonerSupportNeed = prisonerSupportNeedRepository.findByIdAndDeletedIsFalse(prisonerSupportNeedId) ?: throw ResourceNotFoundException("Cannot find prisoner support need $prisonerSupportNeedId")

    if (prisonerSupportNeed.prisonerId != prisoner.id) {
      throw ResourceNotFoundException("Prisoner support need $prisonerSupportNeedId is not associated with prisoner $nomsId")
    }

    val updates = prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(prisonerSupportNeedId)

    val title = getTitleFromPrisonerSupportNeed(prisonerSupportNeed)

    return PrisonerNeedWithUpdates(
      title = title,
      isPrisonResponsible = updates.firstOrNull()?.isPrison,
      isProbationResponsible = updates.firstOrNull()?.isProbation,
      status = updates.firstOrNull()?.status,
      previousUpdates = updates.map {
        SupportNeedUpdate(
          id = it.id!!,
          prisonerNeedId = prisonerSupportNeedId,
          title = title,
          status = it.status,
          isPrisonResponsible = it.isPrison,
          isProbationResponsible = it.isProbation,
          text = it.updateText,
          createdBy = it.createdBy,
          createdAt = it.createdDate,
        )
      },
    )
  }

  @Transactional
  fun postSupportNeeds(nomsId: String, prisonerNeedsRequest: PrisonerNeedsRequest, auth: String) {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Cannot find prisoner $nomsId")
    val name = getClaimFromJWTToken(auth, "name")
      ?: getClaimFromJWTToken(auth, "sub")
      ?: throw ServerWebInputException("JWT token must include a claim for 'name or 'sub'")

    prisonerNeedsRequest.needs.forEach { need ->
      // Get the prisoner support need or create new one if required
      val prisonerSupportNeed = getAndSavePrisonerSupportNeed(need, prisoner.id!!, name)

      // Save the new update against the prisoner support need apart from when excludeFromCount is set as these do not have updates
      if (!prisonerSupportNeed.supportNeed.excludeFromCount) {
        if (need.status == null || need.isPrisonResponsible == null || need.isProbationResponsible == null) {
          throw ServerWebInputException("Update (status, isPrisonResponsible, isProbationResponsible) cannot not be null for support need ${prisonerSupportNeed.supportNeed.id}")
        }
        val update = PrisonerSupportNeedUpdateEntity(
          prisonerSupportNeedId = prisonerSupportNeed.id!!,
          createdBy = name,
          createdDate = LocalDateTime.now(),
          updateText = need.text,
          status = need.status,
          isPrison = need.isPrisonResponsible,
          isProbation = need.isProbationResponsible,
        )
        val savedPrisonerSupportNeedUpdate = prisonerSupportNeedUpdateRepository.save(update)

        // Update prisoner support need with the latest update id
        prisonerSupportNeed.latestUpdateId = savedPrisonerSupportNeedUpdate.id
        prisonerSupportNeedRepository.save(prisonerSupportNeed)
      }
    }

    // Send event to app insights
    val prisonCode = prisonerSearchApiService.findPrisonerPersonalDetails(nomsId).prisonId
    val username = getClaimFromJWTToken(auth, "sub")
      ?: throw ServerWebInputException("JWT token must include a claim for 'sub'")
    val authSource = getClaimFromJWTToken(auth, "auth_source")?.lowercase()
      ?: throw ServerWebInputException("JWT token must include a claim for 'auth_source'")
    telemetryClient.trackEvent(
      "PSFR_SupportNeedsSubmitted",
      mapOf(
        "supportNeedIds" to prisonerNeedsRequest.needs.map { it.needId }.toString(),
        "prisonId" to prisonCode,
        "prisonerId" to nomsId,
        "submittedBy" to username,
        "authSource" to authSource,
      ),
      null,
    )
  }

  private fun getAndSavePrisonerSupportNeed(need: PrisonerNeedRequest, prisonerId: Long, name: String): PrisonerSupportNeedEntity {
    // If a prisonerSupportNeed is given i.e. it's an update, just find and return this
    if (need.prisonerSupportNeedId != null) {
      return prisonerSupportNeedRepository.findByIdAndDeletedIsFalse(need.prisonerSupportNeedId)
        ?: throw ResourceNotFoundException("Cannot find prisoner support need ${need.prisonerSupportNeedId}")
    } else {
      // Check if there's any existing in case of any parallel working, if there is return this
      val existingPrisonerSupportNeed = prisonerSupportNeedRepository.findFirstByPrisonerIdAndSupportNeedIdAndOtherDetailAndDeletedIsFalseOrderByCreatedDateDesc(prisonerId, need.needId, need.otherDesc)
      if (existingPrisonerSupportNeed != null) {
        return existingPrisonerSupportNeed
      } else {
        // If there's no existing prisoner support need, create and return it
        val supportNeed = supportNeedRepository.findByIdAndDeletedIsFalse(need.needId)
          ?: throw ResourceNotFoundException("Support need with id ${need.needId} not found or is deleted")
        val prisonerSupportNeed = PrisonerSupportNeedEntity(
          prisonerId = prisonerId,
          supportNeed = supportNeed,
          otherDetail = need.otherDesc,
          createdBy = name,
          createdDate = LocalDateTime.now(),
        )
        return prisonerSupportNeedRepository.save(prisonerSupportNeed)
      }
    }
  }

  @Transactional
  fun patchPrisonerNeedById(nomsId: String, prisonerNeedId: Long, supportNeedsUpdateRequest: SupportNeedsUpdateRequest, auth: String) {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Cannot find prisoner $nomsId")
    val name = getClaimFromJWTToken(auth, "name")
      ?: getClaimFromJWTToken(auth, "sub")
      ?: throw ServerWebInputException("JWT token must include a claim for 'name or 'sub'")

    val prisonerSupportNeed = prisonerSupportNeedRepository.findByIdAndDeletedIsFalse(prisonerNeedId) ?: throw ResourceNotFoundException("Cannot find prisoner support need $prisonerNeedId")

    if (prisoner.id != prisonerSupportNeed.prisonerId) {
      throw ResourceNotFoundException("Cannot find prisoner support need on prisoner $nomsId")
    }

    val update = PrisonerSupportNeedUpdateEntity(
      prisonerSupportNeedId = prisonerNeedId,
      createdBy = name,
      createdDate = LocalDateTime.now(),
      updateText = supportNeedsUpdateRequest.text,
      status = supportNeedsUpdateRequest.status,
      isPrison = supportNeedsUpdateRequest.isPrisonResponsible,
      isProbation = supportNeedsUpdateRequest.isProbationResponsible,
    )
    val savedPrisonerSupportNeedUpdate = prisonerSupportNeedUpdateRepository.save(update)

    // Update prisoner support need with the latest update id
    prisonerSupportNeed.latestUpdateId = savedPrisonerSupportNeedUpdate.id

    prisonerSupportNeedRepository.save(prisonerSupportNeed)

    // Send event to app insights
    val prisonCode = prisonerSearchApiService.findPrisonerPersonalDetails(nomsId).prisonId
    val username = getClaimFromJWTToken(auth, "sub")
      ?: throw ServerWebInputException("JWT token must include a claim for 'sub'")
    val authSource = getClaimFromJWTToken(auth, "auth_source")?.lowercase()
      ?: throw ServerWebInputException("JWT token must include a claim for 'auth_source'")
    telemetryClient.trackEvent(
      "PSFR_SupportNeedUpdated",
      mapOf(
        "prisonerSupportNeedId" to prisonerNeedId.toString(),
        "supportNeedId" to prisonerSupportNeed.supportNeed.id.toString(),
        "prisonId" to prisonCode,
        "prisonerId" to nomsId,
        "submittedBy" to username,
        "authSource" to authSource,
      ),
      null,
    )
  }

  fun mapFromCaseNoteToSupportNeedUpdate(caseNotes: List<PathwayCaseNote>, pathway: Pathway) = caseNotes.map {
    SupportNeedUpdate(
      id = 0,
      prisonerNeedId = null,
      title = "${pathway.displayName} (case note)",
      status = null,
      isPrisonResponsible = null,
      isProbationResponsible = null,
      text = it.text,
      createdBy = it.createdBy,
      createdAt = it.creationDateTime,
    )
  }

  @Transactional
  fun resetSupportNeeds(nomsId: String, reason: String, user: String) {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Cannot find prisoner $nomsId")

    val existingPrisonerSupportNeeds = prisonerSupportNeedRepository.findAllByPrisonerIdAndDeletedIsFalse(prisoner.id!!)

    existingPrisonerSupportNeeds.forEach {
      it.deleted = true
      it.deletedDate = LocalDateTime.now()
      prisonerSupportNeedRepository.save(it)
    }

    val profileResetUpdates = existingPrisonerSupportNeeds.filter { !it.supportNeed.excludeFromCount }.map {
      PrisonerSupportNeedUpdateEntity(
        prisonerSupportNeedId = it.id!!,
        createdBy = user,
        createdDate = LocalDateTime.now(),
        updateText = "Support need removed because of profile reset\n\nReason for reset: $reason",
        status = null,
        isPrison = false,
        isProbation = false,
      )
    }

    prisonerSupportNeedUpdateRepository.saveAll(profileResetUpdates)
  }

  fun getAllSupportNeedsForPrisoner(prisonerId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<PrisonerSupportNeedEntity> = prisonerSupportNeedRepository.findAllByPrisonerIdAndCreatedDateBetween(prisonerId, startDate, endDate)

  fun getAllSupportNeedUpdatesForPrisoner(supportNeeds: List<PrisonerSupportNeedEntity>, startDate: LocalDateTime, endDate: LocalDateTime): List<PrisonerSupportNeedUpdateEntity> = prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdInAndCreatedDateBetween(supportNeeds.mapNotNull { it.id }, startDate, endDate)
}
