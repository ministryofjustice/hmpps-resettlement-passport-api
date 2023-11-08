package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "status")
data class StatusEntity(
  @Id
  val id: Long,

  @Column
  val name: String,

  @Column
  val active: Boolean,

  @Column(name = "creation_date")
  val creationDate: LocalDateTime,
)

enum class Status(val id: Long) {
  NOT_STARTED(1),
  IN_PROGRESS(2),
  SUPPORT_NOT_REQUIRED(3),
  SUPPORT_DECLINED(4),
  DONE(5),
  ;

  companion object {
    fun getById(id: Long) = values().first { it.id == id }
    fun getCompletedStatuses() = listOf(SUPPORT_NOT_REQUIRED.id, SUPPORT_DECLINED.id, DONE.id)
  }
}
