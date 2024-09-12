package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi

import java.time.LocalDate

data class KnowledgeBasedVerification(
  val urn: String,
  val email: String,

  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val nomsId: String,
)
