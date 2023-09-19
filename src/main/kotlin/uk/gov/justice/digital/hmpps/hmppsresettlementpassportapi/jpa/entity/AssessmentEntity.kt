package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "assessment")
data class AssessmentEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "prisoner_id", referencedColumnName = "id")
  val prisoner: PrisonerEntity,

  @Column(name = "when_created")
  val creationDate: LocalDateTime,

  @Column(name = "assessment_date")
  val assessmentDate: LocalDateTime,

  @Column(name = "is_bank_account_required")
  var isBankAccountRequired: Boolean,

  @Column(name = "is_id_required")
  var isIdRequired: Boolean,

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "assessment_id_type",
    joinColumns = [JoinColumn(name = "assessment_id")],
    inverseJoinColumns = [JoinColumn(name = "id_type_id")],
  )
  var idDocuments: Set<IdTypeEntity>,

  @Column(name = "is_deleted")
  var isDeleted: Boolean = false,

  @Column(name = "deleted_at")
  var deletionDate: LocalDateTime?,
)
