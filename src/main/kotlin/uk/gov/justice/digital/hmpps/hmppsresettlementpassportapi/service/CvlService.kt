package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.LicenceSummary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CvlApiService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class CvlService(
  private val cvlApiService: CvlApiService,
) {

  // added from CvlApiService
  fun getLicenceByNomsId(nomsId: String): LicenceSummary? {
    val nomsIdList = ArrayList<String>()
    nomsIdList.add(nomsId)
    val licenceList = cvlApiService.findLicencesByNomsId(nomsIdList)
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
}
