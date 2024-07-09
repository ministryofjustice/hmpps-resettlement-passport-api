package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Repository
interface DocumentsRepository : JpaRepository<DocumentsEntity, Long> {
  fun findAllByPrisoner(prisoner: PrisonerEntity): DocumentsEntity?

  fun findByPrisonerAndOriginalDocumentKey(prisoner: PrisonerEntity, documentKey: String): DocumentsEntity?
}
