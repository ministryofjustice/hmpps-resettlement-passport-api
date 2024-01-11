package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Assessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.*
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.IdTypeRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ResettlementAssessmentServiceTest {
  private lateinit var resettlementAssessmentService: ResettlementAssessmentService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var pathwayRepository: PathwayRepository

  @Mock
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Mock
  private lateinit var idTypeRepository: IdTypeRepository
  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentService = ResettlementAssessmentService(resettlementAssessmentRepository, prisonerRepository, pathwayRepository)
  }

  @Test
  fun `test getResettlementAssessmentSummaryByNomsId - returns assessment`() {
    val nomsId: String = "GY3245"
    val assessmentType = ResettlementAssessmentType.BCST2
    val prisonerEntity = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23"))
    val accommodationPathwayEntity = PathwayEntity(1, "Accommodation", true, fakeNow)
    val accommodationResettlementAssessmentEntity = createResettlementAssessmentEntity(1, "Accommodation")
    val

    val prisonerResettlementAssessmentSummary = [
      PrisonerResettlementAssessment(Pathway.ACCOMMODATION, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.HEALTH, ResettlementAssessmentStatus.NOT_STARTED),

    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, accommodationPathwayEntity, assessmentType)).thenReturn(accommodationResettlementAssessmentEntity))

    val response = resettlementAssessmentService.getResettlementAssessmentSummaryByNomsId(nomsId)
    Assertions.assertEquals(prisonerResettlementAssessmentSummary, response)



  }

  private fun createResettlementAssessmentEntity(id: Long, name: String) = ResettlementAssessmentEntity(
    id= id,
    prisoner = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23")),
    pathway = PathwayEntity(id = id, name = name, active=true, fakeNow),
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatusEntity(id = 1, name = "Not Started", true, fakeNow),
    assessment = "assessment",
    creationDate = fakeNow,
    createdBy = "PO",
    statusChangedTo = null )

}
