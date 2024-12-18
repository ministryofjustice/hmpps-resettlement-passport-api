package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseNoteRetryEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime

class CaseNoteRetryRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var caseNoteRetryRepository: CaseNoteRetryRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Test
  fun `test findByNextRuntimeBeforeAndRetryCountLessThan`() {
    // Entries to be returned (i.e. nextRuntime is before "now" and retryCount < 10)
    val prisoner = getTestPrisonerEntity("A001ABC")
    val savedPrisoner = prisonerRepository.saveAndFlush(prisoner)

    val caseNotesEntity1 = CaseNoteRetryEntity(
      id = null,
      prisoner = savedPrisoner,
      type = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      notes = "some notes to be sent",
      author = "John Smith",
      prisonCode = "ABC",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 0,
      nextRuntime = LocalDateTime.parse("2024-07-01T08:12:23"),
    )
    val caseNotesEntity2 = CaseNoteRetryEntity(
      id = null,
      prisoner = savedPrisoner,
      type = DeliusCaseNoteType.PRE_RELEASE_REPORT,
      notes = "case notes text",
      author = "Jane Smith",
      prisonCode = "EFG",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 5,
      nextRuntime = LocalDateTime.parse("2024-07-02T10:10:56"),
    )
    val caseNotesEntity3 = CaseNoteRetryEntity(
      id = null,
      prisoner = savedPrisoner,
      type = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      notes = "case notes text here",
      author = "Alan Johnson",
      prisonCode = "ABC",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 9,
      nextRuntime = LocalDateTime.parse("2024-06-30T09:34:09"),
    )

    // Entries not to be returned (i.e. nextRuntime is after "now" and/or retryCount >= 10)
    val caseNotesEntity4 = CaseNoteRetryEntity(
      id = null,
      prisoner = savedPrisoner,
      type = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      notes = "some case note details",
      author = "Alan Johnson",
      prisonCode = "DEF",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 4,
      nextRuntime = LocalDateTime.parse("2024-07-02T16:00:00"),
    )

    val caseNotesEntity5 = CaseNoteRetryEntity(
      id = null,
      prisoner = savedPrisoner,
      type = DeliusCaseNoteType.PRE_RELEASE_REPORT,
      notes = "case notes",
      author = "Paula Smith",
      prisonCode = "ABC",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 10,
      nextRuntime = null,
    )

    caseNoteRetryRepository.saveAll(listOf(caseNotesEntity1, caseNotesEntity2, caseNotesEntity3, caseNotesEntity4, caseNotesEntity5))

    val expectedResults = listOf(caseNotesEntity1, caseNotesEntity2, caseNotesEntity3)
    val actualResults = caseNoteRetryRepository.findByNextRuntimeBeforeAndRetryCountLessThan(LocalDateTime.parse("2024-07-02T12:12:12"), 10)

    Assertions.assertEquals(expectedResults, actualResults)
  }

  private fun getTestPrisonerEntity(nomsId: String) = PrisonerEntity(null, nomsId, LocalDateTime.parse("2023-09-01T15:09:21"), "MDI")
}
