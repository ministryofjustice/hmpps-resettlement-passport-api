package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "todo_item")
data class TodoEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val prisonerId: Long,
  val task: String,
  val notes: String? = null,
  val dueDate: LocalDate? = null,
  val completed: Boolean = false,
  val createdByUrn: String,
  val updatedByUrn: String = createdByUrn,
  val creationDate: LocalDateTime = LocalDateTime.now(),
  val updatedAt: LocalDateTime = LocalDateTime.now(),
)
