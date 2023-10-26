package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Repository
interface PrisonerRepository : JpaRepository<PrisonerEntity, Long> {
  fun findByNomsId(nomsId: String): PrisonerEntity?

  fun findAllByPrisonIdIsNull(): List<PrisonerEntity>

  fun countByPrisonId(prisonId: String): Long

  @Query("select p.nomsId from PrisonerEntity p where p.prisonId = :prisonId")
  fun findNomisIdsByPrisonId(@Param("prisonId") prisonId: String): List<String>
}
