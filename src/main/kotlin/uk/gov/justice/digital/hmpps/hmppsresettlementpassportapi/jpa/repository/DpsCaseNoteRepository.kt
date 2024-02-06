package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DpsCaseNoteEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Repository
interface DpsCaseNoteRepository : JpaRepository<DpsCaseNoteEntity, Long> {
  fun findByPrisoner(prisoner: PrisonerEntity): List<DpsCaseNoteEntity>

  @Query("select a from DpsCaseNoteEntity a where a.prisoner = :prisoner and a.pathway.id = :pathwayId")
  fun findByPrisonerAndPathway(prisoner: PrisonerEntity, pathwayId: Long): List<DpsCaseNoteEntity>
}
