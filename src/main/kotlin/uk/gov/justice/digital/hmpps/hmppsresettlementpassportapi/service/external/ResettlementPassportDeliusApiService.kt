package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MappaData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AccommodationsDelius
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AppointmentDelius
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AppointmentsDeliusList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.CaseIdentifiers
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusAuthor
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusCreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.Manager
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.PersonalDetail
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.convertNameToTitleCase
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class ResettlementPassportDeliusApiService(
  private val rpDeliusWebClientCredentials: WebClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Cacheable("resettlement-passport-delius-api-get-crn")
  fun getCrn(nomsId: String): String? {
    val prisonersDetails = rpDeliusWebClientCredentials.get()
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

    return prisonersDetails?.crn
  }

  @Cacheable("resettlement-passport-delius-api-get-mappa-data-by-noms-id")
  fun getMappaDataByCrn(crn: String): MappaData {
    val mappaDetail = rpDeliusWebClientCredentials.get()
      .uri("/probation-cases/$crn/mappa")
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw ResourceNotFoundException("Cannot find MAPPA Data for CRN $crn in Delius API") },
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

  @Cacheable("resettlement-passport-delius-api-get-com-by-noms-id", unless = "#result == null")
  fun getComByCrn(crn: String): String? {
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
      log.warn("No COM data found in Community API for CRN $crn")
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
          // Assume there will never be more than 1000 appointments
          "size" to 1000,
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

  @Cacheable("resettlement-passport-delius-api-fetch-accommodation")
  fun fetchAccommodation(nomsId: String, crn: String): AccommodationsDelius = rpDeliusWebClientCredentials.get()
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

  @Cacheable("resettlement-passport-delius-api-get-personal-details", unless = "#result == null")
  fun getPersonalDetails(nomsId: String, crn: String): PersonalDetail? {
    val prisonersDetails = rpDeliusWebClientCredentials.get()
      .uri("/probation-cases/$crn")
      .retrieve()
      .bodyToMono<PersonalDetail>()
      .onErrorReturn(
        {
          log.warn("Unable to fetch personal details for nomsId $nomsId in delius due to error. Setting to null.", it)
          it is WebClientException
        },
        PersonalDetail(crn, null, null, null),
      )
      .block()

    return prisonersDetails
  }

  fun createAppointment(crn: String, appointment: DeliusCreateAppointment) {
    runBlocking {
      try {
        rpDeliusWebClientCredentials.post()
          .uri("/appointments/{crn}", crn)
          .bodyValue(appointment)
          .retrieve()
          .awaitBodilessEntity()
      } catch (e: WebClientResponseException) {
        log.error("Error calling create appointment delius api {}, {}", e.statusCode, e.responseBodyAsString)
        throw e
      }
    }
  }

  fun createCaseNote(crn: String, type: DeliusCaseNoteType, dateTime: OffsetDateTime, notes: String, author: DeliusAuthor, description: String?): Boolean {
    var success: Boolean
    runBlocking {
      try {
        rpDeliusWebClientCredentials.post()
          .uri("/nomis-case-note/$crn")
          .bodyValue(
            DeliusCaseNote(
              type = type,
              dateTime = dateTime,
              notes = notes,
              author = author,
              description = description,
            ),
          )
          .retrieve()
          .awaitBodilessEntity()
        success = true
      } catch (e: WebClientResponseException) {
        log.warn("Error calling post case note delius api {}, {}", e.statusCode, e.responseBodyAsString)
        success = false
      }
    }
    return success
  }
}
