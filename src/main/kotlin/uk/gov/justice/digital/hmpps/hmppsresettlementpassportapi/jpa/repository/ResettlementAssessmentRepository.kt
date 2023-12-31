package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType

interface ResettlementAssessmentRepository : JpaRepository<ResettlementAssessmentEntity, Long> {
  fun findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisoner: PrisonerEntity, pathwayEntity: PathwayEntity, assessmentType: ResettlementAssessmentType): ResettlementAssessmentEntity
}
