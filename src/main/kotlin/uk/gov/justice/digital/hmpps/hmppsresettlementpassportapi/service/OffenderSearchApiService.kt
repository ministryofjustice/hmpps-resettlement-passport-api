package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDate
import java.time.Period
import kotlin.collections.ArrayList

@Service
class OffenderSearchApiService(
  private val pathwayRepository: PathwayRepository,
  private val prisonerRepository: PrisonerRepository,
  private val pathwayStatusRepository: PathwayStatusRepository,
  private val offendersSearchWebClientClientCredentials: WebClient,
  private val offendersImageWebClientCredentials: WebClient,
  private val pathwayApiService: PathwayApiService,
) {

  private fun findPrisonersBySearchTerm(prisonId: String, searchTerm: String): Flow<List<PrisonersSearch>> = flow {
    var page = 0
    do {
      val data = offendersSearchWebClientClientCredentials.get()
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
        .retrieve().onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("PrisonId $prisonId not found") })

      val pageOfData = data.awaitBodyOrNull<PrisonersSearchList>()
      if (pageOfData != null) {
        emit(pageOfData.content!!)
      }
      page += 1
    } while (!pageOfData?.last!!)
  }

  /**
   * Searches for offenders in a prison using prison ID  (e.g. MDI)
   * returning a complete list.
   * Requires role PRISONER_IN_PRISON_SEARCH or PRISONER_SEARCH
   */
  suspend fun getPrisonersByPrisonId(
    searchTerm: String,
    prisonId: String,
    days: Long,
    pageNumber: Int,
    pageSize: Int,
    sort: String,
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
    findPrisonersBySearchTerm(prisonId, searchTerm).collect {
      offenders.addAll(it)
    }
    if (offenders.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoners", prisonId)
    }

    // RP2-487 Remove all youth offenders from the results
    offenders.removeAll { it.youthOffender != null && it.youthOffender }

    val startIndex = (pageNumber * pageSize)
    if (startIndex >= offenders.size) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber",
      )
    }

    when (sort) {
      "releaseDate,ASC" -> offenders.sortBy { it.releaseDate }
      "firstName,ASC" -> offenders.sortBy { it.firstName }
      "lastName,ASC" -> offenders.sortBy { it.lastName }
      "prisonerNumber,ASC" -> offenders.sortBy { it.prisonerNumber }
      "releaseDate,DESC" -> offenders.sortByDescending { it.releaseDate }
      "firstName,DESC" -> offenders.sortByDescending { it.firstName }
      "lastName,DESC" -> offenders.sortByDescending { it.lastName }
      "prisonerNumber,DESC" -> offenders.sortByDescending { it.prisonerNumber }
      else -> throw NoDataWithCodeFoundException(
        "Data",
        "Sort value Invalid",
      )
    }

    val endIndex = (pageNumber * pageSize) + (pageSize)
    if (startIndex < endIndex && endIndex <= offenders.size) {
      val searchList = offenders.subList(startIndex, endIndex)
      val pList: List<Prisoners> = objectMapper(searchList)
      return PrisonersList(pList, pList.toList().size, pageNumber, sort, offenders.size, (endIndex == offenders.size))
    } else if (startIndex < endIndex) {
      val searchList = offenders.subList(startIndex, offenders.size)
      val pList: List<Prisoners> = objectMapper(searchList)
      return PrisonersList(pList, pList.toList().size, pageNumber, sort, offenders.size, true)
    }
    return PrisonersList(null, null, null, null, 0, false)
  }

  private fun objectMapper(searchList: List<PrisonersSearch>): List<Prisoners> {
    val prisonersList = mutableListOf<Prisoners>()
    searchList.forEach { prisonersSearch ->
      val prisoner = Prisoners(
        prisonersSearch.prisonerNumber,
        prisonersSearch.firstName,
        prisonersSearch.middleNames,
        prisonersSearch.lastName,
        prisonersSearch.releaseDate,
        prisonersSearch.nonDtoReleaseDateType,
      )

      val prisonerEntity = prisonerRepository.findByNomsId(prisonersSearch.prisonerNumber)

      if (prisonerEntity != null) {
        val pathwayStatuses = getPathwayStatuses(prisonerEntity, prisonersSearch.prisonerNumber)
        prisoner.status = pathwayStatuses
      } else {
        // We don't know about this prisoner yet so just set all the statuses to NOT_STARTED.
        val pathwayStatuses = getDefaultPathwayStatuses()
        prisoner.status = pathwayStatuses
      }

      prisonersList.add(prisoner)
    }
    return prisonersList
  }

  private suspend fun findPrisonerPersonalDetails(nomsId: String): PrisonersSearch {
    return offendersSearchWebClientClientCredentials
      .get()
      .uri(
        "/prisoner/{nomsId}",
        mapOf(
          "nomsId" to nomsId,
        ),
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Prisoner $nomsId not found") })
      .awaitBody<PrisonersSearch>()
  }

  private suspend fun findPrisonerImageDetails(nomsId: String): List<PrisonerImage> {
    return offendersImageWebClientCredentials
      .get()
      .uri(
        "/api/images/offenders/{nomsId}",
        mapOf(
          "nomsId" to nomsId,
        ),
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Prisoner $nomsId not found") })
      .awaitBody<List<PrisonerImage>>()
  }

  suspend fun getPrisonerDetailsByNomsId(nomsId: String): Prisoner {
    val prisonerSearch = findPrisonerPersonalDetails(nomsId)

    // Add initial pathway statuses if required
    pathwayApiService.addPrisonerAndInitialPathwayStatus(nomsId)

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Unable to find prisoner $nomsId in database.")

    val prisonerImageDetailsList = findPrisonerImageDetails(nomsId)
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
      prisonerSearch.firstName,
      prisonerSearch.middleNames,
      prisonerSearch.lastName,
      prisonerSearch.releaseDate,
      prisonerSearch.nonDtoReleaseDateType,
      prisonerSearch.dateOfBirth,
      age,
      prisonerSearch.cellLocation,
      prisonerImage?.imageId,
    )

    val pathwayStatuses = getPathwayStatuses(prisonerEntity, nomsId)

    return Prisoner(prisonerPersonal, pathwayStatuses)
  }

  protected fun getPathwayStatuses(
    prisonerEntity: PrisonerEntity,
    nomsId: String,
  ): ArrayList<PathwayStatus> {
    val pathwayStatuses = ArrayList<PathwayStatus>()
    val pathwayRepoData = pathwayRepository.findAll()
    pathwayRepoData.forEach { pathwayEntity ->
      if (pathwayEntity.active) {
        // Find the status in the database of each pathway for this prisoner - if any data is missing then throw an exception (should never happen)
        val pathwayStatusEntity = pathwayStatusRepository.findByPathwayAndPrisoner(pathwayEntity, prisonerEntity)
          ?: throw ResourceNotFoundException("Missing pathway_status entry in database for prisoner $nomsId and pathway ${pathwayEntity.name}")
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

  fun getDefaultPathwayStatuses(): List<PathwayStatus> {
    val pathwayStatuses = ArrayList<PathwayStatus>()
    val pathwayRepoData = pathwayRepository.findAll()
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

  fun getPrisonerImageData(nomsId: String, imageId: Int): Flow<ByteArray> = flow {
    val prisonerImageDetailsList = findPrisonerImageDetails(nomsId)
    var imageIdExists = false
    prisonerImageDetailsList.forEach {
      if (it.imageId.toInt() == imageId) {
        imageIdExists = true
        val image = offendersImageWebClientCredentials
          .get()
          .uri(
            "/api/images/$imageId/data",
          )
          .retrieve()
          .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Image not found") })
          .awaitBody<ByteArray>()
        emit(image)
      }
    }
    if (!imageIdExists) {
      throw ResourceNotFoundException("Image not found")
    }
  }
}
