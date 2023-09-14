package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "id_type")
data class IdTypeEntity(
  @Id
  val id: Long,

  @Column
  val name: String,
)

enum class IdType(val id: Long) {
  BIRTH_CERTIFICATE(1),
  MARRIAGE_CERTIFICATE(2),
  CIVIL_PARTNERSHIP_CERTIFICATE(3),
  ADOPTION_CERTIFICATE(4),
  DIVORCE_DECREE_ABSOLUTE_CERTIFICATE(5),
  DRIVING_LICENCE(6),
  BIOMETRIC_RESIDENCE_PERMIT(7),
  DEED_POLL_CERTIFICATE(8),
  ;

  companion object {
    fun getById(id: Long) = values().first { it.id == id }
  }
}
