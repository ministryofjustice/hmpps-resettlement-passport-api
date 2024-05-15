package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AssessmentSkipReason
import java.time.LocalDateTime

@Entity
@Table(name = "assessment_skip")
@EntityListeners(AuditingEntityListener::class)
data class AssessmentSkipEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
  val assessmentType: ResettlementAssessmentType,
  val prisonerId: Long,
  @Enumerated(EnumType.STRING)
  val reason: AssessmentSkipReason,
  val moreInfo: String? = null,
  @CreatedBy
  var createdBy: String? = null,
  @CreatedDate
  var creationDate: LocalDateTime? = null,
)
