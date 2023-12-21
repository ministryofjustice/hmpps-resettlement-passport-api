package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ContactType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

interface DeliusContactRepository : JpaRepository<DeliusContactEntity, Long> {
  fun findByPrisonerAndContactType(prisoner: PrisonerEntity, contactType: ContactType): List<DeliusContactEntity>

  fun findByPrisonerAndContactTypeAndCategory(prisoner: PrisonerEntity, contactType: ContactType, category: Category): List<DeliusContactEntity>
}
