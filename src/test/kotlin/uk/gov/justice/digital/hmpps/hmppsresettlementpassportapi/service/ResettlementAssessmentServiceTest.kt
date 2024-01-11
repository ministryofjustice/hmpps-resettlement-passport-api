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

//  @Test
//  fun `test getResettlementAssessmentSummaryByNomsId - returns assessment`() {
//    val nomsId: String = "GY3245"
//    val prisonerEntity = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23"))
//    val pathwayEntity = PathwayEntity()
//    val prisonerResettlementAssessmentSummary = [
//      PrisonerResettlementAssessment(Pathway.ACCOMMODATION, ResettlementAssessmentStatus.NOT_STARTED),
//      PrisonerResettlementAssessment(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.NOT_STARTED),
//      PrisonerResettlementAssessment(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.NOT_STARTED),
//      PrisonerResettlementAssessment(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.NOT_STARTED),
//      PrisonerResettlementAssessment(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.NOT_STARTED),
//      PrisonerResettlementAssessment(Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.NOT_STARTED),
//      PrisonerResettlementAssessment(Pathway.HEALTH, ResettlementAssessmentStatus.NOT_STARTED),
//    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, )  ..findById(assessmentId)).thenReturn(Optional.of(assessmentEntity))
//
//    val response = assessmentService.getAssessmentById(assessmentId)
//    Assertions.assertEquals(assessmentEntity, response)
//  }

}
