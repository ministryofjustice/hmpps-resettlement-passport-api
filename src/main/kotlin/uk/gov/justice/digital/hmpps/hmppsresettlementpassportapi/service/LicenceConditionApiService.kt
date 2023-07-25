package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToFlow
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.Conditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.Licence
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.LicenceRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.LicenceSummary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class LicenceConditionApiService(
  private val cvlWebClient: WebClient,
) {

  suspend fun findLicencesByNomisId(nomisId: List<String>): Flow<LicenceSummary> =
    cvlWebClient.post()
      .uri("/licence/match")
      .bodyValue(
        LicenceRequest(
          nomsId = nomisId,
        ),
      )
      .retrieve()
      .bodyToFlow()

  suspend fun getLicenceByNomisId(nomisId: String): LicenceSummary? {
    val nomisIdList = ArrayList<String>()
    nomisIdList.add(nomisId)
    val licenceList = findLicencesByNomisId(nomisIdList)
    val licences = mutableListOf<LicenceSummary>()
    if (licenceList.toList().size == 1) {
      licenceList.collect {
        licences.addAll(licenceList.toList())
      }
    } else if (licenceList.toList().size > 1) {
      var breakFlag = false
      val pattern = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
      licenceList.collect() {
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
      licences.get(0)
    }
  }

  suspend fun fetchLicenceConditionsByLicenceId(licenceId: Long): Licence =
    cvlWebClient.get()
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

    val standardPssConditions = licence.standardPssConditions
    if (standardPssConditions != null) {
      for (item in standardPssConditions) {
        standardConditionList.add(Conditions(item.id, false, item.text))
      }
    }

    val additionalLicenceConditions = licence.additionalLicenceConditions
    for (item in additionalLicenceConditions) {
      otherConditionList.add(Conditions(item.id, item.uploadSummary.isNotEmpty(), item.text))
    }

    val additionalPssConditions = licence.additionalPssConditions
    for (item in additionalPssConditions) {
      otherConditionList.add(Conditions(item.id, item.uploadSummary.isNotEmpty(), item.text))
    }

    val beSpokeConditions = licence.bespokeConditions
    for (item in beSpokeConditions) {
      otherConditionList.add(Conditions(item.id, false, item.text))
    }

    licenceConditions.standardLicenceConditions = standardConditionList
    licenceConditions.otherLicenseConditions = otherConditionList

    return licenceConditions
  }
}
