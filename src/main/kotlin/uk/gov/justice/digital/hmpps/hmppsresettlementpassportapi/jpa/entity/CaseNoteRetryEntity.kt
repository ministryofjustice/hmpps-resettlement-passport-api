package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import java.time.LocalDateTime

@Entity
@Table(name = "case_note_retry")
data class CaseNoteRetryEntity (
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "prisoner_id", referencedColumnName = "id")
  val prisoner: PrisonerEntity,

  @Enumerated(EnumType.STRING)
  val type: DeliusCaseNoteType,

  val notes: String,

  val author: String,

  val prisonCode: String,

  @Column(name = "original_submission_date")
  val originalSubmissionDate: LocalDateTime,

  @Column(name = "retry_count")
  val retryCount: Int,

  @Column(name = "next_runtime")
  val nextRuntime: LocalDateTime,

)