package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Contact
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.StaffContacts

@Service
class StaffContactsApiService(val communityApiService: ResettlementPassportDeliusApiService, val keyWorkerApiService: KeyWorkerApiService, val allocationManagerApiService: AllocationManagerApiService) {
  suspend fun getStaffContacts(prisonerId: String): StaffContacts {
    // Get COM details from Community API
    val comName = communityApiService.getComByNomsId(prisonerId)
    var com: Contact? = null
    if (comName != null) {
      com = Contact(comName)
    }

    // Get Key Worker details from Key Worker API
    val keyWorkerName = keyWorkerApiService.getKeyWorkerName(prisonerId)
    var keyWorker: Contact? = null
    if (keyWorkerName != null) {
      keyWorker = Contact(keyWorkerName)
    }

    // Get POM details from Allocation Manager API
    val prisonOffenderManagers = allocationManagerApiService.getPomsByNomsId(prisonerId)
    var primaryPom: Contact? = null
    if (prisonOffenderManagers.primaryPom != null) {
      primaryPom = Contact(prisonOffenderManagers.primaryPom)
    }
    var secondaryPom: Contact? = null
    if (prisonOffenderManagers.secondaryPom != null) {
      secondaryPom = Contact(prisonOffenderManagers.secondaryPom)
    }

    return StaffContacts(primaryPom = primaryPom, secondaryPom = secondaryPom, com = com, keyWorker = keyWorker)
  }
}
