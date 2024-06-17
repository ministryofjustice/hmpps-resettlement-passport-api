package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotePathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CaseNotesApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDateTime
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CaseNotesServiceTest {

  @ParameterizedTest
  @MethodSource("test remove duplicates data")
  fun `test remove duplicates`(inputList: List<PathwayCaseNote>, expectedList: List<PathwayCaseNote>) {
    val caseNotesService = CaseNotesService(mockkClass(CaseNotesApiService::class), mockkClass(DeliusContactService::class), mockkClass(PrisonerRepository::class), mockkClass(ResettlementPassportDeliusApiService::class))
    Assertions.assertEquals(expectedList, caseNotesService.removeDuplicates(inputList))
  }

  private fun `test remove duplicates data`() = Stream.of(
    Arguments.of(emptyList<PathwayCaseNote>(), emptyList<PathwayCaseNote>()),
    // Test where all element are unique
    Arguments.of(generatePathwayCaseNotes(times = 1), generatePathwayCaseNotes(times = 1)),
    // Test where there are 3 copies of each element
    Arguments.of(generatePathwayCaseNotes(times = 3), generatePathwayCaseNotes(times = 1)),
    // Test where the id is different but the rest is the same
    Arguments.of(generatePathwayCaseNotesSameId(number = 100), generatePathwayCaseNotesSameId(number = 1)),
    // Test where the creationDate is in the same Date but different time
    Arguments.of(generatePathwayCaseNotesSameCreationDate(number = 100), generatePathwayCaseNotesSameCreationDate(number = 1)),
    // Test where the occurrenceDate is in the same Date but different time
    Arguments.of(generatePathwayCaseNotesSameOccurrenceDate(number = 100), generatePathwayCaseNotesSameOccurrenceDate(number = 1)),
  )

  private fun generatePathwayCaseNote(seed: Long, createdBy: String, text: String, creationDateTime: LocalDateTime, occurrenceDateTime: LocalDateTime, pathway: CaseNotePathway) =
    PathwayCaseNote(
      caseNoteId = seed.toString(),
      pathway = pathway,
      creationDateTime = creationDateTime,
      occurenceDateTime = occurrenceDateTime,
      createdBy = createdBy,
      text = text,
    )

  private fun generatePathwayCaseNotes(number: Long = 10000, times: Int): List<PathwayCaseNote> {
    val list = mutableListOf<PathwayCaseNote>()
    for (i in 1..number) {
      repeat(times) {
        list.add(
          generatePathwayCaseNote(
            seed = i,
            createdBy = "createdBy$i",
            text = "text$i",
            creationDateTime = LocalDateTime.parse("2023-09-01T12:13:12").plusDays(i),
            occurrenceDateTime = LocalDateTime.parse("2023-08-12T23:01:09").plusDays(i),
            pathway = CaseNotePathway.entries[(i % CaseNotePathway.entries.size).toInt()],
          ),
        )
      }
    }
    return list
  }

  private fun generatePathwayCaseNotesSameId(number: Long = 100): List<PathwayCaseNote> {
    val list = mutableListOf<PathwayCaseNote>()
    for (i in 1..number) {
      list.add(
        generatePathwayCaseNote(
          seed = i,
          createdBy = "createdBy",
          text = "text",
          creationDateTime = LocalDateTime.parse("2023-09-01T12:13:12"),
          occurrenceDateTime = LocalDateTime.parse("2023-08-12T23:01:09"),
          pathway = CaseNotePathway.OTHER,
        ),
      )
    }
    return list
  }

  private fun generatePathwayCaseNotesSameCreationDate(number: Long = 100): List<PathwayCaseNote> {
    val list = mutableListOf<PathwayCaseNote>()
    for (i in 1..number) {
      list.add(
        generatePathwayCaseNote(
          seed = i,
          createdBy = "createdBy",
          text = "text",
          creationDateTime = LocalDateTime.parse("2023-09-01T00:00:00").plusSeconds(i),
          occurrenceDateTime = LocalDateTime.parse("2023-08-12T00:00:00"),
          pathway = CaseNotePathway.OTHER,
        ),
      )
    }
    return list
  }

  private fun generatePathwayCaseNotesSameOccurrenceDate(number: Long = 100): List<PathwayCaseNote> {
    val list = mutableListOf<PathwayCaseNote>()
    for (i in 1..number) {
      list.add(
        generatePathwayCaseNote(
          seed = i,
          createdBy = "createdBy",
          text = "text",
          creationDateTime = LocalDateTime.parse("2023-09-01T00:00:00"),
          occurrenceDateTime = LocalDateTime.parse("2023-08-12T00:00:00").plusSeconds(i),
          pathway = CaseNotePathway.OTHER,
        ),
      )
    }
    return list
  }
}
