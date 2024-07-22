package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserOTP
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.OneLoginData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PoPUserApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import java.time.LocalDateTime
import java.util.Optional

@Service
class PoPUserOTPService(
  private val popUserOTPRepository: PoPUserOTPRepository,
  private val prisonerRepository: PrisonerRepository,
  private val popUserApiService: PoPUserApiService,
  private val prisonerSearchApiService: PrisonerSearchApiService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getOTPByPrisoner(prisoner: PrisonerEntity): PoPUserOTP? {
    val popUserOTP: PoPUserOTP
    val popUserOTPEntity = popUserOTPRepository.findByPrisonerId(prisoner.id())
      ?: throw ResourceNotFoundException("OTP for Prisoner with id ${prisoner.id} not found in database")
    popUserOTP =
      PoPUserOTP(
        popUserOTPEntity.id,
        popUserOTPEntity.creationDate,
        popUserOTPEntity.expiryDate,
        popUserOTPEntity.otp,

      )

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
  fun createPoPUserOTP(prisoner: PrisonerEntity): PoPUserOTP {
    val now = LocalDateTime.now()
    val popUserOTPExists = popUserOTPRepository.findByPrisonerId(prisoner.id())
    val prisonerDOB = prisonerSearchApiService.findPrisonerPersonalDetails(prisoner.nomsId).dateOfBirth
      ?: throw ValidationException("Person On Probation User DOB not found in Prisoner Search Service.")

    // For now OTP generated is in 6 digits, for 8 digits the below value should be 99999999
    val otpValue = randomAlphaNumericString()
    if (popUserOTPExists != null) {
      popUserOTPRepository.delete(popUserOTPExists)
    }
    val popUserOTPEntity = PoPUserOTPEntity(
      id = null,
      prisonerId = prisoner.id(),
      creationDate = now,
      expiryDate = now.plusDays(7).withHour(23).withMinute(59).withSecond(59),
      otp = otpValue,
      dob = prisonerDOB,
    )
    popUserOTPRepository.save(popUserOTPEntity)
    val popUserOTP =
      PoPUserOTP(
        popUserOTPEntity.id,
        popUserOTPEntity.creationDate,
        popUserOTPEntity.expiryDate,
        popUserOTPEntity.otp,

      )
    return popUserOTP
  }

  @Transactional
  fun getPoPUserVerified(oneLoginData: OneLoginData): PoPUserResponse? {
    if (oneLoginData.otp != null && oneLoginData.dob != null && oneLoginData.urn != null) {
      val popUserOTPEntityExists = popUserOTPRepository.findByOtpAndDobAndExpiryDateIsGreaterThan(
        oneLoginData.otp,
        oneLoginData.dob,
        LocalDateTime.now(),
      )
        ?: throw ResourceNotFoundException("Person On Probation User otp  ${oneLoginData.otp}  not found in database or expired.")

      val prisonerEntity: Optional<PrisonerEntity>? = prisonerRepository.findById(popUserOTPEntityExists.prisonerId)
      val prisoner = prisonerSearchApiService.findPrisonerPersonalDetails(prisonerEntity!!.get().nomsId)

      if (!prisonerEntity.isEmpty) {
        val response = popUserApiService.postPoPUserVerification(
          oneLoginData,
          prisonerEntity,
          prisoner,
        )
        if (response != null) {
          popUserOTPRepository.delete(popUserOTPEntityExists)
        } else {
          throw RuntimeException("Person On Probation User Verification failed for prisonerId ${popUserOTPEntityExists.prisonerId} ")
        }
        return response
      } else {
        throw ResourceNotFoundException("Prisoner with id ${popUserOTPEntityExists.prisonerId}  not found in database")
      }
    } else {
      throw ValidationException("required data otp, urn or dob  is missing")
    }
  }

  @Transactional
  fun deleteExpiredPoPUserOTP() {
    log.info("Started running scheduled deleteExpiredPoPUserOTP job")
    popUserOTPRepository.deleteByExpiryDateIsLessThan(LocalDateTime.now())
    log.info("Finished running scheduled deleteExpiredPoPUserOTP job")
  }

  @Transactional
  fun getPoPUserOTPByPrisoner(prisoner: PrisonerEntity): PoPUserOTPEntity {
    val popUserOTP = popUserOTPRepository.findByPrisonerId(prisoner.id()) ?: throw ResourceNotFoundException("OTP for Prisoner with id ${prisoner.id} not found in database")
    return popUserOTP
  }
}
