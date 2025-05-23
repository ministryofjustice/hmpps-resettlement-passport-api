package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagsEntity

@Repository
interface ProfileTagsRepository : JpaRepository<ProfileTagsEntity, Long> {

  fun findByPrisonerId(prisonerId: Long): ProfileTagsEntity

  fun findAllByPrisonerId(prisonerId: Long): List<ProfileTagsEntity>

  fun findFirstByPrisonerId(prisonerId: Long): ProfileTagsEntity

  fun existsProfileTagsEntityByPrisonerId(prisonerId: Long): Boolean

  @Query(
    value = """
      select t
      from ProfileTagsEntity t
      join PrisonerEntity p on t.prisonerId = p.id
      where p.nomsId = :nomsId
    """,
  )
  fun findByNomsId(nomsId: String): List<ProfileTagsEntity>
}
