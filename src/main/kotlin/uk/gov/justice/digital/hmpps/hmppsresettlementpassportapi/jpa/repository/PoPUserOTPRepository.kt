package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface PoPUserOTPRepository : JpaRepository<PoPUserOTPEntity, Long> {
  fun findByPrisonerId(prisonerId: Long): PoPUserOTPEntity?

  fun findByOtpAndDobAndExpiryDateIsGreaterThan(otp: String?, dob: LocalDate, expiryDate: LocalDateTime): PoPUserOTPEntity?

  fun deleteByExpiryDateIsLessThan(expiryDate: LocalDateTime)
}
