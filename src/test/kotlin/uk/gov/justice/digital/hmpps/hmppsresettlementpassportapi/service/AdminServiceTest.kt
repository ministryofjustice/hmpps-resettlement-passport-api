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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseNoteRetryEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseNoteRetryRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseNoteRetryService.Companion.MAX_RETRIES
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class AdminServiceTest {
  private lateinit var adminService: AdminService

  @Mock
  private lateinit var caseNoteRetryRepository: CaseNoteRetryRepository

  @Mock
  private lateinit var caseNoteRetryService: CaseNoteRetryService

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
    adminService = AdminService(caseNoteRetryService, caseNoteRetryRepository)
  }

  @Test
  fun `test retryFailedDeliusCaseNotes`() {
    val caseNotesToRetry = listOf(Mockito.mock(CaseNoteRetryEntity::class.java), Mockito.mock(CaseNoteRetryEntity::class.java), Mockito.mock(CaseNoteRetryEntity::class.java))
    Mockito.`when`(caseNoteRetryRepository.findByNextRuntimeBeforeAndRetryCountLessThan(fakeNow, MAX_RETRIES)).thenReturn(caseNotesToRetry)

    adminService.retryFailedDeliusCaseNotes()

    caseNotesToRetry.forEach {
      Mockito.verify(caseNoteRetryService).processDeliusCaseNote(it)
    }
  }
}
