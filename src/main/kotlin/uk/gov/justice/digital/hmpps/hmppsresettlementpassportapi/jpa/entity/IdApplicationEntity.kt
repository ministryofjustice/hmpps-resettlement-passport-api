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
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "id_application")
data class IdApplicationEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "prisoner_id")
  val prisonerId: Long,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "id_type_id", referencedColumnName = "id")
  val idType: IdTypeEntity,

  @Column(name = "when_created")
  val creationDate: LocalDateTime = LocalDateTime.now(),

  @Column(name = "application_submitted_date")
  val applicationSubmittedDate: LocalDateTime,

  @Column(name = "priority_application")
  var isPriorityApplication: Boolean,

  @Column(name = "cost_of_application")
  var costOfApplication: BigDecimal,

  @Column(name = "refund_amount")
  var refundAmount: BigDecimal? = null,

  @Column(name = "have_gro")
  var haveGro: Boolean? = null,

  @Column(name = "uk_national_born_overseas")
  var isUkNationalBornOverseas: Boolean? = null,

  @Column(name = "country_born_in")
  var countryBornIn: String? = null,

  @Column(name = "case_number")
  var caseNumber: String? = null,

  @Column(name = "court_details")
  var courtDetails: String? = null,

  @Column(name = "drivers_licence_type")
  var driversLicenceType: String? = null,

  @Column(name = "drivers_licence_application_made_at")
  var driversLicenceApplicationMadeAt: String? = null,

  @Column(name = "added_to_personal_items")
  var isAddedToPersonalItems: Boolean? = null,

  @Column(name = "added_to_personal_items_date")
  var addedToPersonalItemsDate: LocalDateTime? = null,

  @Column(name = "status")
  var status: String = "pending",

  @Column(name = "status_update_date")
  var statusUpdateDate: LocalDateTime? = null,

  @Column(name = "is_deleted")
  var isDeleted: Boolean = false,

  @Column(name = "deleted_at")
  var deletionDate: LocalDateTime? = null,

  @Column(name = "date_id_received")
  var dateIdReceived: LocalDateTime? = null,
)
