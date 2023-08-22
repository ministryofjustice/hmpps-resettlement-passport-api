package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonerRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearchList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

@Service
class OffenderSearchApiService(
  private val pathwayRepository: PathwayRepository,
  private val offendersSearchWebClientClientCredentials: WebClient,
  private val offendersImageWebClientCredentials: WebClient,
  private val prisonerRepository: PrisonerRepository,
  private val communityApiService: CommunityApiService,
  private val pathwayStatusRepository: PathwayStatusRepository,
  private val statusRepository: StatusRepository,
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

  private fun findPrisonersByReleaseDate(prisonId: String, earliestReleaseDate: String, latestReleaseDate: String, prisonIds: List<String>): Flow<List<PrisonersSearch>> = flow {
    var page = 0
    do {
      val pageOfData = offendersSearchWebClientClientCredentials.post()
        .uri(
          "/prisoner-search/release-date-by-prison?size={size}&page={page}&sort={sort}",
          mapOf(
            "prisonId" to prisonId,
            "size" to 50, // NB: API allows up 3,000 results per page
            "page" to page,
            "sort" to "releaseDate,ASC",
          ),
        ).bodyValue(
          PrisonerRequest(
            earliestReleaseDate = earliestReleaseDate,
            latestReleaseDate = latestReleaseDate,
            prisonIds = prisonIds,
          ),
        )
        .retrieve()
        .awaitBody<PrisonersSearchList>()
      emit(pageOfData.content!!)
      page += 1
    } while (!pageOfData.last)
  }

  /**
   * Searches for offenders in a prison using prison ID  (e.g. MDI)
   * returning a complete list.
   * Requires role PRISONER_IN_PRISON_SEARCH or PRISONER_SEARCH
   */
  suspend fun getPrisonersByPrisonId(dateRangeAPI: Boolean, searchTerm: String, prisonId: String, days: Long, pageNumber: Int, pageSize: Int, sort: String): PrisonersList {
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
    if (dateRangeAPI) {
      val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val earliestReleaseDate = LocalDate.now().minusDays(days).format(pattern)
      val latestReleaseDate = LocalDate.now().plusDays(days).format(pattern)
      val prisonIds = ArrayList<String>()
      prisonIds.add(prisonId)
      findPrisonersByReleaseDate(
        prisonId,
        earliestReleaseDate.toString(),
        latestReleaseDate.toString(),
        prisonIds,
      ).collect {
        offenders.addAll(it)
      }
    } else {
      findPrisonersBySearchTerm(prisonId, searchTerm).collect {
        offenders.addAll(it)
      }
    }
    if (offenders.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoners", prisonId)
    }
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
    val pathwayRepoData = pathwayRepository.findAll()
    searchList.forEach { prisonersSearch ->
      val prisoner = Prisoners(
        prisonersSearch.prisonerNumber,
        prisonersSearch.firstName,
        prisonersSearch.middleNames,
        prisonersSearch.lastName,
        prisonersSearch.releaseDate,
        prisonersSearch.nonDtoReleaseDateType,
      )
      val argStatus = ArrayList<PathwayStatus>()

      pathwayRepoData.forEach {
        if (it.active) {
          val pathwayStatus = PathwayStatus(Pathway.values().get(it.id.toInt() - 1), Status.NOT_STARTED.toString())
          argStatus.add(pathwayStatus)
        }
      }
      prisoner.status = argStatus
      prisonersList.add(prisoner)
    }
    return prisonersList
  }

  private suspend fun findPrisonerPersonalDetails(nomisId: String): PrisonersSearch {
    return offendersSearchWebClientClientCredentials
      .get()
      .uri(
        "/prisoner/{nomisId}",
        mapOf(
          "nomisId" to nomisId,
        ),
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Prisoner $nomisId not found") })
      .awaitBody<PrisonersSearch>()
  }

  private suspend fun findPrisonerImageDetails(nomisId: String): List<PrisonerImage> {
    return offendersImageWebClientCredentials
      .get()
      .uri(
        "/api/images/offenders/{nomisId}",
        mapOf(
          "nomisId" to nomisId,
        ),
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Prisoner $nomisId not found") })
      .awaitBody<List<PrisonerImage>>()
  }
  suspend fun getPrisonerDetailsByNomisId(nomisId: String): Prisoner {
    val prisonerSearch = findPrisonerPersonalDetails(nomisId)

    val prisonerImageDetailsList = findPrisonerImageDetails(nomisId)

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
      prisonerSearch.prisonName + "(" + prisonerSearch.prisonId + ")",
      prisonerImage?.imageId,
    )

    val argStatus = ArrayList<PathwayStatus>()
    val prisonerRepoData = prisonerRepository.findByNomsId(prisonerSearch.prisonerNumber)
    val pathwayRepoData = pathwayRepository.findAll()

    pathwayRepoData.forEach {
      if (it.active) {
        // val pathwayStatus = PathwayStatus(Pathway.values().get(it.id.toInt() - 1), Status.NOT_STARTED.toString())
        val pathwayStatusRepoData =
          prisonerRepoData?.let { it1 -> pathwayStatusRepository.findByPathwayAndPrisoner(it, it1) }
        var pathwayStatus = PathwayStatus(Pathway.values().get(it.id.toInt() - 1), Status.NOT_STARTED.toString())
        if (pathwayStatusRepoData != null) {
          pathwayStatus = PathwayStatus(
            Pathway.values().get(it.id.toInt() - 1),
            getStatusEnum(pathwayStatusRepoData),
            pathwayStatusRepoData.updatedDate.toString(),
          )
        }
        argStatus.add(pathwayStatus)
      }
    }
    // check the Prisoner Seed Completed
    addPrisonerAndInitialPathwayStatus(nomisId)

    return Prisoner(prisonerPersonal, argStatus)
  }

  private fun getStatusEnum(pathwayStatusRepoData: PathwayStatusEntity): String {
    return if (pathwayStatusRepoData.id == Status.NOT_STARTED.id) {
      Status.NOT_STARTED.toString()
    } else if (pathwayStatusRepoData.id == Status.IN_PROGRESS.id) {
      Status.IN_PROGRESS.toString()
    } else if (pathwayStatusRepoData.id == Status.SUPPORT_DECLINED.id) {
      Status.SUPPORT_DECLINED.toString()
    } else if (pathwayStatusRepoData.id == Status.SUPPORT_NOT_REQUIRED.id) {
      Status.SUPPORT_NOT_REQUIRED.toString()
    } else if (pathwayStatusRepoData.id == Status.DONE.id) {
      Status.DONE.toString()
    } else {
      Status.NOT_STARTED.toString()
    }
  }

  fun getPrisonerImageData(nomisId: String, imageId: Int): Flow<ByteArray> = flow {
    val prisonerImageDetailsList = findPrisonerImageDetails(nomisId)
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

  @Transactional
  suspend fun addPrisonerAndInitialPathwayStatus(nomisId: String) {
    // Seed the Prisoner data into the DB
    var prisonerEntity = prisonerRepository.findByNomsId(nomisId)
    @Suppress("SENSELESS_COMPARISON")
    if (prisonerEntity == null) {
      val crn = communityApiService.getCrn(nomisId)
      prisonerEntity = PrisonerEntity(null, nomisId, LocalDateTime.now(), crn.toString())
      prisonerEntity = prisonerRepository.save(prisonerEntity)
      val statusRepoData = statusRepository.findById(Status.NOT_STARTED.id)
      val pathwayRepoData = pathwayRepository.findAll()
      pathwayRepoData.forEach {
        if (it.active) {
          val pathwayStatusEntity =
            PathwayStatusEntity(null, prisonerEntity, it, statusRepoData.get(), LocalDateTime.now())
          pathwayStatusRepository.save(pathwayStatusEntity)
        }
      }
    } else if (prisonerEntity.crn == null) {
      val crn = communityApiService.getCrn(nomisId)
      if (crn != null) {
        prisonerEntity.crn = crn.toString()
        prisonerRepository.save(prisonerEntity)
      } else {
        val pathwayStatusRepoData = pathwayStatusRepository.findByPrisoner(prisonerEntity)
        if (pathwayStatusRepoData == null) {
          val statusRepoData = statusRepository.findById(Status.NOT_STARTED.id)
          val pathwayRepoData = pathwayRepository.findAll()
          pathwayRepoData.forEach {
            if (it.active) {
              val pathwayStatusEntity =
                PathwayStatusEntity(null, prisonerEntity, it, statusRepoData.get(), LocalDateTime.now())
              pathwayStatusRepository.save(pathwayStatusEntity)
            }
          }
        }
      }
    }
  }
}
