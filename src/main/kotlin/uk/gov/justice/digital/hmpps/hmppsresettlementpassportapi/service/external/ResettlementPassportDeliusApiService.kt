package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MappaData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AccommodationsDelius
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AppointmentDelius
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AppointmentsDeliusList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.CaseIdentifiers
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.Manager
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.convertNameToTitleCase
import java.time.LocalDate

@Service
class ResettlementPassportDeliusApiService(
  private val rpDeliusWebClientCredentials: WebClient,
  private val prisonerRepository: PrisonerRepository,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun findCrn(nomsId: String): String? {
    return prisonerRepository.findByNomsId(nomsId)?.crn
  }

  fun getCrn(nomsId: String): String? {
    val offenderDetails = rpDeliusWebClientCredentials.get()
      .uri("/probation-cases/$nomsId/crn")
      .retrieve()
      .bodyToMono<CaseIdentifiers>()
      .onErrorReturn(
        {
          log.warn("Unable to find CRN for nomsId $nomsId in delius due to error. Setting to null.", it)
          it is WebClientException
        },
        CaseIdentifiers(null),
      )
      .block()

    return offenderDetails?.crn
  }

  fun getMappaDataByNomsId(nomsId: String): MappaData? {
    val crn = findCrn(nomsId) ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in database")
    val mappaDetail = rpDeliusWebClientCredentials.get()
      .uri("/probation-cases/$crn/mappa")
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Cannot find MAPPA Data for NomsId $nomsId / CRN $crn in Delius API") },
      )
      .bodyToMono<MappaDetail>()
      .block()
    return MappaData(
      mappaDetail?.level,
      mappaDetail?.levelDescription,
      mappaDetail?.category,
      mappaDetail?.categoryDescription,
      mappaDetail?.startDate,
      mappaDetail?.reviewDate,
    )
  }

  fun getComByNomsId(nomsId: String): String? {
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
      ).block()

    if (communityManager?.unallocated == true || communityManager?.name == null) {
      log.warn("No COM data found in Community API for NomsId $nomsId / CRN $crn")
      return null
    }

    return "${communityManager.name.forename} ${communityManager.name.surname}".convertNameToTitleCase()
  }

  fun fetchAppointments(nomsId: String, crn: String, startDate: LocalDate, endDate: LocalDate): List<AppointmentDelius> {
    val appointments = rpDeliusWebClientCredentials.get()
      .uri(
        "/appointments/{crn}?page={page}&size={size}&startDate={startDate}&endDate={endDate}",
        mapOf(
          "crn" to crn,
          "size" to 1000, // Assume there will never be more than 1000 appointments
          "page" to 0,
          "startDate" to startDate.toString(),
          "endDate" to endDate.toString(),
        ),
      )
      .retrieve().onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("NomsId $nomsId / CRN  $crn not found") })
      .bodyToMono<AppointmentsDeliusList>()
      .block() ?: throw RuntimeException("Unexpected null returned from request.")

    return appointments.results
  }

  fun fetchAccommodation(nomsId: String, crn: String): AccommodationsDelius {
    return rpDeliusWebClientCredentials.get()
      .uri(
        "/duty-to-refer-nsi/$crn",
      )
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Cannot find duty to refer nsi Data for NomsId $nomsId / CRN $crn in Delius API") },
      )
      .bodyToMono<AccommodationsDelius>()
      .block() ?: throw RuntimeException("Unexpected null returned from request.")
  }
}
