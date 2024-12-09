package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime

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
        crn = "crn1",
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
        "crn1",
        "xyz1",
      ),
    )
    val prisoner2 = prisonerRepository.save(
      PrisonerEntity(
        null,
        "NOM5678",
        LocalDateTime.now(),
        "crn1",
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
  fun `test getPrisonsWithCaseAllocations`() {
    val prisoner1 = prisonerRepository.save(
      PrisonerEntity(
        nomsId = "A000001",
        crn = null,
        prisonId = "AAI",
      ),
    )
    val prisoner2 = prisonerRepository.save(
      PrisonerEntity(
        nomsId = "A000002",
        crn = null,
        prisonId = "AAI",
      ),
    )
    val prisoner3 = prisonerRepository.save(
      PrisonerEntity(
        nomsId = "A000003",
        crn = null,
        prisonId = "BBI",
      ),
    )
    val prisoner4 = prisonerRepository.save(
      PrisonerEntity(
        nomsId = "A000004",
        crn = null,
        prisonId = "BBI",
      ),
    )
    prisonerRepository.save(
      PrisonerEntity(
        nomsId = "A000005",
        crn = null,
        prisonId = "BBI",
      ),
    )
    val prisoner6 = prisonerRepository.save(
      PrisonerEntity(
        nomsId = "A000006",
        crn = null,
        prisonId = "CCI",
      ),
    )
    prisonerRepository.save(
      PrisonerEntity(
        nomsId = "A000007",
        crn = null,
        prisonId = "DDI",
      ),
    )

    caseAllocationRepository.saveAll(
      listOf(
        CaseAllocationEntity(
          prisonerId = prisoner1.id(),
          staffId = 123,
          staffFirstname = "James",
          staffLastname = "Smith",
        ),
        CaseAllocationEntity(
          prisonerId = prisoner2.id(),
          staffId = 123,
          staffFirstname = "James",
          staffLastname = "Smith",
        ),
        CaseAllocationEntity(
          prisonerId = prisoner3.id(),
          staffId = 123,
          staffFirstname = "James",
          staffLastname = "Smith",
          isDeleted = true,
        ),
        CaseAllocationEntity(
          prisonerId = prisoner4.id(),
          staffId = 123,
          staffFirstname = "James",
          staffLastname = "Smith",
          isDeleted = true,
        ),
        CaseAllocationEntity(
          prisonerId = prisoner6.id(),
          staffId = 123,
          staffFirstname = "James",
          staffLastname = "Smith",
          isDeleted = true,
        ),
      ),
    )
    Assertions.assertThat(caseAllocationRepository.findPrisonsWithCaseAllocations().sorted()).isEqualTo(listOf("AAI", "BBI", "CCI"))
  }
}
