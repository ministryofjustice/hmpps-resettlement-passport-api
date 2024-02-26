package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.OneLoginUserData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PoPUserApiService
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
class PoPUserOTPService(
  private val popUserOTPRepository: PoPUserOTPRepository,
  private val prisonerRepository: PrisonerRepository,
  private val popUserApiService: PoPUserApiService,
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
    val otp = SecureRandom.getInstanceStrong().nextLong(999999)
    val otpValue = String.format("%06d", otp).reversed().toLong()
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

  fun getPoPUserVerified(oneLoginUserData: OneLoginUserData): PoPUserResponse? {
    return if (oneLoginUserData.otp != null && oneLoginUserData.urn != null && oneLoginUserData.email != null) {
      val popUserOTPEntityExists = popUserOTPRepository.findByOtp(oneLoginUserData.otp.toLong())
        ?: throw ResourceNotFoundException("Person On Probation User otp  ${oneLoginUserData.otp}  not found in database")

      var prisonerEntity: Optional<PrisonerEntity>? = popUserOTPEntityExists.id?.let { prisonerRepository.findById(it) }
        ?: throw ResourceNotFoundException("Prisoner with id ${popUserOTPEntityExists.prisoner.id}  not found in database")
      popUserApiService.postPoPUserVerification(
        oneLoginUserData,
        prisonerEntity,
      )
    } else {
      throw ValidationException("required data otp, urn or email is missing")
    }
  }
}
