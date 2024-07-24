package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime

@Repository
interface BankApplicationRepository : JpaRepository<BankApplicationEntity, Long> {
  fun findByPrisonerAndIsDeletedAndCreationDateBetween(prisoner: PrisonerEntity, isDeleted: Boolean = false,
                                                       fromDate: LocalDateTime = LocalDateTime.now().minusYears(50),
                                                       toDate: LocalDateTime = LocalDateTime.now()): BankApplicationEntity?
}
