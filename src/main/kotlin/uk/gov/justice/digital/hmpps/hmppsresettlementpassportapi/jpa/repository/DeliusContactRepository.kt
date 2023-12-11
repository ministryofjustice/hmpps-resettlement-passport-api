package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

interface DeliusContactRepository : JpaRepository<DeliusContactEntity, Long> {
  fun findByPrisoner(prisoner: PrisonerEntity): List<DeliusContactEntity>

  fun findByPrisonerAndCategory(prisoner: PrisonerEntity, category: Category): List<DeliusContactEntity>
}
