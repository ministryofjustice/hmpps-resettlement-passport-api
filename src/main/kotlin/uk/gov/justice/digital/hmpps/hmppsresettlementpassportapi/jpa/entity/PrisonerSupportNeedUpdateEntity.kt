package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import java.time.LocalDateTime

@Entity
@Table(name = "prisoner_support_need_update")
data class PrisonerSupportNeedUpdateEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "prisoner_support_need_id")
  val prisonerSupportNeedId: Long,

  @Column(name = "created_by")
  val createdBy: String,

  @Column(name = "created_date")
  val createdDate: LocalDateTime,

  @Column(name = "update_text")
  val updateText: String?,

  @Enumerated(EnumType.STRING)
  val status: SupportNeedStatus,

  @Column(name = "is_prison")
  val isPrison: Boolean,

  @Column(name = "is_probation")
  val isProbation: Boolean,

  @Column(name = "is_deleted")
  val isDeleted: Boolean = false,

  @Column(name = "deleted_date")
  val deletedDate: LocalDateTime? = null,
)
