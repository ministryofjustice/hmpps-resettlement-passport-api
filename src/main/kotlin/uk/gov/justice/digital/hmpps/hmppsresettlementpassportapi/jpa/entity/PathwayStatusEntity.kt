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
import java.util.*

@Entity
@Table(name = "pathway_status")
data class PathwayStatusEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "prisoner_id", referencedColumnName = "id")
  val prisoner: PrisonerEntity,

  @ManyToOne
  @JoinColumn(name = "pathway_id", referencedColumnName = "id")
  val pathway: PathwayEntity,

  @ManyToOne
  @JoinColumn(name = "status_id", referencedColumnName = "id")
  var status: StatusEntity,

  @Column(name = "creation_date")
  var creationDate: LocalDateTime,
)
