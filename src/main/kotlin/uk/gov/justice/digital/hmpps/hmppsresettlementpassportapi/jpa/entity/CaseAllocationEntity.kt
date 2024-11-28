package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "case_allocation")
data class CaseAllocationEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "prisoner_id")
  val prisonerId: Long,

  @Column(name = "staff_id")
  val staffId: Int? = null,

  @Column(name = "staff_firstname")
  val staffFirstname: String,

  @Column(name = "staff_lastname")
  val staffLastname: String,

  @Column(name = "is_deleted")
  var isDeleted: Boolean = false,

  @Column(name = "when_created")
  val creationDate: LocalDateTime = LocalDateTime.now(),

  @Column(name = "deleted_at")
  var deletionDate: LocalDateTime? = null,

  @Transient
  var nomsId: String? = null,

)
