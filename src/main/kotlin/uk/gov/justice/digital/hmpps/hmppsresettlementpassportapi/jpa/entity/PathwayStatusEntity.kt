package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import java.time.LocalDateTime

@Entity
@Table(name = "pathway_status")
data class PathwayStatusEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val prisonerId: Long,

  @Enumerated(EnumType.STRING)
  val pathway: Pathway,

  @Enumerated(EnumType.STRING)
  var status: Status,

  @Column(name = "updated_date")
  var updatedDate: LocalDateTime? = null,
)
