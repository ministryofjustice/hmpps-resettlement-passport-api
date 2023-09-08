package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Contact
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.StaffContacts

@Service
class StaffContactsApiService(val communityApiService: CommunityApiService) {
  suspend fun getStaffContacts(prisonerId: String): StaffContacts {
    // Get COM details from Community API
    val comName = communityApiService.getComByNomsId(prisonerId)

    var com: Contact? = null

    if (comName != null) {
      com = Contact(comName)
    }

    // TODO Add in POM and Key Worker
    return StaffContacts(primaryPom = null, com = com, keyWorker = null)
  }
}
