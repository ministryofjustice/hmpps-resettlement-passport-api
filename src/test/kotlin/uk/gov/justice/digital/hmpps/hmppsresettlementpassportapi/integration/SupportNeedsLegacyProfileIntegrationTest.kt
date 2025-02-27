package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.SupportNeedRepository
import java.time.LocalDateTime

class SupportNeedsLegacyProfileIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var prisonerSupportNeedRepository: PrisonerSupportNeedRepository

  @Autowired
  lateinit var supportNeedRepository: SupportNeedRepository

  @Test
  @Sql("classpath:testdata/sql/seed-prisoners-legacy-profile-3.sql")
  fun `test addLegacySupportNeeds - happy path`() {
    val fakeNow = LocalDateTime.parse("2025-02-27T08:29:47.029814")
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    authedWebTestClient.post()
      .uri("/add-legacy-support-needs")
      .exchange()
      .expectStatus().isOk

    val expectedPrisonerSupportNeeds = listOf(
      PrisonerSupportNeedEntity(id = 1, prisonerId = 4, supportNeed = supportNeedRepository.findById(114).get(), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 2, prisonerId = 4, supportNeed = supportNeedRepository.findById(115).get(), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 3, prisonerId = 4, supportNeed = supportNeedRepository.findById(116).get(), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 4, prisonerId = 4, supportNeed = supportNeedRepository.findById(117).get(), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 5, prisonerId = 4, supportNeed = supportNeedRepository.findById(118).get(), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 6, prisonerId = 4, supportNeed = supportNeedRepository.findById(119).get(), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 7, prisonerId = 4, supportNeed = supportNeedRepository.findById(120).get(), otherDetail = null, createdBy = "System User", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 101, prisonerId = 2, supportNeed = supportNeedRepository.findById(1).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2023-08-17T12:26:03.441"), deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 102, prisonerId = 2, supportNeed = supportNeedRepository.findById(2).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2023-08-18T12:26:03.441"), deleted = false, deletedDate = null, latestUpdateId = null),
    )
    Assertions.assertEquals(expectedPrisonerSupportNeeds, prisonerSupportNeedRepository.findAll().sortedBy { it.id })

    unmockkAll()
  }

  @Test
  fun `test addLegacySupportNeeds - unauthorized`() {
    webTestClient.post()
      .uri("/add-legacy-support-needs")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `test addLegacySupportNeeds - forbidden`() {
    webTestClient.post()
      .uri("/add-legacy-support-needs")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }
}