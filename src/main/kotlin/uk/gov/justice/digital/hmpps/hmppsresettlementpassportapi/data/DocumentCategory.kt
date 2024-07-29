package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity

enum class DocumentCategory(val displayName: String) {
  LICENCE_CONDITIONS("Licence Conditions"),
}

data class DocumentResponse(
  val value: DocumentsEntity,
)
