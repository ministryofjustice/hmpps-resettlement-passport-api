package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime

class PathwayStatusRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Test
  fun `test create new pathway status`() {
    var prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1")
    prisoner = prisonerRepository.save(prisoner)

    val pathwayStatus = PathwayStatusEntity(
      null,
      prisoner.id(),
      Pathway.ACCOMMODATION,
      Status.IN_PROGRESS,
      LocalDateTime.now(),
    )
    pathwayStatusRepository.save(pathwayStatus)

    val pathwayStatusInDatabase = pathwayStatusRepository.findAll()[0]

    assertThat(pathwayStatusInDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(pathwayStatus)
  }

  @Test
  fun `test findByPrison query`() {
    // Seed database with prisoners and pathway statuses
    prisonerRepository.save(PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN1", "MDI"))
    val prisoner2 = prisonerRepository.save(PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN2", "MDI"))
    val prisoner3 = prisonerRepository.save(PrisonerEntity(null, "NOMS3", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN3", "MDI"))
    val prisoner4 = prisonerRepository.save(PrisonerEntity(null, "NOMS4", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN4", "BWI"))

    // Prisoner 1 has no pathway statuses
    // Prisoners 2 and 3 has all pathways set
    // Prisoner 4 has pathways but is in a different prison
    val pathwayStatusesPrisoner2 = listOf(
      PathwayStatusEntity(null, prisoner2.id(), Pathway.ACCOMMODATION, Status.NOT_STARTED, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2.id(), Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, Status.SUPPORT_NOT_REQUIRED, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2.id(), Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, Status.SUPPORT_DECLINED, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2.id(), Pathway.DRUGS_AND_ALCOHOL, Status.IN_PROGRESS, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2.id(), Pathway.EDUCATION_SKILLS_AND_WORK, Status.DONE, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2.id(), Pathway.FINANCE_AND_ID, Status.NOT_STARTED, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner2.id(), Pathway.HEALTH, Status.DONE, LocalDateTime.parse("2023-09-12T12:34:56")),
    )
    val pathwayStatusesPrisoner3 = listOf(
      PathwayStatusEntity(null, prisoner3.id(), Pathway.ACCOMMODATION, Status.DONE, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3.id(), Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, Status.SUPPORT_DECLINED, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3.id(), Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, Status.NOT_STARTED, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3.id(), Pathway.DRUGS_AND_ALCOHOL, Status.IN_PROGRESS, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3.id(), Pathway.EDUCATION_SKILLS_AND_WORK, Status.SUPPORT_DECLINED, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3.id(), Pathway.FINANCE_AND_ID, Status.SUPPORT_NOT_REQUIRED, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner3.id(), Pathway.HEALTH, Status.NOT_STARTED, LocalDateTime.parse("2023-09-12T12:34:56")),
    )
    val pathwayStatusesPrisoner4 = listOf(
      PathwayStatusEntity(null, prisoner4.id(), Pathway.ACCOMMODATION, Status.SUPPORT_NOT_REQUIRED, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4.id(), Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, Status.IN_PROGRESS, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4.id(), Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, Status.DONE, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4.id(), Pathway.DRUGS_AND_ALCOHOL, Status.IN_PROGRESS, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4.id(), Pathway.EDUCATION_SKILLS_AND_WORK, Status.IN_PROGRESS, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4.id(), Pathway.FINANCE_AND_ID, Status.SUPPORT_DECLINED, LocalDateTime.parse("2023-09-12T12:34:56")),
      PathwayStatusEntity(null, prisoner4.id(), Pathway.HEALTH, Status.NOT_STARTED, LocalDateTime.parse("2023-09-12T12:34:56")),
    )

    pathwayStatusRepository.saveAll(pathwayStatusesPrisoner2 + pathwayStatusesPrisoner3 + pathwayStatusesPrisoner4)

    val result = pathwayStatusRepository.findByPrison("MDI")
    assertThat(result)
      .extracting(PrisonerWithStatusProjection::prisonerId, PrisonerWithStatusProjection::pathway, PrisonerWithStatusProjection::pathwayStatus)
      .containsExactlyInAnyOrderElementsOf(
        pathwayStatusesPrisoner2.map { Tuple(it.prisonerId, it.pathway, it.status) } +
          pathwayStatusesPrisoner3.map { Tuple(it.prisonerId, it.pathway, it.status) },
      )
  }
}
