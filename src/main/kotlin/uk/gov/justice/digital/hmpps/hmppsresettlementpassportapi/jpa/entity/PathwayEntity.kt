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

enum class Pathway(val id: Long) {
  ACCOMMODATION(1),
  ATTITUDES_THINKING_AND_BEHAVIOUR(2),
  CHILDREN_FAMILIES_AND_COMMUNITY(3),
  DRUGS_AND_ALCOHOL(4),
  EDUCATION_SKILLS_AND_WORK(5),
  FINANCE_AND_ID(6),
  HEALTH(7),
}
