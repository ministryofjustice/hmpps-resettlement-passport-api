package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime
import java.util.*

class DocumentsRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var documentsRepository: DocumentsRepository

  @Test
  fun `test findAllByPrisonerIdAndCreationDateBetween query`() {
    // Seed database with prisoners and documents
    val prisoner1 = prisonerRepository.save(PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))
    val prisoner2 = prisonerRepository.save(PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))

    val searchDate = LocalDateTime.now()

    val todoItems = listOf(
      DocumentsEntity(
        id = null,
        prisonerId = prisoner2.id(),
        originalDocumentKey = UUID.randomUUID(),
        pdfDocumentKey = UUID.randomUUID(),
        creationDate = searchDate.minusDays(10),
        category = DocumentCategory.LICENCE_CONDITIONS,
        originalDocumentFileName = "license1.pdf",
        isDeleted = true,
        deletionDate = searchDate.minusDays(1),
      ),
      DocumentsEntity(
        id = null,
        prisonerId = prisoner2.id(),
        originalDocumentKey = UUID.randomUUID(),
        pdfDocumentKey = UUID.randomUUID(),
        creationDate = searchDate.minusDays(5),
        category = DocumentCategory.LICENCE_CONDITIONS,
        originalDocumentFileName = "license2.pdf",
        isDeleted = true,
        deletionDate = searchDate.minusDays(1),
      ),
      DocumentsEntity(
        id = null,
        prisonerId = prisoner2.id(),
        originalDocumentKey = UUID.randomUUID(),
        pdfDocumentKey = UUID.randomUUID(),
        creationDate = searchDate,
        category = DocumentCategory.LICENCE_CONDITIONS,
        originalDocumentFileName = "license3.pdf",
        isDeleted = false,
        deletionDate = null,
      ),
    )

    documentsRepository.saveAll(todoItems)

    // Prisoner 1 has documents
    Assertions.assertThat(documentsRepository.findAllByPrisonerIdAndCreationDateBetween(prisoner1.id(), searchDate.minusDays(7), searchDate.plusDays(1))).isEmpty()

    // Prisoner 2 has three - one is out of search range
    Assertions.assertThat(documentsRepository.findAllByPrisonerIdAndCreationDateBetween(prisoner2.id(), searchDate.minusDays(7), searchDate.plusDays(1))).isEqualTo(todoItems.filter { it.creationDate > searchDate.minusDays(7) })
  }
}
