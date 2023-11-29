package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoner
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerPersonal
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoners
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonerImage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearchList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PathwayAndStatusService
import java.time.LocalDate
import java.time.Period

@Service
class OffenderSearchApiService(
  private val prisonerRepository: PrisonerRepository,
  private val offenderSearchWebClientClientCredentials: WebClient,
  private val pathwayAndStatusService: PathwayAndStatusService,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val prisonApiService: PrisonApiService,
) {

  fun findPrisonersBySearchTerm(prisonId: String, searchTerm: String?): List<PrisonersSearch> {
    val listToReturn = mutableListOf<PrisonersSearch>()

    var page = 0
    do {
      val data = offenderSearchWebClientClientCredentials.get()
        .uri(
          "/prison/{prisonId}/prisoners?term={term}&size={size}&page={page}&sort={sort}",
          mapOf(
            "prisonId" to prisonId,
            "term" to searchTerm,
            "size" to 500, // NB: API allows up 3,000 results per page
            "page" to page,
            "sort" to "prisonerNumber",
          ),
        )
        .retrieve()
        .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("PrisonId $prisonId not found") })

      val pageOfData = data.bodyToMono<PrisonersSearchList>().block()
      if (pageOfData != null) {
        listToReturn.addAll(pageOfData.content!!)
      }
      page += 1
    } while (!pageOfData?.last!!)
    return listToReturn
  }

  /**
   * Searches for offenders in a prison using prison ID  (e.g. MDI)
   * returning a complete list.
   * Requires role PRISONER_IN_PRISON_SEARCH or PRISONER_SEARCH
   */
  fun getPrisonersByPrisonId(
    searchTerm: String?,
    prisonId: String,
    days: Int,
    pathwayView: Pathway?,
    pathwayStatus: Status?,
    pageNumber: Int,
    pageSize: Int,
    sort: String? = "ASC",
  ): PrisonersList {
    val offenders = mutableListOf<PrisonersSearch>()
    if (prisonId.isBlank() || prisonId.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoners", prisonId)
    }

    if (pageNumber < 0 || pageSize <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber and Size $pageSize",
      )
    }
    findPrisonersBySearchTerm(prisonId, searchTerm).forEach {
      setDisplayedReleaseDate(it)
      offenders.add(it)
    }
    if (days > 0) {
      val earliestReleaseDate = LocalDate.now().minusDays(1)
      val latestReleaseDate = LocalDate.now().plusDays(days.toLong())
      offenders.removeAll { it.displayReleaseDate == null || it.displayReleaseDate!! <= earliestReleaseDate || it.displayReleaseDate!! > latestReleaseDate }
    } else {
      val earliestReleaseDate = LocalDate.now().minusDays(1)
      offenders.removeAll { (it.displayReleaseDate != null && it.displayReleaseDate!! <= earliestReleaseDate) }
    }

    if (offenders.isEmpty()) {
      return PrisonersList(emptyList(), pageSize, pageNumber, sort, 0, true)
    }

    val startIndex = (pageNumber * pageSize)
    if (startIndex >= offenders.size) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber",
      )
    }

    val fullList = objectMapper(offenders, pathwayView, pathwayStatus)

    sortPrisonersByNomsId(sort, fullList)

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

  fun sortPrisoners(
    sort: String?,
    offenders: MutableList<Prisoners>,
  ) {
    when (sort) {
      "releaseDate,ASC" -> offenders.sortWith(compareBy(nullsFirst()) { it.releaseDate })
      "paroleEligibilityDate,ASC" -> offenders.sortWith(compareBy(nullsLast()) { it.paroleEligibilityDate })
      "name,ASC" -> offenders.sortWith(compareBy { "${it.lastName}, ${it.firstName}" })
      "lastUpdatedDate,ASC" -> offenders.sortWith(compareBy(nullsLast()) { it.lastUpdatedDate })
      "prisonerNumber,ASC" -> offenders.sortBy { it.prisonerNumber }
      "pathwayStatus,ASC" -> offenders.sortBy { it.pathwayStatus }
      "releaseDate,DESC" -> offenders.sortWith(compareByDescending(nullsLast()) { it.releaseDate })
      "paroleEligibilityDate,DESC" -> offenders.sortWith(compareByDescending(nullsLast()) { it.paroleEligibilityDate })
      "name,DESC" -> offenders.sortWith(compareByDescending(nullsLast()) { "${it.lastName}, ${it.firstName}" })
      "lastUpdatedDate,DESC" -> offenders.sortWith(compareByDescending(nullsLast()) { it.lastUpdatedDate })
      "prisonerNumber,DESC" -> offenders.sortWith(compareByDescending(nullsLast()) { it.prisonerNumber })
      "pathwayStatus,DESC" -> offenders.sortByDescending { it.pathwayStatus }

      else -> throw NoDataWithCodeFoundException(
        "Data",
        "Sort value Invalid",
      )
    }
  }

  fun sortPrisonersByNomsId(
    sort: String? = "ASC",
    offenders: MutableList<Prisoners>,
  ) {
    val sortNoms = sort?.split(",")?.last()
    when (sortNoms) {
      "ASC" -> offenders.sortWith(compareBy(nullsLast()) { it.prisonerNumber })
      "DESC" -> offenders.sortWith(compareByDescending(nullsFirst()) { it.prisonerNumber })
    }
  }

  private fun compareByDescending(comparator: () -> Int, selector: (PrisonersSearch) -> String) {
  }

  private fun objectMapper(
    searchList: List<PrisonersSearch>,
    pathwayView: Pathway?,
    pathwayStatusToFilter: Status?,
  ): MutableList<Prisoners> {
    val prisonersList = mutableListOf<Prisoners>()
    searchList.forEach { prisonersSearch ->

      val prisonerEntity = prisonerRepository.findByNomsId(prisonersSearch.prisonerNumber)

      val pathwayStatuses: List<PathwayStatus>?
      val sortedPathwayStatuses: List<PathwayStatus>?
      val pathwayStatus: Status?
      val lastUpdatedDate: LocalDate?

      if (prisonerEntity != null) {
        pathwayStatuses = if (pathwayView == null) getPathwayStatuses(prisonerEntity) else null
        sortedPathwayStatuses = pathwayStatuses?.sortedWith(compareBy(nullsLast()) { it.lastDateChange })
        lastUpdatedDate =
          if (pathwayView == null) {
            sortedPathwayStatuses?.first()?.lastDateChange
          } else {
            getPathwayStatusLastUpdated(
              prisonerEntity,
              pathwayView,
            )
          }
        pathwayStatus = if (pathwayView != null) getPathwayStatus(prisonerEntity, pathwayView) else null
      } else {
        // We don't know about this prisoner yet so just set all the statuses to NOT_STARTED.
        pathwayStatuses = if (pathwayView == null) getDefaultPathwayStatuses() else null
        pathwayStatus = if (pathwayView != null) Status.NOT_STARTED else null
        lastUpdatedDate = null
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
        )
        prisonersList.add(prisoner)
      }
    }
    return prisonersList
  }

  fun findPrisonerPersonalDetails(nomsId: String): PrisonersSearch {
    return offenderSearchWebClientClientCredentials
      .get()
      .uri(
        "/prisoner/{nomsId}",
        mapOf(
          "nomsId" to nomsId,
        ),
      )
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Prisoner $nomsId not found in offender search api") },
      )
      .bodyToMono<PrisonersSearch>()
      .block() ?: throw RuntimeException("Unexpected null returned from request.")
  }

  fun getPrisonerDetailsByNomsId(nomsId: String): Prisoner {
    val prisonerSearch = findPrisonerPersonalDetails(nomsId)
    setDisplayedReleaseDate(prisonerSearch)

    checkPrisonerIsInActivePrison(prisonerSearch)

    // Add initial pathway statuses if required
    pathwayAndStatusService.addPrisonerAndInitialPathwayStatus(
      nomsId,
      prisonerSearch.prisonId,
      prisonerSearch.displayReleaseDate,
    )

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Unable to find prisoner $nomsId in database.")

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
    )

    val pathwayStatuses = getPathwayStatuses(prisonerEntity)

    return Prisoner(prisonerPersonal, pathwayStatuses)
  }

  fun checkPrisonerIsInActivePrison(prisoner: PrisonersSearch) {
    // Send back a 404 if the prisonId is not in the active list as we should only display data for non-released prisoners.
    if (!prisonRegisterApiService.getActivePrisonsList().map { it.id }.contains(prisoner.prisonId)) {
      throw ResourceNotFoundException("Prisoner with nomsId ${prisoner.prisonerNumber} and prisonId ${prisoner.prisonId} not found in any active prison")
    }
  }

  protected fun getPathwayStatuses(

    prisonerEntity: PrisonerEntity,
  ): ArrayList<PathwayStatus> {
    val pathwayStatuses = ArrayList<PathwayStatus>()
    val pathwayRepoData = pathwayAndStatusService.findAllPathways()
    pathwayRepoData.forEach { pathwayEntity ->
      if (pathwayEntity.active) {
        // Find the status in the database of each pathway for this prisoner - if any data is missing then throw an exception (should never happen)
        val pathwayStatusEntity =
          pathwayAndStatusService.findPathwayStatusFromPathwayAndPrisoner(pathwayEntity, prisonerEntity)
        val pathwayStatus = PathwayStatus(
          Pathway.getById(pathwayEntity.id),
          Status.getById(pathwayStatusEntity.status.id),
          pathwayStatusEntity.updatedDate?.toLocalDate(),
        )
        pathwayStatuses.add(pathwayStatus)
      }
    }
    return pathwayStatuses
  }

  private fun getPathwayStatus(prisonerEntity: PrisonerEntity, pathwayView: Pathway): Status {
    val pathwayStatusEntity = pathwayAndStatusService.findPathwayStatusFromPathwayAndPrisoner(
      pathwayAndStatusService.getPathwayEntity(pathwayView),
      prisonerEntity,
    )
    return Status.getById(pathwayStatusEntity.status.id)
  }

  private fun getPathwayStatusLastUpdated(prisonerEntity: PrisonerEntity, pathwayView: Pathway): LocalDate? {
    val pathwayStatusEntity = pathwayAndStatusService.findPathwayStatusFromPathwayAndPrisoner(
      pathwayAndStatusService.getPathwayEntity(pathwayView),
      prisonerEntity,
    )
    return pathwayStatusEntity.updatedDate?.toLocalDate()
  }

  fun getDefaultPathwayStatuses(): List<PathwayStatus> {
    val pathwayStatuses = ArrayList<PathwayStatus>()
    val pathwayRepoData = pathwayAndStatusService.findAllPathways()
    pathwayRepoData.forEach { pathwayEntity ->
      if (pathwayEntity.active) {
        val pathwayStatus = PathwayStatus(
          Pathway.getById(pathwayEntity.id),
          Status.NOT_STARTED,
          null,
        )
        pathwayStatuses.add(pathwayStatus)
      }
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
}
