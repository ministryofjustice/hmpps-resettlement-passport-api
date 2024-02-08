package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import java.time.LocalDate
import java.time.LocalDateTime

class PathwayStatusRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var pathwayRepository: PathwayRepository

  @Autowired
  lateinit var statusRepository: StatusRepository

  @Test
  fun `test create new pathway status`() {
    var prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1", LocalDate.parse("2025-01-23"))
    prisoner = prisonerRepository.save(prisoner)

    val pathwayStatus = PathwayStatusEntity(
      null,
      prisoner,
      PathwayEntity(Pathway.ACCOMMODATION.id, "Accommodation", true, LocalDateTime.now()),
      StatusEntity(Status.IN_PROGRESS.id, "In Progress", true, LocalDateTime.now()),
      LocalDateTime.now(),
    )
    pathwayStatusRepository.save(pathwayStatus)

    val pathwayStatusInDatabase = pathwayStatusRepository.findAll()[0]

    assertThat(pathwayStatusInDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(pathwayStatus)
  }

  @Test
  fun `test findByPrison query`() {
    // Seed database with prisoners and pathway statuses
    val prisoner1 = PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN1", "MDI", LocalDate.parse("2033-01-02"))
    val prisoner2 = PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN2", "MDI", LocalDate.parse("2043-09-16"))
    val prisoner3 = PrisonerEntity(null, "NOMS3", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN3", "MDI", LocalDate.parse("2024-04-12"))
    val prisoner4 = PrisonerEntity(null, "NOMS4", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN4", "BWI", LocalDate.parse("2030-07-11"))

    // Prisoner 1 has no pathway statuses
    // Prisoners 2 and 3 has all pathways set
    // Prisoner 4 has pathways but is in a different prison
    val pathwayStatusesPrisoner2 = listOf(
      PathwayStatusEntity(null, prisoner2, getPathwayEntity(Pathway.ACCOMMODATION), getStatusEntity(Status.NOT_STARTED), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2, getPathwayEntity(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR), getStatusEntity(Status.SUPPORT_NOT_REQUIRED), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2, getPathwayEntity(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY), getStatusEntity(Status.SUPPORT_DECLINED), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2, getPathwayEntity(Pathway.DRUGS_AND_ALCOHOL), getStatusEntity(Status.IN_PROGRESS), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2, getPathwayEntity(Pathway.EDUCATION_SKILLS_AND_WORK), getStatusEntity(Status.DONE), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2, getPathwayEntity(Pathway.FINANCE_AND_ID), getStatusEntity(Status.NOT_STARTED), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2, getPathwayEntity(Pathway.HEALTH), getStatusEntity(Status.DONE), LocalDateTime.parse("2023-09-12T12:34:56")),
    )
    val pathwayStatusesPrisoner3 = listOf(
      PathwayStatusEntity(null, prisoner3, getPathwayEntity(Pathway.ACCOMMODATION), getStatusEntity(Status.DONE), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3, getPathwayEntity(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR), getStatusEntity(Status.SUPPORT_DECLINED), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3, getPathwayEntity(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY), getStatusEntity(Status.NOT_STARTED), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3, getPathwayEntity(Pathway.DRUGS_AND_ALCOHOL), getStatusEntity(Status.IN_PROGRESS), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3, getPathwayEntity(Pathway.EDUCATION_SKILLS_AND_WORK), getStatusEntity(Status.SUPPORT_DECLINED), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3, getPathwayEntity(Pathway.FINANCE_AND_ID), getStatusEntity(Status.SUPPORT_NOT_REQUIRED), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3, getPathwayEntity(Pathway.HEALTH), getStatusEntity(Status.NOT_STARTED), LocalDateTime.parse("2023-09-12T12:34:56")),
    )
    val pathwayStatusesPrisoner4 = listOf(
      PathwayStatusEntity(null, prisoner4, getPathwayEntity(Pathway.ACCOMMODATION), getStatusEntity(Status.SUPPORT_NOT_REQUIRED), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4, getPathwayEntity(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR), getStatusEntity(Status.IN_PROGRESS), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4, getPathwayEntity(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY), getStatusEntity(Status.DONE), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4, getPathwayEntity(Pathway.DRUGS_AND_ALCOHOL), getStatusEntity(Status.IN_PROGRESS), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4, getPathwayEntity(Pathway.EDUCATION_SKILLS_AND_WORK), getStatusEntity(Status.IN_PROGRESS), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4, getPathwayEntity(Pathway.FINANCE_AND_ID), getStatusEntity(Status.SUPPORT_DECLINED), LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4, getPathwayEntity(Pathway.HEALTH), getStatusEntity(Status.NOT_STARTED), LocalDateTime.parse("2023-09-12T12:34:56")),
    )

    prisonerRepository.saveAll(listOf(prisoner1, prisoner2, prisoner3, prisoner4))
    pathwayStatusRepository.saveAll(pathwayStatusesPrisoner2 + pathwayStatusesPrisoner3 + pathwayStatusesPrisoner4)

    assertThat(pathwayStatusRepository.findByPrison("MDI")).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(pathwayStatusesPrisoner2 + pathwayStatusesPrisoner3)
  }

  fun getPathwayEntity(pathway: Pathway): PathwayEntity = pathwayRepository.getReferenceById(pathway.id)
  fun getStatusEntity(status: Status): StatusEntity = statusRepository.getReferenceById(status.id)
}
