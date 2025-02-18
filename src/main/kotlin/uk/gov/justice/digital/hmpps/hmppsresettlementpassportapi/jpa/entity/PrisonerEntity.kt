package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "prisoner")
data class PrisonerEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "noms_id")
  val nomsId: String,

  @Column(name = "creation_date")
  val creationDate: LocalDateTime = LocalDateTime.now(),

  @Column(name = "prison_id")
  var prisonId: String?,

  @Column(name = "legacy_profile")
  var legacyProfile: Boolean? = null,
) {
  fun id() = id ?: throw IllegalStateException("Tried to get id before saving")
}
