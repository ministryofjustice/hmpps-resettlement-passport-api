package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

interface CaseAllocationCountResponse {
  val staffId: Int
  val firstName: String?
  val lastName: String?
  val casesAssigned: Int?
}
