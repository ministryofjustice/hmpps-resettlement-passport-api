package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Repository
interface PathwayStatusRepository : JpaRepository<PathwayStatusEntity, Long> {
  fun findByPathwayAndPrisoner(pathway: PathwayEntity, prisoner: PrisonerEntity): PathwayStatusEntity?

  fun findByPrisoner(prisoner: PrisonerEntity): PathwayStatusEntity?
}
