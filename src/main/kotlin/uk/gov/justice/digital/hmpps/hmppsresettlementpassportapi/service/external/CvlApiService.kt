package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToFlow
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

  private suspend fun findLicencesByNomsId(nomsId: List<String>): Flow<LicenceSummary> =
    cvlWebClientClientCredentials.post()
      .uri("/licence/match")
      .bodyValue(
        LicenceRequest(
          nomsId = nomsId,
        ),
      )
      .retrieve()
      .bodyToFlow()

  suspend fun getLicenceByNomsId(nomsId: String): LicenceSummary? {
    val nomsIdList = ArrayList<String>()
    nomsIdList.add(nomsId)
    val licenceList = findLicencesByNomsId(nomsIdList)
    val licences = mutableListOf<LicenceSummary>()
    if (licenceList.toList().size == 1) {
      licenceList.collect {
        licences.addAll(licenceList.toList())
      }
    } else if (licenceList.toList().size > 1) {
      var breakFlag = false
      val pattern = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
      licenceList.collect {
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
    return if (licences.isEmpty()) {
      null
    }; else {
      licences[0]
    }
  }

  private suspend fun fetchLicenceConditionsByLicenceId(licenceId: Long): Licence =
    cvlWebClientClientCredentials.get()
      .uri(
        "/licence/id/{licenceId}",
        mapOf(
          "licenceId" to licenceId,
        ),
      )
      .retrieve()
      .awaitBody<Licence>()

  suspend fun getLicenceConditionsByLicenceId(licenceId: Long): LicenceConditions {
    val licence = fetchLicenceConditionsByLicenceId(licenceId)
    val licenceConditions = LicenceConditions(licenceId, "", emptyList(), emptyList())
    licenceConditions.status = licence.statusCode
    val standardConditionList = mutableListOf<Conditions>()
    val otherConditionList = mutableListOf<Conditions>()

    val standardLicenceConditions = licence.standardLicenceConditions
    if (standardLicenceConditions != null) {
      for (item in standardLicenceConditions) {
        standardConditionList.add(Conditions(item.id, false, item.text))
      }
    }

    val additionalLicenceConditions = licence.additionalLicenceConditions
    for (item in additionalLicenceConditions) {
      otherConditionList.add(Conditions(item.id, item.uploadSummary.isNotEmpty(), item.expandedText))
    }

    val beSpokeConditions = licence.bespokeConditions
    for (item in beSpokeConditions) {
      otherConditionList.add(Conditions(item.id, false, item.text))
    }

    licenceConditions.standardLicenceConditions = standardConditionList
    licenceConditions.otherLicenseConditions = otherConditionList

    return licenceConditions
  }

  fun getImageFromLicenceIdAndConditionId(licenceId: String, conditionId: String): Flow<ByteArray> = flow {
    val image = cvlWebClientClientCredentials
      .get()
      .uri(
        "/exclusion-zone/id/$licenceId/condition/id/$conditionId/full-size-image",
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Image not found") })
      .awaitBody<ByteArray>()
    emit(image)
  }
}