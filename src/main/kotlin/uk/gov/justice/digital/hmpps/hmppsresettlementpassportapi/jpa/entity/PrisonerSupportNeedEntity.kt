package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

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
@Table(name = "prisoner_support_need")
data class PrisonerSupportNeedEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "prisoner_id")
  val prisonerId: Long,

  @ManyToOne
  @JoinColumn(name = "support_need_id", referencedColumnName = "id", updatable = false)
  val supportNeed: SupportNeedEntity,

  @Column(name = "other_detail")
  val otherDetail: String?,

  @Column(name = "created_by")
  val createdBy: String,

  @Column(name = "created_date")
  val createdDate: LocalDateTime,

  @Column(name = "is_deleted")
  var deleted: Boolean = false,

  @Column(name = "deleted_date")
  var deletedDate: LocalDateTime? = null,

  @Column(name = "latest_update_id")
  var latestUpdateId: Long? = null,
)
