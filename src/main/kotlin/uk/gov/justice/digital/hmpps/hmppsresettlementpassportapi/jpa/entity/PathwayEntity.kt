package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "pathway")
data class PathwayEntity(
  @Id
  val id: Long,

  @Column
  val name: String,

  @Column
  val active: Boolean,

  @Column(name = "creation_date")
  val creationDate: LocalDateTime,
)

enum class Pathway(val id: Long, val displayName: String) {
  ACCOMMODATION(1, "Accommodation"),
  ATTITUDES_THINKING_AND_BEHAVIOUR(2, "Attitudes, thinking and behaviour"),
  CHILDREN_FAMILIES_AND_COMMUNITY(3, "Children, families and communities"),
  DRUGS_AND_ALCOHOL(4, "Drugs and alcohol"),
  EDUCATION_SKILLS_AND_WORK(5, "Education, skills and work"),
  FINANCE_AND_ID(6, "Finance and ID"),
  HEALTH(7, "Health"),
  ;

  companion object {
    fun getById(id: Long) = entries.first { it.id == id }

    fun getAllPathways() = entries.sortedBy { it.id }.toSet()
  }
}
