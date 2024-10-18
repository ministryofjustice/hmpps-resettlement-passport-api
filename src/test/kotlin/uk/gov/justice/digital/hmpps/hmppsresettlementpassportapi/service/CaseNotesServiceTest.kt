package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotePathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CaseNotesApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class CaseNotesServiceTest {
  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var caseNotesApiService: CaseNotesApiService

  @Mock
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @ParameterizedTest
  @MethodSource("test remove duplicates data")
  fun `test remove duplicates`(inputList: List<PathwayCaseNote>, expectedList: List<PathwayCaseNote>) {
    val caseNotesService = CaseNotesService(
      mockkClass(CaseNotesApiService::class),
      mockkClass(DeliusContactService::class),
      mockkClass(PrisonerRepository::class),
      mockkClass(ResettlementPassportDeliusApiService::class),
      mockkClass(ResettlementAssessmentRepository::class),
    )
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

  @ParameterizedTest
  @MethodSource("test get case notes data")
  fun `test get case notes`(caseNoteType: CaseNoteType, pathway: Pathway?, expectedList: List<PathwayCaseNote>) {
    val nomsId = "12345"
    val createdBy = 1
    val days = 100
    val prisoner = PrisonerEntity(1, nomsId, LocalDateTime.now(), null, null, LocalDate.parse("2025-01-23"))

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisoner)
    Mockito.`when`(caseNotesApiService.getCaseNotesByNomsId(nomsId, days, caseNoteType, createdBy)).thenReturn(emptyList())
    if (caseNoteType !== CaseNoteType.All) {
      Mockito.`when`(resettlementAssessmentRepository.findCaseNotesFor(1, pathway!!)).thenReturn(emptyList())

      Mockito.`when`(caseNotesApiService.getCaseNotesByNomsId(nomsId, days, CaseNoteType.All, createdBy))
        .thenReturn(expectedList)
    }

    val caseNotesService = CaseNotesService(
      caseNotesApiService,
      mockkClass(DeliusContactService::class),
      prisonerRepository,
      mockkClass(ResettlementPassportDeliusApiService::class),
      resettlementAssessmentRepository,
    )
    val size = expectedList.size

    Assertions.assertEquals(
      CaseNotesList(content = expectedList, pageSize = size, page = 0, sortName = "", totalElements = size, last = true),
      caseNotesService.getCaseNotesByNomsId(nomsId, 0, 1, "", days, caseNoteType, createdBy),
    )
  }

  private fun `test get case notes data`() = Stream.of(
    // All shouldn't try and get profile reset case note as it should already be returned in initial call
    Arguments.of(CaseNoteType.All, null, emptyList<PathwayCaseNote>()),
    // Profile reset note returned for each pathway
    Arguments.of(CaseNoteType.HEALTH, Pathway.HEALTH, generateProfileResetCaseNote()),
    Arguments.of(CaseNoteType.ACCOMMODATION, Pathway.ACCOMMODATION, generateProfileResetCaseNote()),
    Arguments.of(CaseNoteType.FINANCE_AND_ID, Pathway.FINANCE_AND_ID, generateProfileResetCaseNote()),
    Arguments.of(CaseNoteType.CHILDREN_FAMILIES_AND_COMMUNITY, Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, generateProfileResetCaseNote()),
    Arguments.of(CaseNoteType.ATTITUDES_THINKING_AND_BEHAVIOUR, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, generateProfileResetCaseNote()),
    Arguments.of(CaseNoteType.DRUGS_AND_ALCOHOL, Pathway.DRUGS_AND_ALCOHOL, generateProfileResetCaseNote()),
    Arguments.of(CaseNoteType.EDUCATION_SKILLS_AND_WORK, Pathway.EDUCATION_SKILLS_AND_WORK, generateProfileResetCaseNote()),
    // No profile reset notes returned
    Arguments.of(CaseNoteType.HEALTH, Pathway.HEALTH, emptyList<PathwayCaseNote>()),
  )

  @ParameterizedTest
  @MethodSource("test get case notes data - status update")
  fun `test get case notes - status update`(caseNoteType: CaseNoteType, pathway: Pathway?, pair: Pair<List<List<Any>>?, List<PathwayCaseNote>>) {
    val nomsId = "12345"
    val createdBy = 1
    val days = 100
    val prisoner = PrisonerEntity(1, nomsId, LocalDateTime.now(), null, null, LocalDate.parse("2025-01-23"))
    val (assessmentRepoReturn, expectedList) = pair

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisoner)
    Mockito.`when`(caseNotesApiService.getCaseNotesByNomsId(nomsId, days, caseNoteType, createdBy)).thenReturn(emptyList())
    if (caseNoteType !== CaseNoteType.All) {
      Mockito.`when`(resettlementAssessmentRepository.findCaseNotesFor(1, pathway!!)).thenReturn(assessmentRepoReturn)

      Mockito.`when`(caseNotesApiService.getCaseNotesByNomsId(nomsId, days, CaseNoteType.All, createdBy))
        .thenReturn(emptyList())
    }

    val caseNotesService = CaseNotesService(
      caseNotesApiService,
      mockkClass(DeliusContactService::class),
      prisonerRepository,
      mockkClass(ResettlementPassportDeliusApiService::class),
      resettlementAssessmentRepository,
    )
    val size = expectedList.size

    Assertions.assertEquals(
      CaseNotesList(content = expectedList, pageSize = size, page = 0, sortName = "", totalElements = size, last = true),
      caseNotesService.getCaseNotesByNomsId(nomsId, 0, 1, "", days, caseNoteType, createdBy),
    )
  }

  private fun `test get case notes data - status update`() = Stream.of(
    Arguments.of(CaseNoteType.All, null, Pair(null, emptyList<PathwayCaseNote>())),
    Arguments.of(CaseNoteType.HEALTH, Pathway.HEALTH, getCaseNotePair(CaseNotePathway.HEALTH, "Resettlement status set to: SUBMITTED")),
    Arguments.of(CaseNoteType.ACCOMMODATION, Pathway.ACCOMMODATION, getCaseNotePair(CaseNotePathway.ACCOMMODATION, "Resettlement status set to: SUBMITTED")),
    Arguments.of(CaseNoteType.FINANCE_AND_ID, Pathway.FINANCE_AND_ID, getCaseNotePair(CaseNotePathway.FINANCE_AND_ID, "Resettlement status set to: SUBMITTED")),
    Arguments.of(CaseNoteType.CHILDREN_FAMILIES_AND_COMMUNITY, Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, getCaseNotePair(CaseNotePathway.CHILDREN_FAMILIES_AND_COMMUNITY, "Resettlement status set to: SUBMITTED")),
    Arguments.of(CaseNoteType.ATTITUDES_THINKING_AND_BEHAVIOUR, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, getCaseNotePair(CaseNotePathway.ATTITUDES_THINKING_AND_BEHAVIOUR, "Resettlement status set to: SUBMITTED")),
    Arguments.of(CaseNoteType.DRUGS_AND_ALCOHOL, Pathway.DRUGS_AND_ALCOHOL, getCaseNotePair(CaseNotePathway.DRUGS_AND_ALCOHOL, "Resettlement status set to: SUBMITTED")),
    Arguments.of(CaseNoteType.EDUCATION_SKILLS_AND_WORK, Pathway.EDUCATION_SKILLS_AND_WORK, getCaseNotePair(CaseNotePathway.EDUCATION_SKILLS_AND_WORK, "Some actual case note text")),
  )

  private fun getCaseNotePair(pathway: CaseNotePathway, caseNoteText: String): Pair<List<List<Any>>, List<PathwayCaseNote>> {
    val id = "ResettlementAssessmentCaseNote1"
    val date = LocalDateTime.parse("2023-09-01T12:13:12")
    val createdBy = "User1"

    return Pair(
      listOf(listOf(id, Pathway.valueOf(pathway.name), date, date, createdBy, caseNoteText)),
      mutableListOf(
        PathwayCaseNote(
          caseNoteId = id,
          pathway = pathway,
          creationDateTime = date,
          occurenceDateTime = date,
          createdBy = createdBy,
          text = caseNoteText,
        ),
      ),
    )
  }

  private fun generateProfileResetCaseNote() =
    mutableListOf(
      PathwayCaseNote(
        caseNoteId = "caseNoteId",
        pathway = CaseNotePathway.OTHER,
        creationDateTime = LocalDateTime.parse("2023-09-01T12:13:12"),
        occurenceDateTime = LocalDateTime.parse("2023-09-01T12:13:12"),
        createdBy = "user1",
        text = PROFILE_RESET_TEXT_PREFIX + "some reason" + PROFILE_RESET_TEXT_SUFFIX + PROFILE_RESET_TEXT_SUPPORT,
      ),
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
