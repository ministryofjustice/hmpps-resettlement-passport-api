package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DpsCaseNoteEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDate
import java.time.LocalDateTime

class DpsCaseNoteRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var dpsCaseNoteRepository: DpsCaseNoteRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var pathwayRepository: PathwayRepository

  @BeforeEach
  @AfterEach
  fun beforeEach() {
    dpsCaseNoteRepository.deleteAll()
    prisonerRepository.deleteAll()
  }

  @Test
  fun `test persist new dps case note`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "crn1", "xyz1", LocalDate.parse("2025-01-23"))
    prisonerRepository.save(prisoner)

    val pathway = pathwayRepository.getReferenceById(Pathway.ACCOMMODATION.id)

    val dpsCaseNote = DpsCaseNoteEntity(
      id = null,
      prisoner = prisoner,
      pathway = pathway,
      createdDate = LocalDateTime.parse("2023-01-01T12:00:00"),
      notes = "some case notes here",
      createdBy = "System User",
    )

    dpsCaseNoteRepository.save(dpsCaseNote)

    val dpsCaseNotesFromDatabase = dpsCaseNoteRepository.findAll()
    assertThat(dpsCaseNotesFromDatabase).usingRecursiveComparison().isEqualTo(listOf(dpsCaseNote))
  }
}
