package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit

import kotlinx.serialization.Serializable
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType

@Serializable
data class AuditDetails(
  val assessmentType: ResettlementAssessmentType? = null,
  val pathway: Pathway? = null,
)
