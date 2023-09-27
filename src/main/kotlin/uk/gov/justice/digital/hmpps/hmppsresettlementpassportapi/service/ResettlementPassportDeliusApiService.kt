package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MappaData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AppointmentsDeliusList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.CaseIdentifiers
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.Manager
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDate

@Service
class ResettlementPassportDeliusApiService(
  private val rpDeliusWebClientCredentials: WebClient,
  private val prisonerRepository: PrisonerRepository,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun findCrn(nomsId: String): String? {
    return prisonerRepository.findByNomsId(nomsId)?.crn
  }

  suspend fun getCrn(nomsId: String): String? {
    val offenderDetails = rpDeliusWebClientCredentials.get()
      .uri("/probation-cases/$nomsId/crn")
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in Delius API") },
      )
      .bodyToMono<CaseIdentifiers>()
      .awaitSingle()

    return offenderDetails.crn
  }

  suspend fun getMappaDataByNomsId(nomsId: String): MappaData? {
    val crn = findCrn(nomsId) ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in database")
    val mappaDetail = rpDeliusWebClientCredentials.get()
      .uri("/probation-cases/$crn/mappa")
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Cannot find MAPPA Data for NomsId $nomsId / CRN $crn in Delius API") },
      )
      .bodyToMono<MappaDetail>()
      .awaitSingle()
    return MappaData(
      mappaDetail.level,
      mappaDetail.levelDescription,
      mappaDetail.category,
      mappaDetail.categoryDescription,
      mappaDetail.startDate,
      mappaDetail.reviewDate,
    )
  }

  suspend fun getComByNomsId(nomsId: String): String? {
    val crn = findCrn(nomsId) ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in database")

    val communityManager = rpDeliusWebClientCredentials.get()
      .uri("/probation-cases/$crn/community-manager")
      .retrieve()
      .bodyToMono<Manager>()
      .onErrorReturn(
        {
          log.warn("Unexpected error from Delius API - ignoring but COM data will be missing from response!", it)
          true
        },
        Manager(null, true),
      ).awaitSingle()

    if (communityManager.unallocated || communityManager.name == null) {
      log.warn("No COM data found in Community API for NomsId $nomsId / CRN $crn")
      return null
    }

    return "${communityManager.name.forename} ${communityManager.name.surname}".convertNameToTitleCase()
  }

  fun fetchAppointments(crn: String, startDate: LocalDate, endDate: LocalDate, page: Int, size: Int): Flow<AppointmentsDeliusList> = flow {
    val data = rpDeliusWebClientCredentials.get()
      .uri(
        "/appointments/{crn}?page={page}&size={size}&startDate={startDate}&endDate={endDate}",
        mapOf(
          "crn" to crn,
          "size" to size,
          "page" to page,
          "startDate" to startDate.toString(),
          "endDate" to endDate.toString(),
        ),
      )
      .retrieve().onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("CRN  $crn not found") })
    val pageOfData = data.awaitBodyOrNull<AppointmentsDeliusList>()
    if (pageOfData != null) {
      emit(pageOfData)
    }
  }
}
