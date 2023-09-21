package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "bank_application_status_log")
data class BankApplicationStatusLogEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @ManyToOne(cascade = [CascadeType.ALL])
  val bankApplication: BankApplicationEntity?,

  @Column(name = "status_changed_to")
  var status_changed_to: String,

  @Column(name = "changed_at")
  val changedAtDate: LocalDateTime,
)
