package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatusAndCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status

@Service
class ResettlementAssessmentResetService(
  private val resettlementAssessmentService: ResettlementAssessmentService,
  private val pathwayPatchService: PathwayPatchService,
) {

  @Transactional
  fun resetProfile(nomsId: String, auth: String) {
    resettlementAssessmentService.deleteAllResettlementAssessments(nomsId)

    Pathway.entries.forEach { pathway ->
      pathwayPatchService.updatePathwayStatusWithCaseNote(nomsId, getResetProfilePathwayStatusAndCaseNote(pathway), auth)
    }
  }
}

fun getResetProfilePathwayStatusAndCaseNote(pathway: Pathway) =
  PathwayStatusAndCaseNote(
    pathway,
    Status.NOT_STARTED,
    "Resettlement status set to: ${Status.NOT_STARTED.name}",
  )
