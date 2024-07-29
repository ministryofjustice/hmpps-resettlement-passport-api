package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DocumentCategory
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
  val originalDocumentKey: UUID?,

  @Column(name = "pdf_document_key")
  val pdfDocumentKey: UUID?,

  @Column(name = "creation_date")
  val creationDate: LocalDateTime,

  @Enumerated(EnumType.STRING)
  val category: DocumentCategory,

  @Column(name = "original_document_file_name")
  val originalDocumentFileName: String,
)
