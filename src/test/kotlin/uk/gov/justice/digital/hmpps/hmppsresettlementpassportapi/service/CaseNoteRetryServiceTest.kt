package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseNoteRetryEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseNoteRetryRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class CaseNoteRetryServiceTest {

  private lateinit var caseNoteRetryService: CaseNoteRetryService

  @Mock
  private lateinit var caseNoteRetryRepository: CaseNoteRetryRepository

  @Mock
  private lateinit var caseNotesService: CaseNotesService

  @Mock
  private lateinit var resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService

  @Mock
  private lateinit var metricsService: MetricsService

  companion object {

    private val fakeNow = LocalDateTime.parse("2024-07-02T12:12:12")

    @JvmStatic
    @BeforeAll
    fun beforeAll() {
      mockkStatic(LocalDateTime::class)
      every { LocalDateTime.now() } returns fakeNow
    }

    @JvmStatic
    @AfterAll
    fun afterAll() {
      unmockkAll()
    }
  }

  @BeforeEach
  fun beforeEach() {
    caseNoteRetryService = CaseNoteRetryService(caseNoteRetryRepository, caseNotesService, resettlementPassportDeliusApiService, metricsService)
  }

  @Test
  fun `test processDeliusCaseNote`() {
    // Entry to be retried for first time
    val caseNotesEntity1 = CaseNoteRetryEntity(
      id = 1,
      prisoner = getTestPrisonerEntity("A001ABC"),
      type = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      notes = "some notes to be sent",
      author = "John Smith",
      prisonCode = "ABC",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 0,
      nextRuntime = LocalDateTime.parse("2024-07-01T08:12:23"),
    )

    // Entry to be retried for fifth time
    val caseNotesEntity2 = CaseNoteRetryEntity(
      id = 2,
      prisoner = getTestPrisonerEntity("A002ABC"),
      type = DeliusCaseNoteType.PRE_RELEASE_REPORT,
      notes = "case notes text",
      author = "Jane Smith",
      prisonCode = "EFG",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 5,
      nextRuntime = LocalDateTime.parse("2024-07-02T10:10:56"),
    )

    // Entry failed due to missing crn and more retries available
    val caseNotesEntity3 = CaseNoteRetryEntity(
      id = 3,
      prisoner = getTestPrisonerEntity("A003ABC"),
      type = DeliusCaseNoteType.PRE_RELEASE_REPORT,
      notes = "the case note",
      author = "John Williams",
      prisonCode = "DEF",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 6,
      nextRuntime = LocalDateTime.parse("2024-06-30T23:12:51"),
    )

    // Entry failed due to error from Delius API and give up retry
    val caseNotesEntity4 = CaseNoteRetryEntity(
      id = 4,
      prisoner = getTestPrisonerEntity("A004ABC"),
      type = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      notes = "case notes text here",
      author = "Alan Johnson",
      prisonCode = "ABC",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 9,
      nextRuntime = LocalDateTime.parse("2024-06-30T09:34:09"),
    )

//    Mockito.`when`(caseNoteRetryRepository.findByNextRuntimeBeforeAndRetryCountLessThan(fakeNow, 10)).thenReturn(
//      listOf(caseNotesEntity1, caseNotesEntity2, caseNotesEntity3, caseNotesEntity4),
//    )

    // Return CRN as per each test case
    Mockito.`when`(resettlementPassportDeliusApiService.getCrn("A001ABC")).thenReturn("A000001")
    Mockito.`when`(resettlementPassportDeliusApiService.getCrn("A002ABC")).thenReturn("A000002")
    Mockito.`when`(resettlementPassportDeliusApiService.getCrn("A003ABC")).thenReturn(null)
    Mockito.`when`(resettlementPassportDeliusApiService.getCrn("A004ABC")).thenReturn("A000004")

    // Success or fail on posting case notes to Delius as per each test case
    Mockito.`when`(
      caseNotesService.postBCSTCaseNoteToDelius(
        crn = "A000001",
        prisonCode = "ABC",
        notes = "some notes to be sent",
        name = "John Smith",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
        description = null,
        // TODO: this needs to be added into the database
      ),
    ).thenReturn(true)
    Mockito.`when`(
      caseNotesService.postBCSTCaseNoteToDelius(
        crn = "A000002",
        prisonCode = "EFG",
        notes = "case notes text",
        name = "Jane Smith",
        deliusCaseNoteType = DeliusCaseNoteType.PRE_RELEASE_REPORT,
        description = null,
        // TODO: this needs to be added into the database
      ),
    ).thenReturn(true)
    Mockito.`when`(
      caseNotesService.postBCSTCaseNoteToDelius(
        crn = "A000004",
        prisonCode = "ABC",
        notes = "case notes text here",
        name = "Alan Johnson",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
        description = null,
        // TODO: this needs to be added into the database
      ),
    ).thenReturn(false)

    // Run test
    caseNoteRetryService.processDeliusCaseNote(caseNotesEntity1)
    caseNoteRetryService.processDeliusCaseNote(caseNotesEntity2)
    caseNoteRetryService.processDeliusCaseNote(caseNotesEntity3)
    caseNoteRetryService.processDeliusCaseNote(caseNotesEntity4)

    // Verify the correct records are deleted
    Mockito.verify(caseNoteRetryRepository).delete(caseNotesEntity1)
    Mockito.verify(caseNoteRetryRepository).delete(caseNotesEntity2)

    // Verify the correct records are updated and saved
    val caseNotesEntity3Updated = CaseNoteRetryEntity(
      id = 3,
      prisoner = getTestPrisonerEntity("A003ABC"),
      type = DeliusCaseNoteType.PRE_RELEASE_REPORT,
      notes = "the case note",
      author = "John Williams",
      prisonCode = "DEF",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 7,
      nextRuntime = LocalDateTime.parse("2024-07-03T01:12:12"),
    )
    val caseNotesEntity4Updated = CaseNoteRetryEntity(
      id = 4,
      prisoner = getTestPrisonerEntity("A004ABC"),
      type = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      notes = "case notes text here",
      author = "Alan Johnson",
      prisonCode = "ABC",
      originalSubmissionDate = LocalDateTime.parse("2024-07-01T08:12:23"),
      retryCount = 10,
      nextRuntime = null,
    )
    Mockito.verify(caseNoteRetryRepository).save(caseNotesEntity3Updated)
    Mockito.verify(caseNoteRetryRepository).save(caseNotesEntity4Updated)

    // Verify the metrics are updated
    Mockito.verify(metricsService).incrementCounter("delius_case_note_retry_give_up")

    Mockito.verifyNoMoreInteractions(caseNotesService, caseNoteRetryRepository, metricsService, resettlementPassportDeliusApiService)
  }

  private fun getTestPrisonerEntity(nomsId: String) = PrisonerEntity(1, nomsId, LocalDateTime.parse("2023-09-01T15:09:21"), "D567890", "MDI", LocalDate.parse("2027-01-01"))
}
