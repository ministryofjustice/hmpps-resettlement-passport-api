package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ContactType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity

interface DeliusContactRepository : JpaRepository<DeliusContactEntity, Long> {
  fun findByPrisonerIdAndContactType(prisonerId: Long, contactType: ContactType): List<DeliusContactEntity>

  fun findByPrisonerIdAndContactTypeAndCategory(prisonerId: Long, contactType: ContactType, category: Category): List<DeliusContactEntity>
}
