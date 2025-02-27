package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime

class PrisonerRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Test
  fun `test persist new prisoner`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "xyz1")
    prisonerRepository.save(prisoner)

    val prisonerFromDatabase = prisonerRepository.findAll()[0]

    assertThat(prisonerFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(prisoner)
  }

  @Test
  fun `test findDistinctPrisonIds`() {
    prisonerRepository.saveAll(
      listOf(
        PrisonerEntity(null, "NOM0001", LocalDateTime.now(), "AA1"),
        PrisonerEntity(null, "NOM0002", LocalDateTime.now(), "AA3"),
        PrisonerEntity(null, "NOM0003", LocalDateTime.now(), "AA2"),
        PrisonerEntity(null, "NOM0004", LocalDateTime.now(), "AA2"),
        PrisonerEntity(null, "NOM0005", LocalDateTime.now(), "AA2"),
        PrisonerEntity(null, "NOM0006", LocalDateTime.now(), "AA1"),
        PrisonerEntity(null, "NOM0007", LocalDateTime.now(), "AA4"),
      ),
    )

    assertThat(prisonerRepository.findDistinctPrisonIds()).isEqualTo(listOf("AA1", "AA2", "AA3", "AA4"))
  }

  @Test
  fun `test persist new prisoner with legacy profile`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2025-02-18T12:00:01"), "xyz1", false)
    prisonerRepository.save(prisoner)

    val prisonerFromDatabase = prisonerRepository.findAll()[0]

    assertThat(prisonerFromDatabase).usingRecursiveComparison().isEqualTo(prisoner)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `test edit legacy profile on prisoner`() {
    val prisonerFromDatabase = prisonerRepository.findById(1).get()
    prisonerFromDatabase.supportNeedsLegacyProfile = true
    prisonerRepository.save(prisonerFromDatabase)

    assertThat(prisonerFromDatabase).usingRecursiveComparison().isEqualTo(PrisonerEntity(1, "G4161UF", LocalDateTime.parse("2024-02-19T09:36:28.713421"), "MDI", true))
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoners-legacy-profile.sql")
  fun `test updateProfileResetLegacyProfileFlags`() {
    prisonerRepository.updateProfileResetLegacyProfileFlags()

    val expectedUpdatedPrisoners = mapOf(1L to true, 2L to false, 3L to false, 4L to false, 5L to false, 6L to true, 7L to false, 8L to true, 9L to false)
    val prisonersFromDatabase = prisonerRepository.findAll().sortedBy { it.id }.associate { it.id to it.supportNeedsLegacyProfile }

    assertThat(prisonersFromDatabase).usingRecursiveComparison().isEqualTo(expectedUpdatedPrisoners)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoners-legacy-profile.sql")
  fun `test findAllBySupportNeedsLegacyProfileIsTrue`() {
    Assertions.assertEquals(listOf(8L), prisonerRepository.findAllBySupportNeedsLegacyProfileIsTrue().map { it.id })
  }
}
