package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CaseAllocationRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var caseAllocationRepository: CaseAllocationRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Test
  fun `test persist new case allocation`() {
    val prisoner = prisonerRepository.save(
      PrisonerEntity(
        nomsId = "NOM1234",
        creationDate = LocalDateTime.now(),
        prisonId = "xyz1",
      ),
    )

    val caseAllocationEntity = CaseAllocationEntity(
      prisonerId = prisoner.id(),
      staffId = 123,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
    )

    caseAllocationRepository.save(caseAllocationEntity)

    val caseAllocationFromDatabase = caseAllocationRepository.findAll()[0]

    Assertions.assertThat(caseAllocationFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(caseAllocationEntity)
  }

  @Test
  fun `test findByPrisonerAndIsDeleted`() {
    val prisoner = prisonerRepository.save(
      PrisonerEntity(
        null,
        "NOM1234",
        LocalDateTime.now(),
        "xyz1",
      ),
    )
    val prisoner2 = prisonerRepository.save(
      PrisonerEntity(
        null,
        "NOM5678",
        LocalDateTime.now(),
        "xyz1",
      ),
    )

    val caseAllocationEntity = CaseAllocationEntity(
      prisonerId = prisoner.id(),
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
    )

    val caseAllocationEntity2 = CaseAllocationEntity(
      prisonerId = prisoner2.id(),
      staffId = 4321,
      staffFirstname = "PSO Firstname",
      staffLastname = "PSO Lastname",
      isDeleted = true,
      deletionDate = LocalDateTime.now(),
    )

    caseAllocationRepository.save(caseAllocationEntity)
    caseAllocationRepository.save(caseAllocationEntity2)

    val caseAllocationFromDatabase = caseAllocationRepository.findByPrisonerIdAndIsDeleted(prisoner2.id(), true)

    Assertions.assertThat(caseAllocationFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(caseAllocationEntity2)
  }

  @Test
  fun `test findByPrisonerId query`() {
    // Seed database with prisoners and case allocations
    val prisoner1 = prisonerRepository.save(PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))
    val prisoner2 = prisonerRepository.save(PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))

    val searchDate = LocalDate.now().atTime(LocalTime.NOON)

    val caseAllocations = listOf(
      CaseAllocationEntity(
        prisonerId = prisoner2.id(),
        staffId = 1,
        staffFirstname = "PSO",
        staffLastname = "Staff 1",
        isDeleted = true,
        creationDate = searchDate.minusDays(5),
      ),
      CaseAllocationEntity(
        prisonerId = prisoner2.id(),
        staffId = 2,
        staffFirstname = "PSO",
        staffLastname = "Staff 2",
        isDeleted = true,
        creationDate = searchDate.minusDays(5),
      ),
      CaseAllocationEntity(
        prisonerId = prisoner2.id(),
        staffId = 2,
        staffFirstname = "PSO",
        staffLastname = "Staff 3",
        isDeleted = false,
        creationDate = searchDate.minusDays(1),
      ),
    )

    caseAllocationRepository.saveAll(caseAllocations)

    // Prisoner 1 has no case allocations
    Assertions.assertThat(caseAllocationRepository.findByPrisonerIdAndCreationDateBetween(prisoner1.id(), searchDate.minusDays(7), searchDate)).isEmpty()

    // Prisoner 2 has three - one is out of search range
    Assertions.assertThat(caseAllocationRepository.findByPrisonerIdAndCreationDateBetween(prisoner2.id(), searchDate.minusDays(7), searchDate)).isEqualTo(caseAllocations.filter { it.creationDate > searchDate.minusDays(7) })
  }
}
