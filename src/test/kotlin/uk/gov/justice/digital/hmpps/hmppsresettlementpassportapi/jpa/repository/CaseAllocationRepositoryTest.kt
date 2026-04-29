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
  fun `test findByPrisonerIdAndCreationDateBetweenOrderByCreationDateDesc query`() {
    // Seed database with prisoners and case allocations
    val prisoner1 = prisonerRepository.save(PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))
    val prisoner2 = prisonerRepository.save(PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))

    val searchDate = LocalDate.now().atTime(LocalTime.NOON)

    val makeCaseAllocation: (staffId: Int, creationDate: LocalDateTime, deletionDate: LocalDateTime?) -> CaseAllocationEntity = { staffId, creationDate, deletionDate ->
      CaseAllocationEntity(
        prisonerId = prisoner2.id(),
        staffId = staffId,
        staffFirstname = "PSO",
        staffLastname = "Staff $staffId",
        isDeleted = deletionDate != null,
        creationDate = creationDate,
        deletionDate = deletionDate,
      )
    }

    // save in chronicle order:
    val caseAllocations = listOf(
      makeCaseAllocation(1, searchDate.minusDays(5), searchDate), // created 5 days ago, deleted today
      makeCaseAllocation(2, searchDate.minusDays(3), searchDate), // created 3 days ago, deleted today
      makeCaseAllocation(3, searchDate.minusDays(1), null), // created 1 day ago, still active (not deleted)
    )

    // search range: 4 days ago to today
    val (searchFrom, searchTo) = searchDate.minusDays(4) to searchDate

    val expectedCaseAllocations = caseAllocations.reversed().filter { it.creationDate > searchFrom }

    caseAllocationRepository.saveAll(caseAllocations)

    // Prisoner 1 has no case allocations
    Assertions.assertThat(caseAllocationRepository.findByPrisonerIdAndCreationDateBetweenOrderByCreationDateDesc(prisoner1.id(), searchFrom, searchTo))
      .isEmpty()

    // Prisoner 2 has three - one is out of search range
    Assertions.assertThat(caseAllocationRepository.findByPrisonerIdAndCreationDateBetweenOrderByCreationDateDesc(prisoner2.id(), searchFrom, searchTo))
      .hasSize(2)
      .isEqualTo(expectedCaseAllocations)
  }
}
