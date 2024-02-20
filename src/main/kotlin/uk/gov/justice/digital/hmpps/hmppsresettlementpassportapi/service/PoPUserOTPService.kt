package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import java.security.SecureRandom
import java.time.LocalDateTime

@Service
class PoPUserOTPService(
  private val popUserOTPRepository: PoPUserOTPRepository,
) {

  @Transactional
  fun getOTPByPrisoner(prisoner: PrisonerEntity): PoPUserOTPEntity? {
    val popUserOTP = popUserOTPRepository.findByPrisoner(prisoner)
      ?: throw ResourceNotFoundException("Prisoner with id ${prisoner.id}  not found in database")
    return popUserOTP
  }

  @Transactional
  fun getAllOTPs(): List<PoPUserOTPEntity>? {
    val popUsersOTPs = popUserOTPRepository.findAll()
    return popUsersOTPs
  }

  @Transactional
  fun deletePoPUserOTP(popUserOTP: PoPUserOTPEntity) {
    popUserOTPRepository.delete(popUserOTP)
  }

  @Transactional
  fun createPoPUserOTP(prisoner: PrisonerEntity): PoPUserOTPEntity {
    val now = LocalDateTime.now()
    val popUserOTPExists = popUserOTPRepository.findByPrisoner(prisoner)
    // val secureRandom = SecureRandom()
    // For now OTP generated is in 6 digits, for 8 digits the below value should be 99999999
    val otpValue = SecureRandom.getInstanceStrong().nextLong(999999)
    SecureRandom.getInstanceStrong()
    if (popUserOTPExists != null) {
      popUserOTPRepository.delete(popUserOTPExists)
    }
    val popUserOTPEntity = PoPUserOTPEntity(
      id = null,
      prisoner = prisoner,
      creationDate = now,
      expiryDate = now.plusDays(7).withHour(23).withMinute(59).withSecond(59),
      otp = otpValue,
    )
    popUserOTPRepository.save(popUserOTPEntity)
    return popUserOTPEntity
  }
}
