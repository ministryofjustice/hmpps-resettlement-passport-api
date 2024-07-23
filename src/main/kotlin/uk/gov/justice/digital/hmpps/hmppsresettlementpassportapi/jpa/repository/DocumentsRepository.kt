package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity

@Repository
interface DocumentsRepository : JpaRepository<DocumentsEntity, Long> {
  fun findAllByPrisonerId(prisonerId: Long): DocumentsEntity?

  fun findByPrisonerIdAndId(prisonerId: Long, documentId: Long): DocumentsEntity?

  fun findAllByPrisonerIdAndCategoryOrderByCreationDateDesc(prisonerId: Long, category: DocumentCategory): List<DocumentsEntity>

  @Query(
    value = """
      select d
      from DocumentsEntity d
      join PrisonerEntity p on p.id = d.prisonerId
      where p.nomsId = :nomsId
      and d.category = :category
      order by d.creationDate desc
      limit 1
    """,
  )
  fun findFirstByNomsIdAndCategory(nomsId: String, category: DocumentCategory): DocumentsEntity?

  @Query(
    value = """
      select d
      from DocumentsEntity d
      join PrisonerEntity p on p.id = d.prisonerId
      where p.nomsId = :nomsId
      and d.category = :category
      order by d.creationDate desc
    """,
  )
  fun findAllByNomsIdAndCategory(nomsId: String, category: DocumentCategory): List<DocumentsEntity>

  @Query(
    value = """
      select d
      from DocumentsEntity d
      join PrisonerEntity p on p.id = d.prisonerId
      where p.nomsId = :nomsId
      order by d.creationDate desc
    """,
  )
  fun findAllByNomsId(nomsId: String): List<DocumentsEntity>
}
