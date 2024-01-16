package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "assessment_status")
data class ResettlementAssessmentStatusEntity(
  @Id
  val id: Long,

  @Column
  val name: String,

  @Column
  val active: Boolean,

  @Column(name = "creation_date")
  val creationDate: LocalDateTime,
)

enum class ResettlementAssessmentStatus(val id: Long) {
  /**
   * Assessment has not been completed
   */
  NOT_STARTED(1),

  /**
   * Not used currently
   */
  IN_PROGRESS(2),

  /**
   * Assessment has been completed but the case note has not been submitted
   */
  COMPLETE(3),

  /**
   * Assessment has been completed and case note has been submitted
   */
  SUBMITTED(4),
  ;

  companion object {
    fun getById(id: Long) = values().first { it.id == id }
  }
}
