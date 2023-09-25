package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationStatusLogEntity

@Repository
interface BankApplicationStatusLogRepository : JpaRepository<BankApplicationStatusLogEntity, Long> {
  fun findByBankApplication(bankApplicationEntity: BankApplicationEntity): List<BankApplicationStatusLogEntity>?
}
