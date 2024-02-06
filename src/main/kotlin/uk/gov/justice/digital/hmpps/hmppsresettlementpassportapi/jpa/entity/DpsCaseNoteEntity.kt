package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "dps_case_note")
data class DpsCaseNoteEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "prisoner_id", referencedColumnName = "id")
  val prisoner: PrisonerEntity,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "pathway_id", referencedColumnName = "id")
  val pathway: PathwayEntity,

  @Column(name = "created_date")
  val createdDate: LocalDateTime,

  @Column(name = "notes")
  val notes: String,

  @Column(name = "created_by")
  val createdBy: String,
)
