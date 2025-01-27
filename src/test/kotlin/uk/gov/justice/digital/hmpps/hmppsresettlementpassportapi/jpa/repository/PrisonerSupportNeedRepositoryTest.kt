package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import java.time.LocalDateTime

class PrisonerSupportNeedRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var prisonerSupportNeedRepository: PrisonerSupportNeedRepository

  @Autowired
  lateinit var supportNeedRepository: SupportNeedRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `test can save and find prisoner support needs`() {
    val prisonerSupportNeed1 = PrisonerSupportNeedEntity(
      prisonerId = 1,
      supportNeed = supportNeedRepository.findById(1).get(),
      otherDetail = null,
      createdBy = "John Smith",
      createdDate = LocalDateTime.parse("2024-04-04T13:00:01"),
    )

    val prisonerSupportNeed2 = PrisonerSupportNeedEntity(
      prisonerId = 1,
      supportNeed = supportNeedRepository.findById(5).get(),
      otherDetail = "Other",
      createdBy = "John Smith",
      createdDate = LocalDateTime.parse("2024-04-04T14:00:01"),
      deleted = true,
      deletedDate = LocalDateTime.parse("2023-04-04T15:00:02"),
    )

    prisonerSupportNeedRepository.saveAll(listOf(prisonerSupportNeed1, prisonerSupportNeed2))

    val prisonerSupportNeedsFromDatabase = prisonerSupportNeedRepository.findAll()
    Assertions.assertEquals(listOf(prisonerSupportNeed1, prisonerSupportNeed2), prisonerSupportNeedsFromDatabase)
  }

  @Test
  fun `test findAllByPrisonerIdAndDeletedIsFalse - no results`() {
    Assertions.assertEquals(listOf<PrisonerSupportNeedEntity>(), prisonerSupportNeedRepository.findAllByPrisonerIdAndDeletedIsFalse(1))
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-1.sql")
  fun `test findAllByPrisonerIdAndDeletedIsFalse`() {
    val expectedPrisonerSupportNeeds = listOf(
      PrisonerSupportNeedEntity(id = 2, prisonerId = 1, supportNeed = supportNeedRepository.findById(1).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 3),
      PrisonerSupportNeedEntity(id = 3, prisonerId = 1, supportNeed = supportNeedRepository.findById(7).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null),
      PrisonerSupportNeedEntity(id = 6, prisonerId = 1, supportNeed = supportNeedRepository.findById(6).get(), otherDetail = "This is an other 1", createdBy = "Someone else", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null),
    )
    Assertions.assertEquals(expectedPrisonerSupportNeeds, prisonerSupportNeedRepository.findAllByPrisonerIdAndDeletedIsFalse(1).sortedBy { it.id })
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-1.sql")
  fun `test getPrisonerSupportNeedsByPrisonId`() {
    val expectedPrisonerSupportNeeds = listOf(
      arrayOf(2L, "G4161UF", Pathway.ACCOMMODATION, LocalDateTime.parse("2024-02-21T09:36:28.713421"), 3L, SupportNeedStatus.MET, LocalDateTime.parse("2024-02-22T09:36:31.713421")),
      arrayOf(5L, "G4161UG", Pathway.ACCOMMODATION, LocalDateTime.parse("2024-02-21T09:36:28.713421"), null, null, null),
      arrayOf(6L, "G4161UF", Pathway.ACCOMMODATION, LocalDateTime.parse("2024-02-21T09:36:28.713421"), null, null, null),
    )
    val prisonerSupportNeedsFromDatabase = prisonerSupportNeedRepository.getPrisonerSupportNeedsByPrisonId("MDI").sortedBy { it[0] as Long }
    assertThat(prisonerSupportNeedsFromDatabase).usingRecursiveComparison().isEqualTo(expectedPrisonerSupportNeeds)
  }
}
