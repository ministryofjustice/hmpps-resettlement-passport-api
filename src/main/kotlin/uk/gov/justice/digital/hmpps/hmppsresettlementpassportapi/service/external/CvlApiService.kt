package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Conditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.Licence
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.LicenceRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.LicenceSummary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class CvlApiService(
  private val cvlWebClientClientCredentials: WebClient,
) {

  private fun findLicencesByNomsId(nomsId: List<String>): List<LicenceSummary> =
    cvlWebClientClientCredentials.post()
      .uri("/licence/match")
      .bodyValue(
        LicenceRequest(
          nomsId = nomsId,
        ),
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Licence condition not found") })
      .bodyToFlux<LicenceSummary>()
      .collectList()
      .block() ?: throw RuntimeException("Unexpected null returned from request.")

  fun getLicenceByNomsId(nomsId: String): LicenceSummary? {
    val nomsIdList = ArrayList<String>()
    nomsIdList.add(nomsId)
    val licenceList = findLicencesByNomsId(nomsIdList)
    val licences = mutableListOf<LicenceSummary>()
    if (licenceList.size == 1) {
      licences.addAll(licenceList)
    } else if (licenceList.toList().size > 1) {
      var breakFlag = false
      val pattern = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
      licenceList.forEach {
        if (it.dateCreated != null) {
          val dateCreated = LocalDateTime.parse(it.dateCreated, pattern)
          if (licences.isEmpty()) {
            licences.add(0, it)
          } else {
            if (it.licenceStatus == "ACTIVE") {
              licences.add(0, it)
              breakFlag = true
            } else if (!breakFlag && dateCreated.isAfter(LocalDateTime.parse(it.dateCreated, pattern))) {
              licences.add(0, it)
            }
          }
        }
      }
    }
    return if (licences.isEmpty()) {
      null
    } else {
      licences[0]
    }
  }

  private fun fetchLicenceConditionsByLicenceId(licenceId: Long): Licence =
    cvlWebClientClientCredentials.get()
      .uri(
        "/licence/id/{licenceId}",
        mapOf(
          "licenceId" to licenceId,
        ),
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Licence condition not found") })
      .bodyToMono<Licence>()
      .block() ?: throw RuntimeException("Unexpected null returned from request.")

  fun getLicenceConditionsByLicenceId(licenceId: Long): LicenceConditions {
    val licence = fetchLicenceConditionsByLicenceId(licenceId)
    val licenceConditions = LicenceConditions(licenceId, "", licence.licenceStartDate, licence.licenceExpiryDate, emptyList(), emptyList())
    licenceConditions.status = licence.statusCode

    val standardConditionList = mutableListOf<Conditions>()
    val otherConditionList = mutableListOf<Conditions>()

    val standardLicenceConditions = licence.standardLicenceConditions
    if (standardLicenceConditions != null) {
      for (item in standardLicenceConditions) {
        standardConditionList.add(Conditions(item.id, false, item.text, item.sequence))
      }
    }

    val additionalLicenceConditions = licence.additionalLicenceConditions
    for (item in additionalLicenceConditions) {
      otherConditionList.add(Conditions(item.id, item.uploadSummary.isNotEmpty(), item.expandedText, item.sequence))
    }

    val beSpokeConditions = licence.bespokeConditions
    for (item in beSpokeConditions) {
      otherConditionList.add(Conditions(item.id, false, item.text, item.sequence))
    }

    licenceConditions.standardLicenceConditions = standardConditionList
    licenceConditions.otherLicenseConditions = otherConditionList

    return licenceConditions
  }

  fun getImageFromLicenceIdAndConditionId(licenceId: String, conditionId: String): ByteArray {
    return cvlWebClientClientCredentials
      .get()
      .uri(
        "/exclusion-zone/id/$licenceId/condition/id/$conditionId/full-size-image",
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Image not found") })
      .bodyToMono<ByteArray>()
      .block() ?: throw RuntimeException("Unexpected null returned from request.")
  }
}
