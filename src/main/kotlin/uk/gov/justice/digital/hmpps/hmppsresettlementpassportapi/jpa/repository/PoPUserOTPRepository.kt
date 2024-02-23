package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Repository
interface PoPUserOTPRepository : JpaRepository<PoPUserOTPEntity, Long> {
  fun findByPrisoner(prisoner: PrisonerEntity): PoPUserOTPEntity?

  fun findByOtp(otp: Long): PoPUserOTPEntity?
}
