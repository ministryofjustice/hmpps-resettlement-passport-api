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
@Table(name = "delius_contact")
data class DeliusContactEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "prisoner_id", referencedColumnName = "id")
  val prisoner: PrisonerEntity,

  val category: Category,

  @Column(name = "contact_type")
  val contactType: ContactType,

  @Column(name = "created_date")
  val createdDate: LocalDateTime,

  @Column(name = "appointment_date")
  val appointmentDate: LocalDateTime? = null,

  @Column(name = "appointment_duration")
  val appointmentDuration: Int? = null, // in minutes

  @Column(name = "notes")
  val notes: String,

  @Column(name = "created_by")
  val createdBy: String,

)

enum class Category {
  ACCOMMODATION,
  ATTITUDES_THINKING_AND_BEHAVIOUR,
  CHILDREN_FAMILIES_AND_COMMUNITY,
  DRUGS_AND_ALCOHOL,
  EDUCATION_SKILLS_AND_WORK,
  FINANCE_AND_ID,
  HEALTH,
  BENEFITS,
  ;

  companion object {
    fun convertPathwayToCategory(pathway: Pathway): Category {
      return when (pathway) {
        Pathway.ACCOMMODATION -> ACCOMMODATION
        Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR -> ATTITUDES_THINKING_AND_BEHAVIOUR
        Pathway.CHILDREN_FAMILIES_AND_COMMUNITY -> CHILDREN_FAMILIES_AND_COMMUNITY
        Pathway.DRUGS_AND_ALCOHOL -> DRUGS_AND_ALCOHOL
        Pathway.EDUCATION_SKILLS_AND_WORK -> EDUCATION_SKILLS_AND_WORK
        Pathway.FINANCE_AND_ID -> FINANCE_AND_ID
        Pathway.HEALTH -> HEALTH
      }
    }
  }
}

enum class ContactType {
  CASE_NOTE,
  APPOINTMENT,
}
