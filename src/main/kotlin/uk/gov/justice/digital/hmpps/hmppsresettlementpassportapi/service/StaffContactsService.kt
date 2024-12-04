package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Contact
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.StaffContacts
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.AllocationManagerApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.KeyWorkerApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService

@Service
class StaffContactsService(val resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService, val keyWorkerApiService: KeyWorkerApiService, val allocationManagerApiService: AllocationManagerApiService, val caseAllocationService: CaseAllocationService) {
  fun getStaffContacts(nomsId: String): StaffContacts {
    // Get COM details from Delius API
    val comName = resettlementPassportDeliusApiService.getComByNomsId(nomsId)
    var com: Contact? = null
    if (comName != null) {
      com = Contact(comName)
    }

    // Get Key Worker details from Key Worker API
    val keyWorkerName = keyWorkerApiService.getKeyWorkerName(nomsId)
    var keyWorker: Contact? = null
    if (keyWorkerName != null) {
      keyWorker = Contact(keyWorkerName)
    }

    // Get POM details from Allocation Manager API
    val poms = allocationManagerApiService.getPomsByNomsId(nomsId)
    var primaryPom: Contact? = null
    if (poms.primaryPom != null) {
      primaryPom = Contact(poms.primaryPom)
    }
    var secondaryPom: Contact? = null
    if (poms.secondaryPom != null) {
      secondaryPom = Contact(poms.secondaryPom)
    }

    var resettlementWorker: Contact? = null
    val caseAllocation = caseAllocationService.getAssignedResettlementWorkerByNomsId(nomsId)
    if (caseAllocation != null) {
      resettlementWorker = Contact((caseAllocation.staffFirstname + " " + caseAllocation.staffLastname).convertNameToTitleCase())
    }
    return StaffContacts(primaryPom = primaryPom, secondaryPom = secondaryPom, com = com, keyWorker = keyWorker, resettlementWorker = resettlementWorker)
  }
}
