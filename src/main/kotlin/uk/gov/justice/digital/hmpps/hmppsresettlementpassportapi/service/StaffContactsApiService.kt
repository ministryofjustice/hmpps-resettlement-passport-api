package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Contact
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.StaffContacts

@Service
class StaffContactsApiService(val communityApiService: CommunityApiService, val keyWorkerApiService: KeyWorkerApiService) {
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

    // TODO Add in POM
    return StaffContacts(primaryPom = null, com = com, keyWorker = keyWorker)
  }
}
