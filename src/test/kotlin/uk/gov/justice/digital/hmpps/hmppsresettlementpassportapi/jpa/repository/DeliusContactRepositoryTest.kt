package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ContactType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime
import kotlin.collections.emptyList

class DeliusContactRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var deliusContactRepository: DeliusContactRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  @AfterEach
  fun beforeEach() {
    deliusContactRepository.deleteAll()
    prisonerRepository.deleteAll()
  }

  @Test
  fun `test persist new delius contact - case note`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "xyz1")
    prisonerRepository.save(prisoner)

    val deliusContact = DeliusContactEntity(
      id = null,
      prisonerId = prisoner.id(),
      category = Category.ACCOMMODATION,
      contactType = ContactType.CASE_NOTE,
      createdDate = LocalDateTime.parse("2023-01-01T12:00:00"),
      notes = "some notes here",
      createdBy = "John Williams",
    )
    deliusContactRepository.save(deliusContact)

    val deliusContactsFromDatabase = deliusContactRepository.findAll()

    assertThat(deliusContactsFromDatabase).usingRecursiveComparison().isEqualTo(listOf(deliusContact))
  }

  @Test
  fun `test persist new delius contact - appointments`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "xyz1")
    prisonerRepository.save(prisoner)

    val deliusContact = DeliusContactEntity(
      id = null,
      prisonerId = prisoner.id(),
      category = Category.ATTITUDES_THINKING_AND_BEHAVIOUR,
      contactType = ContactType.APPOINTMENT,
      createdDate = LocalDateTime.parse("2023-01-01T12:00:00"),
      appointmentDate = LocalDateTime.parse("2023-02-10T14:05:00"),
      appointmentDuration = 60,
      notes = "some notes here",
      createdBy = "William, John",
    )
    deliusContactRepository.save(deliusContact)

    val deliusContactsFromDatabase = deliusContactRepository.findAll()

    assertThat(deliusContactsFromDatabase).usingRecursiveComparison().isEqualTo(listOf(deliusContact))
  }

  @Test
  fun `test findByPrisonerIdAndContactTypeAndCreatedDateBetween`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "xyz1")
    prisonerRepository.save(prisoner)

    val deliusContact = DeliusContactEntity(
      id = null,
      prisonerId = prisoner.id(),
      category = Category.ATTITUDES_THINKING_AND_BEHAVIOUR,
      contactType = ContactType.APPOINTMENT,
      createdDate = LocalDateTime.parse("2023-01-01T12:00:00"),
      appointmentDate = LocalDateTime.parse("2023-02-10T14:05:00"),
      appointmentDuration = 60,
      notes = "some notes here",
      createdBy = "William, John",
    )
    deliusContactRepository.save(deliusContact)

    val found = deliusContactRepository.findByPrisonerIdAndContactTypeAndCreatedDateBetween(prisoner.id(), ContactType.APPOINTMENT, LocalDateTime.parse("2023-01-01T11:00:00"), LocalDateTime.parse("2023-01-01T13:00:00"))
    assertThat(found).usingRecursiveComparison().isEqualTo(listOf(deliusContact))

    val notFoundPrisonerId = deliusContactRepository.findByPrisonerIdAndContactTypeAndCreatedDateBetween(prisoner.id() + 1, ContactType.APPOINTMENT, LocalDateTime.parse("2023-01-01T11:00:00"), LocalDateTime.parse("2023-01-01T13:00:00"))
    assertThat(notFoundPrisonerId).usingRecursiveComparison().isEqualTo(emptyList<DeliusContactEntity>())

    val notFoundContactType = deliusContactRepository.findByPrisonerIdAndContactTypeAndCreatedDateBetween(prisoner.id(), ContactType.CASE_NOTE, LocalDateTime.parse("2023-01-01T11:00:00"), LocalDateTime.parse("2023-01-01T13:00:00"))
    assertThat(notFoundContactType).usingRecursiveComparison().isEqualTo(emptyList<DeliusContactEntity>())

    val notFoundCreatedDate = deliusContactRepository.findByPrisonerIdAndContactTypeAndCreatedDateBetween(prisoner.id(), ContactType.APPOINTMENT, LocalDateTime.parse("2023-01-01T11:00:00"), LocalDateTime.parse("2023-01-01T11:59:00"))
    assertThat(notFoundCreatedDate).usingRecursiveComparison().isEqualTo(emptyList<DeliusContactEntity>())
  }
}
