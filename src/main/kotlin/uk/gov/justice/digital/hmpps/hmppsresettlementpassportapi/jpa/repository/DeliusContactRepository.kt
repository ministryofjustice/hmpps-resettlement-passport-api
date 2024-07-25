package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ContactType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity
import java.time.LocalDateTime

interface DeliusContactRepository : JpaRepository<DeliusContactEntity, Long> {
  fun findByPrisonerIdAndContactType(prisonerId: Long, contactType: ContactType): List<DeliusContactEntity>

  fun findByPrisonerIdAndContactTypeAndCreatedDateBetween(prisonerId: Long, contactType: ContactType,
                                                        fromDate: LocalDateTime, toDate: LocalDateTime ): List<DeliusContactEntity>

  fun findByPrisonerIdAndContactTypeAndCategory(prisonerId: Long, contactType: ContactType, category: Category): List<DeliusContactEntity>
}
