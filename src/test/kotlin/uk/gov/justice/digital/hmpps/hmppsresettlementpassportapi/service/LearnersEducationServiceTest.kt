package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.curiousapi.LearnersEducationList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CuriousApiService
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class LearnersEducationServiceTest {
  private lateinit var learnersEducationService: LearnersEducationService

  @Mock
  private lateinit var curiousApiService: CuriousApiService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    learnersEducationService =
      LearnersEducationService(curiousApiService, prisonerRepository)
  }

  @Test
  fun `test get Course happy path full json`() {
    val prisonerEntity = PrisonerEntity(1, "A8339DY", testDate, "xyz")
    val prisonerId = "A8339DY"
    val expectedCoursename = "Dummy_Automated_320"

    val mockedJsonResponse: LearnersEducationList = readFileAsObject("testdata/curious-api/learners-education-list.json")
    whenever(curiousApiService.getLearnersEducation(prisonerId, 1, 0)).thenReturn(mockedJsonResponse)
    whenever(prisonerRepository.findByNomsId(prisonerId)).thenReturn(prisonerEntity)
    val learnersCourseList =
      learnersEducationService.getLearnersEducationCourseData(
        prisonerId,
        0,
        1,

      )
    Assertions.assertEquals(expectedCoursename, learnersCourseList.content?.get(0)?.courseName ?: 0)
  }

  private inline fun <reified T> readFileAsObject(filename: String): T = readStringAsObject(readFile(filename))

  private inline fun <reified T> readStringAsObject(string: String): T = jacksonObjectMapper().configure(
    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
    false,
  ).registerKotlinModule().registerModule(JavaTimeModule()).readValue(string)
}
