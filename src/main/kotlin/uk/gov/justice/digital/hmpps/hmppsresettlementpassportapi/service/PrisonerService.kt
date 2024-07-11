package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prison
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoner
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerPersonal
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoners
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.PersonalDetail
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonerImage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.Period
import java.util.stream.Stream

@Service
class PrisonerService(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val prisonApiService: PrisonApiService,
  private val prisonerRepository: PrisonerRepository,
  private val pathwayStatusRepository: PathwayStatusRepository,
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val watchlistService: WatchlistService,
  private val pathwayAndStatusService: PathwayAndStatusService,
  private val deliusApiService: ResettlementPassportDeliusApiService,
  private val prisonRegisterApiService: PrisonRegisterApiService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getActivePrisonsList(): MutableList<Prison> {
    val prisons = prisonRegisterApiService.getPrisons()

    val prisonList = mutableListOf<Prison>()
    for (item in prisons) {
      if (item.active) {
        prisonList.add(Prison(item.prisonId, item.prisonName, true))
      }
    }
    prisonList.sortBy { it.name }
    return prisonList
  }

  fun getPrisonersByPrisonId(
    searchTerm: String?,
    prisonId: String,
    days: Int,
    pathwayView: Pathway?,
    pathwayStatus: Status?,
    assessmentRequired: Boolean?,
    pageNumber: Int,
    pageSize: Int,
    sort: String,
    watchList: Boolean?,
    auth: String,
  ): PrisonersList {
    if (pathwayStatus != null && pathwayView == null) {
      throw ServerWebInputException("pathwayStatus cannot be used without pathwayView")
    }
    if (pathwayView == null && (sort == "pathwayStatus,ASC" || sort == "pathwayStatus,DESC")) {
      throw ServerWebInputException("Pathway must be selected to sort by pathway status")
    }
    if (assessmentRequired != null && pathwayView != null) {
      throw ServerWebInputException("pathwayView cannot be used in conjunction with assessmentRequired")
    }
    val staffUsername = getClaimFromJWTToken(auth, "sub") ?: throw ServerWebInputException("Cannot get name from auth token")

    val prisoners = mutableListOf<PrisonersSearch>()
    if (prisonId.isBlank() || prisonId.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoners", prisonId)
    }

    if (pageNumber < 0 || pageSize <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber and Size $pageSize",
      )
    }
    prisonerSearchApiService.findPrisonersBySearchTerm(prisonId).forEach {
      setDisplayedReleaseDate(it)
      prisoners.add(it)
    }

    if (searchTerm?.isNotBlank() == true) {
      prisoners.retainAll { searchTermMatchesPrisoner(searchTerm, it) }
    }

    if (days > 0) {
      val earliestReleaseDate = LocalDate.now().minusDays(1)
      val latestReleaseDate = LocalDate.now().plusDays(days.toLong())
      prisoners.removeAll { it.displayReleaseDate == null || it.displayReleaseDate!! <= earliestReleaseDate || it.displayReleaseDate!! > latestReleaseDate }
    } else {
      val earliestReleaseDate = LocalDate.now().minusDays(1)
      prisoners.removeAll { (it.displayReleaseDate != null && it.displayReleaseDate!! <= earliestReleaseDate) }
    }

    if (prisoners.isEmpty()) {
      return PrisonersList(emptyList(), pageSize, pageNumber, sort, 0, true)
    }

    val startIndex = (pageNumber * pageSize)
    if (startIndex >= prisoners.size) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber",
      )
    }

    val fullList = objectMapper(prisoners, pathwayView, pathwayStatus, prisonId, assessmentRequired, watchList, staffUsername)

    sortPrisoners(sort, fullList)

    val endIndex = (pageNumber * pageSize) + (pageSize)
    if (startIndex < endIndex && endIndex <= fullList.size) {
      val pList = fullList.subList(startIndex, endIndex)
      return PrisonersList(pList, pList.toList().size, pageNumber, sort, fullList.size, (endIndex == fullList.size))
    } else if (startIndex < endIndex) {
      val pList = fullList.subList(startIndex, fullList.size)
      return PrisonersList(pList, pList.toList().size, pageNumber, sort, fullList.size, true)
    }
    return PrisonersList(emptyList(), 0, 0, sort, 0, false)
  }

  fun sortPrisoners(sort: String?, prisoners: MutableList<Prisoners>) {
    if (sort == null) {
      sortPrisonersByNomsId("ASC", prisoners)
    } else {
      sortPrisonersByNomsId(sort, prisoners)
      sortPrisonersByField(sort, prisoners)
    }
  }

  fun sortPrisonersByField(
    sort: String,
    prisoners: MutableList<Prisoners>,
  ) {
    when (sort) {
      "releaseDate,ASC" -> prisoners.sortWith(compareBy(nullsLast()) { it.releaseDate })
      "releaseEligibilityDate,ASC" -> prisoners.sortWith(compareBy(nullsLast()) { it.releaseEligibilityDate })
      "name,ASC" -> prisoners.sortWith(compareBy { "${it.lastName}, ${it.firstName}" })
      "lastUpdatedDate,ASC" -> prisoners.sortWith(compareBy(nullsLast()) { it.lastUpdatedDate })
      "prisonerNumber,ASC" -> prisoners.sortBy { it.prisonerNumber }
      "pathwayStatus,ASC" -> prisoners.sortBy { it.pathwayStatus }
      "releaseOnTemporaryLicenceDate,ASC" -> prisoners.sortWith(compareBy(nullsLast()) { it.releaseOnTemporaryLicenceDate })
      "releaseDate,DESC" -> prisoners.sortWith(compareByDescending(nullsLast()) { it.releaseDate })
      "releaseEligibilityDate,DESC" -> prisoners.sortWith(compareByDescending(nullsLast()) { it.releaseEligibilityDate })
      "name,DESC" -> prisoners.sortWith(compareByDescending(nullsLast()) { "${it.lastName}, ${it.firstName}" })
      "lastUpdatedDate,DESC" -> prisoners.sortWith(compareByDescending(nullsLast()) { it.lastUpdatedDate })
      "prisonerNumber,DESC" -> prisoners.sortWith(compareByDescending(nullsLast()) { it.prisonerNumber })
      "pathwayStatus,DESC" -> prisoners.sortByDescending { it.pathwayStatus }
      "releaseOnTemporaryLicenceDate,DESC" -> prisoners.sortWith(compareByDescending(nullsLast()) { it.releaseOnTemporaryLicenceDate })

      else -> throw NoDataWithCodeFoundException(
        "Data",
        "Sort value Invalid",
      )
    }
  }

  fun sortPrisonersByNomsId(
    sort: String,
    prisoners: MutableList<Prisoners>,
  ) {
    val sortNoms = sort.split(",").last()
    when (sortNoms) {
      "ASC" -> prisoners.sortBy { it.prisonerNumber }
      "DESC" -> prisoners.sortByDescending { it.prisonerNumber }
    }
  }

  fun objectMapper(
    searchList: List<PrisonersSearch>,
    pathwayView: Pathway?,
    pathwayStatusToFilter: Status?,
    prisonId: String,
    assessmentRequiredFilter: Boolean?,
    watchListFilter: Boolean?,
    staffUsername: String,
  ): MutableList<Prisoners> {
    val prisonersList = mutableListOf<Prisoners>()

    // Find prisoners in prison that do not require an assessment
    val prisonersWithSubmittedAssessment = resettlementAssessmentRepository.findPrisonersWithAllAssessmentsInStatus(prisonId, ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED, Pathway.entries.size)

    // Find the pathway statuses for prisoners in prison
    val prisonerPathwayStatusesFromDatabase = pathwayStatusRepository.findByPrison(prisonId)

    val defaultPathwayStatuses = getDefaultPathwayStatuses()
    val watchedOffenders = watchlistService.findAllWatchedPrisonerForStaff(staffUsername)
    searchList.forEach { prisonersSearch ->

      val pathwayStatusesEntities = prisonerPathwayStatusesFromDatabase.filter { it.prisoner.nomsId == prisonersSearch.prisonerNumber }
      val prisonerEntity = pathwayStatusesEntities.map { it.prisoner }.firstOrNull()

      val pathwayStatuses: List<PathwayStatus>?
      val sortedPathwayStatuses: List<PathwayStatus>?
      val pathwayStatus: Status?
      val lastUpdatedDate: LocalDate?
      val isInWatchList = watchedOffenders.contains(prisonerEntity)

      if (watchListFilter == true && !isInWatchList) {
        return@forEach
      }

      if (prisonerEntity != null) {
        pathwayStatuses = if (pathwayView == null) pathwayStatusesEntities.map { PathwayStatus(pathway = it.pathway, status = it.status, lastDateChange = it.updatedDate?.toLocalDate()) }.sortedBy { it.pathway } else null
        sortedPathwayStatuses = pathwayStatuses?.sortedWith(compareBy(nullsLast()) { it.lastDateChange })
        lastUpdatedDate =
          if (pathwayView == null) {
            sortedPathwayStatuses?.first()?.lastDateChange
          } else {
            pathwayStatusesEntities.find { it.pathway == pathwayView }?.updatedDate?.toLocalDate()
          }
        pathwayStatus = if (pathwayView != null) pathwayStatusesEntities.first { it.pathway == pathwayView }.status else null
      } else {
        // We don't know about this prisoner yet so just set all the statuses to NOT_STARTED.
        pathwayStatuses = if (pathwayView == null) defaultPathwayStatuses else null
        pathwayStatus = if (pathwayView != null) Status.NOT_STARTED else null
        lastUpdatedDate = null
      }

      // Find out if resettlement assessment is required
      val assessmentRequired = !prisonersWithSubmittedAssessment.contains(prisonerEntity)

      // If assessmentRequired filter is defined, skip over any that don't match
      if (assessmentRequiredFilter != null) {
        if (assessmentRequired != assessmentRequiredFilter) {
          return@forEach
        }
      }

      if (pathwayStatusToFilter == null || pathwayStatusToFilter == pathwayStatus) {
        val prisoner = Prisoners(
          prisonersSearch.prisonerNumber.trim(),
          prisonersSearch.firstName.trim(),
          prisonersSearch.middleNames?.trim(),
          prisonersSearch.lastName.trim(),
          prisonersSearch.displayReleaseDate,
          prisonersSearch.nonDtoReleaseDateType,
          lastUpdatedDate,
          pathwayStatuses,
          pathwayStatus,
          prisonersSearch.homeDetentionCurfewEligibilityDate,
          prisonersSearch.paroleEligibilityDate,
          getDisplayedReleaseEligibilityDate(prisonersSearch),
          getDisplayedReleaseEligibilityType(prisonersSearch),
          prisonersSearch.releaseOnTemporaryLicenceDate,
          assessmentRequired,
        )
        prisonersList.add(prisoner)
      }
    }
    return prisonersList
  }

  fun getPrisonerDetailsByNomsId(nomsId: String, auth: String): Prisoner {
    val prisonerSearch = prisonerSearchApiService.findPrisonerPersonalDetails(nomsId)
    setDisplayedReleaseDate(prisonerSearch)

    // Add initial pathway statuses if required
    val prisonerEntity = pathwayAndStatusService.getOrCreatePrisoner(
      nomsId,
      prisonerSearch.prisonId,
      prisonerSearch.displayReleaseDate,
    )

    val prisonerImageDetailsList = prisonApiService.findPrisonerImageDetails(nomsId)
    var prisonerImage: PrisonerImage? = null
    prisonerImageDetailsList.forEach {
      if (prisonerImage == null || (prisonerImage!!.captureDateTime?.isBefore(it.captureDateTime) == true)) {
        prisonerImage = it
      }
    }
    var age = 0
    if (prisonerSearch.dateOfBirth != null) {
      age = Period.between(prisonerSearch.dateOfBirth, LocalDate.now()).years
    }
    var personalDetails = PersonalDetail(null.toString(), null, null, null)
    if (prisonerEntity.crn != null) {
      personalDetails = deliusApiService.getPersonalDetails(nomsId, prisonerEntity.crn!!)!!
    } else {
      personalDetails.contactDetails = PersonalDetail.ContactDetails(null, null, null)
    }

    val prisonerPersonal = PrisonerPersonal(
      prisonerSearch.prisonerNumber,
      prisonerSearch.prisonId,
      prisonerSearch.firstName,
      prisonerSearch.middleNames,
      prisonerSearch.lastName,
      prisonerSearch.displayReleaseDate,
      prisonerSearch.nonDtoReleaseDateType,
      prisonerSearch.dateOfBirth,
      age,
      prisonerSearch.cellLocation,
      prisonerImage?.imageId,
      personalDetails.contactDetails?.mobile,
      personalDetails.contactDetails?.telephone,
      personalDetails.contactDetails?.email,
      prisonerSearch.prisonName,
      hasHomeDetentionDates(prisonerSearch),
      isRecall = prisonerSearch.recall,
    )

    val pathwayStatuses = getPathwayStatuses(prisonerEntity)

    val assessmentStatus = isAssessmentRequired(prisonerEntity)
    val staffUsername = getClaimFromJWTToken(auth, "sub") ?: throw ServerWebInputException("Cannot get name from auth token")
    val isInWatchlist = watchlistService.isPrisonerInWatchList(staffUsername, prisonerEntity)
    return Prisoner(
      personalDetails = prisonerPersonal,
      pathways = pathwayStatuses,
      assessmentRequired = assessmentStatus.assessmentRequired,
      resettlementReviewAvailable = assessmentStatus.resettlementReviewAvailable,
      immediateNeedsSubmitted = assessmentStatus.immediateNeedsSubmitted,
      preReleaseSubmitted = assessmentStatus.preReleaseSubmitted,
      isInWatchlist = isInWatchlist,
    )
  }

  private fun hasHomeDetentionDates(prisonerSearch: PrisonersSearch): Boolean = prisonerSearch.homeDetentionCurfewActualDate != null ||
    prisonerSearch.homeDetentionCurfewEligibilityDate != null

  protected fun getPathwayStatuses(
    prisonerEntity: PrisonerEntity,
  ): ArrayList<PathwayStatus> {
    val pathwayStatuses = ArrayList<PathwayStatus>()
    Pathway.entries.forEach { pathway ->
      // Find the status in the database of each pathway for this prisoner - if any data is missing then throw an exception (should never happen)
      val pathwayStatusEntity =
        pathwayAndStatusService.findPathwayStatusFromPathwayAndPrisoner(pathway, prisonerEntity)
      val pathwayStatus = PathwayStatus(
        pathway,
        pathwayStatusEntity.status,
        pathwayStatusEntity.updatedDate?.toLocalDate(),
      )
      pathwayStatuses.add(pathwayStatus)
    }
    return pathwayStatuses
  }

  fun getDefaultPathwayStatuses(): List<PathwayStatus> {
    val pathwayStatuses = ArrayList<PathwayStatus>()
    Pathway.entries.forEach { pathway ->
      val pathwayStatus = PathwayStatus(
        pathway,
        Status.NOT_STARTED,
        null,
      )
      pathwayStatuses.add(pathwayStatus)
    }
    return pathwayStatuses
  }

  fun setDisplayedReleaseDate(prisoner: PrisonersSearch) {
    if (prisoner.confirmedReleaseDate != null) {
      prisoner.displayReleaseDate = prisoner.confirmedReleaseDate
    } else if (prisoner.actualParoleDate != null) {
      prisoner.displayReleaseDate = prisoner.actualParoleDate
    } else if (prisoner.homeDetentionCurfewActualDate != null) {
      prisoner.displayReleaseDate = prisoner.homeDetentionCurfewActualDate
    } else if (prisoner.conditionalReleaseDate != null) {
      prisoner.displayReleaseDate = prisoner.conditionalReleaseDate
    } else if (prisoner.automaticReleaseDate != null) {
      prisoner.displayReleaseDate = prisoner.automaticReleaseDate
    } else {
      prisoner.displayReleaseDate = null
    }
  }

  fun getDisplayedReleaseEligibilityDate(prisoner: PrisonersSearch): LocalDate? {
    var releaseEligibilityDate: LocalDate? = null
    if (prisoner.paroleEligibilityDate != null && prisoner.homeDetentionCurfewEligibilityDate != null) {
      log.warn("Parole eligibility date and home detention actual date both set for ${prisoner.prisonerNumber}")
      if (prisoner.paroleEligibilityDate < prisoner.homeDetentionCurfewEligibilityDate) {
        releaseEligibilityDate = prisoner.paroleEligibilityDate
      } else if (prisoner.paroleEligibilityDate > prisoner.homeDetentionCurfewEligibilityDate) {
        releaseEligibilityDate = prisoner.homeDetentionCurfewEligibilityDate
      }
    } else if (prisoner.paroleEligibilityDate != null) {
      releaseEligibilityDate = prisoner.paroleEligibilityDate
    } else if (prisoner.homeDetentionCurfewEligibilityDate != null) {
      releaseEligibilityDate = prisoner.homeDetentionCurfewEligibilityDate
    }
    return releaseEligibilityDate
  }

  fun getDisplayedReleaseEligibilityType(prisoner: PrisonersSearch): String? {
    var releaseEligibilityType: String? = null
    if (prisoner.paroleEligibilityDate != null && prisoner.homeDetentionCurfewEligibilityDate != null) {
      log.warn("Parole eligibility date and home detention actual date both set for ${prisoner.prisonerNumber}")
      if (prisoner.paroleEligibilityDate < prisoner.homeDetentionCurfewEligibilityDate) {
        releaseEligibilityType = "PED"
      } else if (prisoner.paroleEligibilityDate > prisoner.homeDetentionCurfewEligibilityDate) {
        releaseEligibilityType = "HDCED"
      }
    } else if (prisoner.paroleEligibilityDate != null) {
      releaseEligibilityType = "PED"
    } else if (prisoner.homeDetentionCurfewEligibilityDate != null) {
      releaseEligibilityType = "HDCED"
    }
    return releaseEligibilityType
  }

  private fun isAssessmentRequired(prisonerEntity: PrisonerEntity): AssessmentRequiredResult {
    // Assessment required we don't have all pathways in submitted
    val statusByType = resettlementAssessmentRepository.findLatestForEachPathwayAndType(prisonerEntity)
      .groupBy { it.assessmentType }

    val iNeedsSubmitted =
      statusByType[ResettlementAssessmentType.BCST2]?.all { it.assessmentStatus == ResettlementAssessmentStatus.SUBMITTED }
        ?: false
    val preRelease = statusByType[ResettlementAssessmentType.RESETTLEMENT_PLAN]
    val preReleaseSubmitted = preRelease?.all { it.assessmentStatus === ResettlementAssessmentStatus.SUBMITTED } ?: false
    val preReleaseInProgress = preRelease?.any { it.assessmentStatus === ResettlementAssessmentStatus.COMPLETE } ?: false

    return AssessmentRequiredResult(
      assessmentRequired = !iNeedsSubmitted && !preReleaseInProgress,
      resettlementReviewAvailable = (iNeedsSubmitted || preReleaseInProgress) && !preReleaseSubmitted,
      immediateNeedsSubmitted = iNeedsSubmitted,
      preReleaseSubmitted = preReleaseSubmitted,
    )
  }

  fun getPrisonerImageData(nomsId: String, imageId: Int): ByteArray? = prisonApiService.getPrisonerImageData(nomsId, imageId)

  @Transactional
  fun getInProgressPrisonersByPrisonId(prisonId: String, earliestReleaseDate: LocalDate, latestReleaseDate: LocalDate): List<PrisonerEntity> {
    val inProgressPrisoners = mutableListOf<PrisonerEntity>()
    val inProgressOrDonePrisoners = pathwayStatusRepository.findPrisonersByPrisonIdWithAtLeastOnePathwayNotInNotStarted(prisonId, earliestReleaseDate, latestReleaseDate)
    val donePrisoners = pathwayStatusRepository.findPrisonersByPrisonWithAllPathwaysDone(prisonId, earliestReleaseDate, latestReleaseDate)
    inProgressPrisoners.addAll(inProgressOrDonePrisoners)
    inProgressPrisoners.removeAll(donePrisoners)
    return inProgressPrisoners
  }

  @Transactional
  fun getNotStartedPrisonersByPrisonId(prisonId: String, earliestReleaseDate: LocalDate, latestReleaseDate: LocalDate) = pathwayStatusRepository.findPrisonersByPrisonIdWithAllPathwaysNotStarted(prisonId, earliestReleaseDate, latestReleaseDate)

  @Transactional
  fun getDonePrisonersByPrisonId(prisonId: String, earliestReleaseDate: LocalDate, latestReleaseDate: LocalDate) = pathwayStatusRepository.findPrisonersByPrisonWithAllPathwaysDone(prisonId, earliestReleaseDate, latestReleaseDate)

  fun getSliceOfAllPrisoners(page: Pageable): Slice<PrisonerEntity> = prisonerRepository.findAll(page)

  @Transactional
  fun updateAndSaveNewReleaseDates(prisonerEntities: Stream<PrisonerEntity>) {
    for (prisonerEntity in prisonerEntities) {
      try {
        val prisoner = prisonerSearchApiService.findPrisonerPersonalDetails(prisonerEntity.nomsId)
        setDisplayedReleaseDate(prisoner)
        prisonerEntity.releaseDate = prisoner.confirmedReleaseDate
        prisonerRepository.save(prisonerEntity)
      } catch (e: ResourceNotFoundException) {
        log.warn("Cannot update release date for prisoner ${prisonerEntity.nomsId} as no results from Prisoner Search API - skipping until next cron run.")
      }
    }
  }

  fun getPrisonerEntity(nomsId: String): PrisonerEntity = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Unable to find prisoner $nomsId in database.")
}

private data class AssessmentRequiredResult(
  val assessmentRequired: Boolean,
  val resettlementReviewAvailable: Boolean,
  val immediateNeedsSubmitted: Boolean,
  val preReleaseSubmitted: Boolean,
)
