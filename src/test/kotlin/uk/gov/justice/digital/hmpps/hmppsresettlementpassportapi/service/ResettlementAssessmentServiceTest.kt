package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
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
  private lateinit var resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository

  @Mock
  private lateinit var deliusContactService: DeliusContactService

  @Mock
  private lateinit var caseNotesService: CaseNotesService

  @Mock
  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Mock
  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentService = ResettlementAssessmentService(resettlementAssessmentRepository, resettlementAssessmentStatusRepository, prisonerRepository, pathwayRepository, deliusContactService, caseNotesService, pathwayAndStatusService)
  }

  @Test
  fun `test getResettlementAssessmentSummaryByNomsId - returns assessment- combination of not started and complete`() {
    val nomsId: String = "GY3245"
    val assessmentType = ResettlementAssessmentType.BCST2
    val prisonerEntity = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23"))
    val accommodationPathwayEntity = PathwayEntity(1, "Accommodation", true, fakeNow)
    val accommodationResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(1, "Accommodation")
    val attitudesPathwayEntity = PathwayEntity(2, "Attitudes, thinking and behaviour", true, fakeNow)
    val attitudesResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(2, "Attitudes, thinking and behaviour")
    val childrenFamiliesPathwayEntity = PathwayEntity(3, "Children, families and communities", true, fakeNow)
    val childrenFamiliesResettlementAssessmentEntity = createCompleteResettlementAssessmentEntity(3, "Children, families and communities")
    val drugsAlcoholPathwayEntity = PathwayEntity(4, "Drugs and alcohol", true, fakeNow)
    val drugsAlcoholResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(4, "Drugs and alcohol")
    val educationSkillsPathwayEntity = PathwayEntity(5, "Education, skills and work", true, fakeNow)
    val educationSkillsResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(5, "Education, skills and work")
    val financeIdPathwayEntity = PathwayEntity(6, "Finance and ID", true, fakeNow)
    val financeIdResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(6, "Finance and ID")
    val healthPathwayEntity = PathwayEntity(7, "Health", true, fakeNow)
    val healthResettlementAssessmentEntity = createCompleteResettlementAssessmentEntity(7, "Health")

    val prisonerResettlementAssessmentSummary = listOf(
      PrisonerResettlementAssessment(Pathway.ACCOMMODATION, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.COMPLETE),
      PrisonerResettlementAssessment(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.HEALTH, ResettlementAssessmentStatus.COMPLETE),
    )

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(pathwayRepository.findById(Pathway.ACCOMMODATION.id)).thenReturn(Optional.of(accommodationPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.id)).thenReturn(Optional.of(attitudesPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.id)).thenReturn(Optional.of(childrenFamiliesPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.DRUGS_AND_ALCOHOL.id)).thenReturn(Optional.of(drugsAlcoholPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.EDUCATION_SKILLS_AND_WORK.id)).thenReturn(Optional.of(educationSkillsPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.FINANCE_AND_ID.id)).thenReturn(Optional.of(financeIdPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.HEALTH.id)).thenReturn(Optional.of(healthPathwayEntity))
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, accommodationPathwayEntity, assessmentType)).thenReturn(accommodationResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, attitudesPathwayEntity, assessmentType)).thenReturn(attitudesResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, childrenFamiliesPathwayEntity, assessmentType)).thenReturn(childrenFamiliesResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, drugsAlcoholPathwayEntity, assessmentType)).thenReturn(drugsAlcoholResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, educationSkillsPathwayEntity, assessmentType)).thenReturn(educationSkillsResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, financeIdPathwayEntity, assessmentType)).thenReturn(financeIdResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, healthPathwayEntity, assessmentType)).thenReturn(healthResettlementAssessmentEntity)

    val response = resettlementAssessmentService.getResettlementAssessmentSummaryByNomsId(nomsId)
    Assertions.assertEquals(prisonerResettlementAssessmentSummary, response)
  }

  @Test
  fun `test getResettlementAssessmentSummaryByNomsId - returns assessment with not started statuses for pathways with null value in resettlement_assessment table`() {
    val nomsId: String = "GY3245"
    val assessmentType = ResettlementAssessmentType.BCST2
    val prisonerEntity = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23"))
    val accommodationPathwayEntity = PathwayEntity(1, "Accommodation", true, fakeNow)
    val accommodationResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(1, "Accommodation")
    val attitudesPathwayEntity = PathwayEntity(2, "Attitudes, thinking and behaviour", true, fakeNow)
    val attitudesResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(2, "Attitudes, thinking and behaviour")
    val childrenFamiliesPathwayEntity = PathwayEntity(3, "Children, families and communities", true, fakeNow)
    val drugsAlcoholPathwayEntity = PathwayEntity(4, "Drugs and alcohol", true, fakeNow)
    val drugsAlcoholResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(4, "Drugs and alcohol")
    val educationSkillsPathwayEntity = PathwayEntity(5, "Education, skills and work", true, fakeNow)
    val educationSkillsResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(5, "Education, skills and work")
    val financeIdPathwayEntity = PathwayEntity(6, "Finance and ID", true, fakeNow)
    val healthPathwayEntity = PathwayEntity(7, "Health", true, fakeNow)
    val healthResettlementAssessmentEntity = createCompleteResettlementAssessmentEntity(7, "Health")

    val prisonerResettlementAssessmentSummary = listOf(
      PrisonerResettlementAssessment(Pathway.ACCOMMODATION, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.HEALTH, ResettlementAssessmentStatus.COMPLETE),
    )

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(pathwayRepository.findById(Pathway.ACCOMMODATION.id)).thenReturn(Optional.of(accommodationPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.id)).thenReturn(Optional.of(attitudesPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.id)).thenReturn(Optional.of(childrenFamiliesPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.DRUGS_AND_ALCOHOL.id)).thenReturn(Optional.of(drugsAlcoholPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.EDUCATION_SKILLS_AND_WORK.id)).thenReturn(Optional.of(educationSkillsPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.FINANCE_AND_ID.id)).thenReturn(Optional.of(financeIdPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.HEALTH.id)).thenReturn(Optional.of(healthPathwayEntity))
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, accommodationPathwayEntity, assessmentType)).thenReturn(accommodationResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, attitudesPathwayEntity, assessmentType)).thenReturn(attitudesResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, childrenFamiliesPathwayEntity, assessmentType)).thenReturn(null)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, drugsAlcoholPathwayEntity, assessmentType)).thenReturn(drugsAlcoholResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, educationSkillsPathwayEntity, assessmentType)).thenReturn(educationSkillsResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, financeIdPathwayEntity, assessmentType)).thenReturn(null)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, healthPathwayEntity, assessmentType)).thenReturn(healthResettlementAssessmentEntity)

    val response = resettlementAssessmentService.getResettlementAssessmentSummaryByNomsId(nomsId)
    Assertions.assertEquals(prisonerResettlementAssessmentSummary, response)
  }

  private fun createNotStartedResettlementAssessmentEntity(id: Long, name: String) = ResettlementAssessmentEntity(
    id = id,
    prisoner = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23")),
    pathway = PathwayEntity(id = id, name = name, active = true, fakeNow),
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatusEntity(id = 1, name = "Not Started", true, fakeNow),
    assessment = ResettlementAssessmentQuestionAndAnswerList(listOf()),
    creationDate = fakeNow,
    createdBy = "PO",
    statusChangedTo = StatusEntity(id = 1, name = "Not Started", active = true, creationDate = fakeNow),
    caseNoteText = "some case note text",
    createdByUserId = "USER_1",
  )

  private fun createCompleteResettlementAssessmentEntity(id: Long, name: String) = ResettlementAssessmentEntity(
    id = id,
    prisoner = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23")),
    pathway = PathwayEntity(id = id, name = name, active = true, fakeNow),
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", true, fakeNow),
    assessment = ResettlementAssessmentQuestionAndAnswerList(listOf()),
    creationDate = fakeNow,
    createdBy = "PO",
    statusChangedTo = StatusEntity(id = 4, name = "Support Declined", active = true, creationDate = fakeNow),
    caseNoteText = "some case note text",
    createdByUserId = "USER_1",
  )
}
