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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.AssessmentSkipReason
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentSkipEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdTypeEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.AssessmentSkipRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.IdTypeRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class AssessmentServiceTest {
  private lateinit var assessmentService: AssessmentService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var assessmentRepository: AssessmentRepository

  @Mock
  private lateinit var assessmentSkipRepository: AssessmentSkipRepository

  @Mock
  private lateinit var idTypeRepository: IdTypeRepository
  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    assessmentService = AssessmentService(assessmentRepository, assessmentSkipRepository, prisonerRepository, idTypeRepository)
  }

  @Test
  fun `test getAssessmentById - returns assessment`() {
    val assessmentId: Long = 1
    val assessmentEntity = AssessmentEntity(1, 1, fakeNow, fakeNow, isBankAccountRequired = true, isIdRequired = true, idDocuments = emptySet(), isDeleted = false, deletionDate = null)
    Mockito.`when`(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessmentEntity))

    val response = assessmentService.getAssessmentById(assessmentId)
    Assertions.assertEquals(assessmentEntity, response)
  }

  @Test
  fun `test getAssessmentById - returns null if assessment is deleted`() {
    val assessmentId: Long = 1

    val assessmentEntity = AssessmentEntity(1, 1, fakeNow, fakeNow, isBankAccountRequired = true, isIdRequired = true, idDocuments = emptySet(), isDeleted = true, deletionDate = fakeNow)
    Mockito.`when`(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessmentEntity))

    val response = assessmentService.getAssessmentById(assessmentId)
    Assertions.assertNull(response)
  }

  @Test
  fun `test getAssessmentById - returns null if assessment does not exist`() {
    val assessmentId: Long = 1
    Mockito.`when`(assessmentRepository.findById(assessmentId)).thenReturn(null)

    val response = assessmentService.getAssessmentById(assessmentId)
    Assertions.assertNull(response)
  }

  @Test
  fun `test deleteAssessment - sets deleted flag`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val assessmentEntity = AssessmentEntity(1, prisonerEntity.id(), fakeNow, fakeNow, isBankAccountRequired = true, isIdRequired = true, idDocuments = emptySet(), isDeleted = false, deletionDate = null)
    val expectedAssessmentEntity = AssessmentEntity(1, prisonerEntity.id(), fakeNow, fakeNow, isBankAccountRequired = true, isIdRequired = true, idDocuments = emptySet(), isDeleted = true, deletionDate = fakeNow)

    assessmentService.deleteAssessment(assessmentEntity)
    Mockito.verify(assessmentRepository).save(expectedAssessmentEntity)

    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test createAssessment - creates assessment`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val nomsId = "abc"
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "xyz")
    val idTypes = listOf(IdTypeEntity(1, "Driving licence"), IdTypeEntity(2, "Birth certificate"), IdTypeEntity(1, "Something else"))
    val expectedIdTypes = listOf(IdTypeEntity(1, "Driving licence"), IdTypeEntity(2, "Birth certificate"))
    val expectedAssessmentEntity = AssessmentEntity(null, prisonerEntity.id(), fakeNow, fakeNow, isBankAccountRequired = true, isIdRequired = true, idDocuments = expectedIdTypes.toSet(), isDeleted = false, deletionDate = null)
    val assessment = Assessment(fakeNow, isBankAccountRequired = true, isIdRequired = true, idDocuments = setOf("Driving licence", "Birth certificate"), nomsId = nomsId)
    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(idTypeRepository.findAll()).thenReturn(idTypes)
    Mockito.`when`(assessmentRepository.save(any())).thenReturn(expectedAssessmentEntity)

    assessmentService.createAssessment(assessment)
    Mockito.verify(assessmentRepository).save(expectedAssessmentEntity)

    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test GetAssessmentByPrisonerIdAndCreationDate should return from repository`() {
    val currentDate = LocalDateTime.now()
    val assessments = listOf(AssessmentEntity(null, 1, currentDate, currentDate, isBankAccountRequired = true, isIdRequired = true, setOf(IdTypeEntity(1, "Birth certificate")), false, null))
    val assessmentsSarContents = listOf(
      AssessmentService.AssessmentSarContent(
        currentDate,
        currentDate,
        isBankAccountRequired = true,
        isIdRequired = true,
        setOf(AssessmentService.IdTypeSarContent("Birth certificate")),
      ),
    )
    Mockito.`when`(assessmentRepository.findByPrisonerIdAndCreationDateBetween(any(), any(), any())).thenReturn(assessments)

    val response = assessmentService.getAssessmentByPrisonerIdAndCreationDate(1, LocalDateTime.now(), LocalDateTime.now())
    Assertions.assertEquals(assessmentsSarContents, response)
  }

  @Test
  fun `test GetSkippedAssessmentsForPrisoner should return from repository`() {
    val skipHistory = listOf(AssessmentSkipEntity(null, ResettlementAssessmentType.BCST2, 1, AssessmentSkipReason.TRANSFER, null, null, null))
    val skipAssessmentsSarContents = listOf(AssessmentService.AssessmentSkipSarContent(ResettlementAssessmentType.BCST2.displayName, AssessmentSkipReason.TRANSFER.displayText, null, null, null))
    Mockito.`when`(assessmentSkipRepository.findByPrisonerIdAndCreationDateBetween(any(), any(), any())).thenReturn(skipHistory)

    val response = assessmentService.getSkippedAssessmentsForPrisoner(1, LocalDateTime.now(), LocalDateTime.now())
    Assertions.assertEquals(skipAssessmentsSarContents, response)
  }
}
