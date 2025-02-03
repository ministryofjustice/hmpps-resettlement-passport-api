package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import java.time.LocalDateTime

@Entity
@Table(name = "support_need")
data class SupportNeedEntity(
  @Id
  val id: Long,

  @Enumerated(EnumType.STRING)
  val pathway: Pathway,

  val section: String?,

  val title: String,

  val hidden: Boolean,

  @Column(name = "exclude_from_count")
  val excludeFromCount: Boolean,

  @Column(name = "allow_other_detail")
  val allowOtherDetail: Boolean,

  @Column(name = "created_date")
  val createdDate: LocalDateTime,

  @Column(name = "is_deleted")
  val deleted: Boolean = false,

  @Column(name = "deleted_date")
  val deletedDate: LocalDateTime? = null,
)
