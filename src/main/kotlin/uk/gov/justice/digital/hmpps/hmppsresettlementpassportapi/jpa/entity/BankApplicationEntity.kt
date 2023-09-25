package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "bank_application")
data class BankApplicationEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "prisoner_id", referencedColumnName = "id")
  val prisoner: PrisonerEntity,

  @OneToMany(mappedBy = "bankApplication")
  var logs: Set<BankApplicationStatusLogEntity>,

  @Column(name = "when_created")
  val creationDate: LocalDateTime,

  @Column(name = "application_submitted_date")
  val applicationSubmittedDate: LocalDateTime,

  @Column(name = "bank_response_date")
  var bankResponseDate: LocalDateTime? = null,

  @Column(name = "status")
  var status: String,

  @Column(name = "added_to_personal_items")
  var isAddedToPersonalItems: Boolean? = null,

  @Column(name = "added_to_personal_items_date")
  var addedToPersonalItemsDate: LocalDateTime? = null,

  @Column(name = "is_deleted")
  var isDeleted: Boolean = false,

  @Column(name = "deleted_at")
  var deletionDate: LocalDateTime? = null,
)
