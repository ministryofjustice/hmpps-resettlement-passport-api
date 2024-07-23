package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "person_on_probation_user_otp")
data class PoPUserOTPEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @Column(name = "prisoner_id")
  val prisonerId: Long,

  @Column(name = "creation_date")
  val creationDate: LocalDateTime,

  @Column(name = "expiry_date")
  val expiryDate: LocalDateTime,

  @Column(name = "otp")
  val otp: String,

  @Column(name = "dob")
  var dob: LocalDate,
)
