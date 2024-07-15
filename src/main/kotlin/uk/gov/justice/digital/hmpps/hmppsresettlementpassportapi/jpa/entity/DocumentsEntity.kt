package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "document_location")
class DocumentsEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val prisonerId: Long,

  @Column(name = "original_document_key")
  val originalDocumentKey: String,

  @Column(name = "html_document_key")
  val htmlDocumentKey: UUID?,

  @Column(name = "creation_date")
  val creationDate: LocalDateTime,
)
