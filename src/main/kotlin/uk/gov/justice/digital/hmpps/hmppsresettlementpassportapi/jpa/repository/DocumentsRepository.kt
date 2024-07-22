package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity

@Repository
interface DocumentsRepository : JpaRepository<DocumentsEntity, Long> {
  fun findAllByPrisonerId(prisonerId: Long): DocumentsEntity?

  fun findByPrisonerIdAndId(prisonerId: Long, documentId: Long): DocumentsEntity?

  fun findAllByPrisonerIdAndCategoryOrderByCreationDateDesc(prisonerId: Long, category: DocumentCategory): List<DocumentsEntity>
}
