package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "licence_conditions_change_audit")
data class LicenceConditionChangeAuditEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @OneToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "prisoner_id", referencedColumnName = "id")
  val prisoner: PrisonerEntity,

  @Column(columnDefinition = "TEXT", name = "licence_conditions_json")
  val licenceConditionsJson: String,

  @Column(name = "creation_date")
  val creationDate: LocalDateTime,

)
