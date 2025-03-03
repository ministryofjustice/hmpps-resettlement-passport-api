package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "offender_event")
@EntityListeners(AuditingEntityListener::class)
data class OffenderEventEntity(
  @Id
  val id: UUID = UUID.randomUUID(),

  val prisonerId: Long,
  val nomsId: String,
  @Enumerated(EnumType.STRING)
  val type: OffenderEventType,
  val occurredAt: ZonedDateTime,
  @Enumerated(EnumType.STRING)
  val reason: MovementReasonType? = null,
  val reasonCode: String? = null,
  @CreatedDate
  var creationDate: LocalDateTime? = null,
)

enum class OffenderEventType {
  PRISON_ADMISSION,
  PRISON_RELEASE,
}

enum class MovementReasonType {
  RECALL,
}
