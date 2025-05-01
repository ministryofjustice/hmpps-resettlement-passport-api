package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.AssessmentSkipReason
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentSkipEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.time.LocalDateTime

class AssessmentSkipRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var assessmentSkipRepository: AssessmentSkipRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  var prisonerId: Long = 0L
  var skippedAssessment: AssessmentSkipEntity? = null

  @BeforeEach
  fun beforeEach() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "xyz1")
    prisonerRepository.save(prisoner)
    prisonerId = prisoner.id()

    skippedAssessment = AssessmentSkipEntity(
      null,
      ResettlementAssessmentType.BCST2,
      prisoner.id(),
      AssessmentSkipReason.OTHER,
      "More info",
      "Admin user",
    )
    assessmentSkipRepository.save(skippedAssessment!!)
  }

  @Test
  fun `should persist new skipped assessment`() {
    val result = assessmentSkipRepository.findAll()[0]
    Assertions.assertEquals(skippedAssessment, result)
  }

  @Nested
  inner class FindByPrisonerIdAndCreationDateBetween {

    @Test
    fun `should return results within time range`() {
      val createdDate = skippedAssessment?.creationDate!!
      val result = assessmentSkipRepository.findByPrisonerIdAndCreationDateBetween(
        prisonerId,
        createdDate.minusDays(1),
        createdDate.plusDays(1),
      )
      Assertions.assertEquals(listOf(skippedAssessment), result)
    }

    @Test
    fun `should not return results outside of time range`() {
      val createdDate = skippedAssessment?.creationDate!!
      val result = assessmentSkipRepository.findByPrisonerIdAndCreationDateBetween(
        prisonerId,
        createdDate.minusDays(2),
        createdDate.minusDays(1),
      )
      Assertions.assertEquals(emptyList<AssessmentSkipEntity>(), result)
    }

    @Test
    fun `should not return results if we don't match the prisoner id`() {
      val createdDate = skippedAssessment?.creationDate!!
      val result = assessmentSkipRepository.findByPrisonerIdAndCreationDateBetween(
        prisonerId + 1,
        createdDate.minusDays(1),
        createdDate.plusDays(1),
      )
      Assertions.assertEquals(emptyList<AssessmentSkipEntity>(), result)
    }
  }
}
