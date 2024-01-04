package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "resettlement_assessment")
data class ResettlementAssessmentEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "prisoner_id", referencedColumnName = "id")
  val prisoner: PrisonerEntity,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "pathway_id", referencedColumnName = "id")
  val pathway: PathwayEntity,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "status_changed_to_status_id", referencedColumnName = "id")
  val statusChangedTo: StatusEntity?,

  @Column(name = "assessment_type")
  @Enumerated(EnumType.STRING)
  val assessmentType: ResettlementAssessmentType,

  @Column(name = "assessment")
  @JdbcTypeCode(SqlTypes.JSON)
  val assessment: String,

  @Column(name = "created_date")
  val creationDate: LocalDateTime,

  @Column(name = "created_by")
  val createdBy: String,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "assessment_status_id", referencedColumnName = "id")
  val assessmentStatus: ResettlementAssessmentStatusEntity,

)

enum class ResettlementAssessmentType {
  BCST2,
  RESETTLEMENT_PLAN,
}
