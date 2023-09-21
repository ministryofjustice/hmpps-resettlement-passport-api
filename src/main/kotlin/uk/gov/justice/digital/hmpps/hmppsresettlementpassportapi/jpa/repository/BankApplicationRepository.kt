package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Repository
interface BankApplicationRepository : JpaRepository<BankApplicationEntity, Long> {
  fun findByPrisonerAndIsDeleted(prisoner: PrisonerEntity, isDeleted: Boolean = false): BankApplicationEntity?
}
