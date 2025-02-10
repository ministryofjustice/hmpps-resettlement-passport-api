package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedsRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedsUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedUpdateRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.SupportNeedRepository
import java.time.LocalDateTime

class SupportNeedsIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var supportNeedRepository: SupportNeedRepository

  @Autowired
  private lateinit var prisonerSupportNeedRepository: PrisonerSupportNeedRepository

  @Autowired
  private lateinit var prisonerSupportNeedUpdateRepository: PrisonerSupportNeedUpdateRepository

  companion object {
    val fakeNow: LocalDateTime = LocalDateTime.parse("2025-01-09T12:00:00")

    @JvmStatic
    @BeforeAll
    fun beforeAll() {
      mockkStatic(LocalDateTime::class)
      every { LocalDateTime.now() } returns fakeNow
    }

    @JvmStatic
    @AfterAll
    fun afterAll() {
      unmockkAll()
    }
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-2.sql")
  fun `get support needs summary - happy path`() {
    val expectedOutput = readFile("testdata/expectation/support-needs-summary-1.json")
    val nomsId = "G4161UF"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `get support needs summary - no support needs`() {
    val expectedOutput = readFile("testdata/expectation/support-needs-summary-2.json")
    val nomsId = "G4161UF"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-3.sql")
  fun `get support needs summary - no updates available (only no support needs identified)`() {
    val expectedOutput = readFile("testdata/expectation/support-needs-summary-3.json")
    val nomsId = "G4161UF"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `get support needs summary - no prisoner found`() {
    val nomsId = "G4161UF"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get support needs summary - unauthorised`() {
    val nomsId = "G4161UF"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get support needs summary - forbidden`() {
    val nomsId = "G4161UF"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/summary")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test get pathway support need summary - happy path`() {
    val expectedOutput = readFile("testdata/expectation/pathway-support-needs-summary-1.json")
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/summary")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `get pathway support needs summary - no prisoner found`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/summary")
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get pathway support needs summary - pathway invalid`() {
    val nomsId = "G4161UF"
    val pathway = "NOT_A_PATHWAY"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/summary")
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `get pathway support needs summary - unauthorised`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/summary")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get pathway support needs summary - forbidden`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/summary")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test get pathway support need updates - happy path with defaults`() {
    val expectedOutput = readFile("testdata/expectation/pathway-support-needs-updates-1.json")
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test get pathway support need updates - happy path with query params`() {
    val expectedOutput = readFile("testdata/expectation/pathway-support-needs-updates-2.json")
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates?page=0&size=5&sort=createdDate,ASC&filterByPrisonerSupportNeedId=1")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `get pathway support needs updates - no prisoner found`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates")
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get pathway support needs updates - pathway invalid`() {
    val nomsId = "G4161UF"
    val pathway = "NOT_A_PATHWAY"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates")
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `get pathway support needs updates - unauthorised`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get pathway support needs updates - forbidden`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway/updates")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test get pathway support needs - happy path`() {
    val expectedOutput = readFile("testdata/expectation/pathway-support-needs-1.json")
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `get pathway support needs - no prisoner found`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway")
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get pathway support needs - pathway invalid`() {
    val nomsId = "G4161UF"
    val pathway = "NOT_A_PATHWAY"
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway")
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `get pathway support needs - unauthorised`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get pathway support needs - forbidden`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.ACCOMMODATION
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/needs/$pathway")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test get a support need - happy path`() {
    val expectedOutput = readFile("testdata/expectation/get-a-support-need-1.json")
    val nomsId = "G4161UF"
    val prisonerSupportNeedId = 1
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/prisoner-need/$prisonerSupportNeedId")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `get a support need - no prisoner found`() {
    val nomsId = "G4161UF"
    val prisonerSupportNeedId = 1
    authedWebTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/prisoner-need/$prisonerSupportNeedId")
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get a support need - unauthorised`() {
    val nomsId = "G4161UF"
    val prisonerSupportNeedId = 1
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/prisoner-need/$prisonerSupportNeedId")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get a support need - forbidden`() {
    val nomsId = "G4161UF"
    val prisonerSupportNeedId = 1
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/prisoner-need/$prisonerSupportNeedId")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-5.sql")
  fun `test post support needs - happy path`() {
    val nomsId = "G4161UF"
    authedWebTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/needs")
      .bodyValue(
        PrisonerNeedsRequest(
          needs = listOf(
            // New need
            PrisonerNeedRequest(
              needId = 8,
              prisonerSupportNeedId = null,
              otherDesc = null,
              text = "This is an update 1",
              status = SupportNeedStatus.NOT_STARTED,
              isPrisonResponsible = true,
              isProbationResponsible = false,
            ),
            // Update of existing need
            PrisonerNeedRequest(
              needId = 1,
              prisonerSupportNeedId = 101,
              otherDesc = null,
              text = "This is an update 2",
              status = SupportNeedStatus.MET,
              isPrisonResponsible = false,
              isProbationResponsible = true,
            ),
            // New need - to be treated as update due to need already being assigned to prisoner
            PrisonerNeedRequest(
              needId = 3,
              prisonerSupportNeedId = null,
              otherDesc = null,
              text = "This is an update 3",
              status = SupportNeedStatus.MET,
              isPrisonResponsible = false,
              isProbationResponsible = true,
            ),
            // New need - other with a new otherDec
            PrisonerNeedRequest(
              needId = 5,
              prisonerSupportNeedId = null,
              otherDesc = "Other 3",
              text = "This is an update 4",
              status = SupportNeedStatus.MET,
              isPrisonResponsible = false,
              isProbationResponsible = true,
            ),
            // New need - "excludeFromCount" i.e. a need with no update
            PrisonerNeedRequest(
              needId = 11,
              prisonerSupportNeedId = null,
              otherDesc = null,
              text = null,
              status = null,
              isPrisonResponsible = null,
              isProbationResponsible = null,
            ),
          ),
        ),
      )
      .exchange()
      .expectStatus().isOk

    val expectedPrisonerSupportNeeds = listOf(
      PrisonerSupportNeedEntity(id = 1, prisonerId = 1, supportNeed = supportNeedRepository.findById(8).get(), otherDetail = null, createdBy = "test", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = 1),
      PrisonerSupportNeedEntity(id = 2, prisonerId = 1, supportNeed = supportNeedRepository.findById(5).get(), otherDetail = "Other 3", createdBy = "test", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = 4),
      PrisonerSupportNeedEntity(id = 3, prisonerId = 1, supportNeed = supportNeedRepository.findById(11).get(), otherDetail = null, createdBy = "test", createdDate = fakeNow, deleted = false, deletedDate = null, latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 101, prisonerId = 1, supportNeed = supportNeedRepository.findById(1).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 2),
      PrisonerSupportNeedEntity(id = 102, prisonerId = 1, supportNeed = supportNeedRepository.findById(3).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = true, deletedDate = LocalDateTime.parse("2024-02-22T09:36:28.713421"), latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 103, prisonerId = 1, supportNeed = supportNeedRepository.findById(3).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 3),
      PrisonerSupportNeedEntity(id = 104, prisonerId = 1, supportNeed = supportNeedRepository.findById(5).get(), otherDetail = "Other 1", createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 107),
      PrisonerSupportNeedEntity(id = 105, prisonerId = 1, supportNeed = supportNeedRepository.findById(5).get(), otherDetail = "Other 2", createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 109),
      PrisonerSupportNeedEntity(id = 106, prisonerId = 1, supportNeed = supportNeedRepository.findById(15).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = null),
    )
    Assertions.assertEquals(expectedPrisonerSupportNeeds, prisonerSupportNeedRepository.findAll().sortedBy { it.id })

    val expectedPrisonerSupportNeedUpdates = listOf(
      PrisonerSupportNeedUpdateEntity(id = 1, prisonerSupportNeedId = 1, createdBy = "test", createdDate = fakeNow, updateText = "This is an update 1", status = SupportNeedStatus.NOT_STARTED, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 2, prisonerSupportNeedId = 101, createdBy = "test", createdDate = fakeNow, updateText = "This is an update 2", status = SupportNeedStatus.MET, isPrison = false, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 3, prisonerSupportNeedId = 103, createdBy = "test", createdDate = fakeNow, updateText = "This is an update 3", status = SupportNeedStatus.MET, isPrison = false, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 4, prisonerSupportNeedId = 2, createdBy = "test", createdDate = fakeNow, updateText = "This is an update 4", status = SupportNeedStatus.MET, isPrison = false, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 101, prisonerSupportNeedId = 101, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-01T09:36:32.713421"), updateText = "This is an update 1", status = SupportNeedStatus.NOT_STARTED, isPrison = false, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 102, prisonerSupportNeedId = 101, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-01T09:36:32.713421"), updateText = "Deleted update", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true, deleted = true, deletedDate = LocalDateTime.parse("2024-02-01T09:36:32.713421")),
      PrisonerSupportNeedUpdateEntity(id = 103, prisonerSupportNeedId = 101, createdBy = "User B", createdDate = LocalDateTime.parse("2024-02-03T09:36:32.713421"), updateText = "This is an update 2", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 104, prisonerSupportNeedId = 101, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-02T09:36:32.713421"), updateText = "This is an update 3", status = SupportNeedStatus.MET, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 105, prisonerSupportNeedId = 103, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-02T09:36:32.713421"), updateText = "This is an update 4", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 106, prisonerSupportNeedId = 103, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-05T09:36:32.713421"), updateText = "This is an update 5", status = SupportNeedStatus.NOT_STARTED, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 107, prisonerSupportNeedId = 104, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-03T10:36:32.713421"), updateText = "This is an update 6", status = SupportNeedStatus.MET, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 108, prisonerSupportNeedId = 104, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-03T09:36:32.713421"), updateText = "This is an update 7", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 109, prisonerSupportNeedId = 105, createdBy = "User B", createdDate = LocalDateTime.parse("2024-02-11T09:36:32.713421"), updateText = "This is an update 8", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = false, deleted = false, deletedDate = null),
    )
    Assertions.assertEquals(expectedPrisonerSupportNeedUpdates, prisonerSupportNeedUpdateRepository.findAll().sortedBy { it.id })
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-5.sql")
  fun `test post support needs - invalid request body (missing status) with rollback`() {
    val nomsId = "G4161UF"
    authedWebTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/needs")
      .bodyValue(
        PrisonerNeedsRequest(
          needs = listOf(
            PrisonerNeedRequest(
              needId = 8,
              prisonerSupportNeedId = null,
              otherDesc = null,
              text = "This is an update 1",
              status = null,
              isPrisonResponsible = true,
              isProbationResponsible = false,
            ),
          ),
        ),
      )
      .exchange()
      .expectStatus().isBadRequest

    assertNoChangesToSupportNeeds()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-5.sql")
  fun `test post support needs - invalid request body (missing isPrisonResponsible) with rollback`() {
    val nomsId = "G4161UF"
    authedWebTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/needs")
      .bodyValue(
        PrisonerNeedsRequest(
          needs = listOf(
            PrisonerNeedRequest(
              needId = 8,
              prisonerSupportNeedId = null,
              otherDesc = null,
              text = "This is an update 1",
              status = SupportNeedStatus.MET,
              isPrisonResponsible = null,
              isProbationResponsible = false,
            ),
          ),
        ),
      )
      .exchange()
      .expectStatus().isBadRequest

    assertNoChangesToSupportNeeds()
  }

  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-5.sql")
  fun `test post support needs - invalid request body (missing isProbationResponsible) with rollback`() {
    val nomsId = "G4161UF"
    authedWebTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/needs")
      .bodyValue(
        PrisonerNeedsRequest(
          needs = listOf(
            PrisonerNeedRequest(
              needId = 8,
              prisonerSupportNeedId = null,
              otherDesc = null,
              text = "This is an update 1",
              status = SupportNeedStatus.MET,
              isPrisonResponsible = true,
              isProbationResponsible = null,
            ),
          ),
        ),
      )
      .exchange()
      .expectStatus().isBadRequest

    assertNoChangesToSupportNeeds()
  }

  private fun assertNoChangesToSupportNeeds() {
    val expectedPrisonerSupportNeeds = listOf(
      PrisonerSupportNeedEntity(id = 101, prisonerId = 1, supportNeed = supportNeedRepository.findById(1).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 103),
      PrisonerSupportNeedEntity(id = 102, prisonerId = 1, supportNeed = supportNeedRepository.findById(3).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = true, deletedDate = LocalDateTime.parse("2024-02-22T09:36:28.713421"), latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 103, prisonerId = 1, supportNeed = supportNeedRepository.findById(3).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 106),
      PrisonerSupportNeedEntity(id = 104, prisonerId = 1, supportNeed = supportNeedRepository.findById(5).get(), otherDetail = "Other 1", createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 107),
      PrisonerSupportNeedEntity(id = 105, prisonerId = 1, supportNeed = supportNeedRepository.findById(5).get(), otherDetail = "Other 2", createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 109),
      PrisonerSupportNeedEntity(id = 106, prisonerId = 1, supportNeed = supportNeedRepository.findById(15).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = null),
    )
    Assertions.assertEquals(expectedPrisonerSupportNeeds, prisonerSupportNeedRepository.findAll().sortedBy { it.id })

    val expectedPrisonerSupportNeedUpdates = listOf(
      PrisonerSupportNeedUpdateEntity(id = 101, prisonerSupportNeedId = 101, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-01T09:36:32.713421"), updateText = "This is an update 1", status = SupportNeedStatus.NOT_STARTED, isPrison = false, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 102, prisonerSupportNeedId = 101, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-01T09:36:32.713421"), updateText = "Deleted update", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true, deleted = true, deletedDate = LocalDateTime.parse("2024-02-01T09:36:32.713421")),
      PrisonerSupportNeedUpdateEntity(id = 103, prisonerSupportNeedId = 101, createdBy = "User B", createdDate = LocalDateTime.parse("2024-02-03T09:36:32.713421"), updateText = "This is an update 2", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 104, prisonerSupportNeedId = 101, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-02T09:36:32.713421"), updateText = "This is an update 3", status = SupportNeedStatus.MET, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 105, prisonerSupportNeedId = 103, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-02T09:36:32.713421"), updateText = "This is an update 4", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 106, prisonerSupportNeedId = 103, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-05T09:36:32.713421"), updateText = "This is an update 5", status = SupportNeedStatus.NOT_STARTED, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 107, prisonerSupportNeedId = 104, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-03T10:36:32.713421"), updateText = "This is an update 6", status = SupportNeedStatus.MET, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 108, prisonerSupportNeedId = 104, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-03T09:36:32.713421"), updateText = "This is an update 7", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 109, prisonerSupportNeedId = 105, createdBy = "User B", createdDate = LocalDateTime.parse("2024-02-11T09:36:32.713421"), updateText = "This is an update 8", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = false, deleted = false, deletedDate = null),
    )
    Assertions.assertEquals(expectedPrisonerSupportNeedUpdates, prisonerSupportNeedUpdateRepository.findAll().sortedBy { it.id })
  }

  @Test
  fun `post support needs - no prisoner found`() {
    val nomsId = "G4161UF"
    authedWebTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/needs")
      .bodyValue(PrisonerNeedsRequest(emptyList()))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `post support needs - unauthorised`() {
    val nomsId = "G4161UF"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/needs")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `post support needs - forbidden`() {
    val nomsId = "G4161UF"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/needs")
      .bodyValue(PrisonerNeedsRequest(emptyList()))
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-5.sql")
  fun `test patch support needs - happy path`() {
    val nomsId = "G4161UF"
    val prisonerNeedId = 101
    authedWebTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/need/$prisonerNeedId")
      .bodyValue(
        SupportNeedsUpdateRequest(
          text = "This is an update 9",
          status = SupportNeedStatus.IN_PROGRESS,
          isProbationResponsible = true,
          isPrisonResponsible = true,
        ),
      )
      .exchange()
      .expectStatus().isOk

    val expectedPrisonerSupportNeeds = listOf(
      PrisonerSupportNeedEntity(id = 101, prisonerId = 1, supportNeed = supportNeedRepository.findById(1).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 1),
      PrisonerSupportNeedEntity(id = 102, prisonerId = 1, supportNeed = supportNeedRepository.findById(3).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = true, deletedDate = LocalDateTime.parse("2024-02-22T09:36:28.713421"), latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 103, prisonerId = 1, supportNeed = supportNeedRepository.findById(3).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 106),
      PrisonerSupportNeedEntity(id = 104, prisonerId = 1, supportNeed = supportNeedRepository.findById(5).get(), otherDetail = "Other 1", createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 107),
      PrisonerSupportNeedEntity(id = 105, prisonerId = 1, supportNeed = supportNeedRepository.findById(5).get(), otherDetail = "Other 2", createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = 109),
      PrisonerSupportNeedEntity(id = 106, prisonerId = 1, supportNeed = supportNeedRepository.findById(15).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = false, deletedDate = null, latestUpdateId = null),
    )
    Assertions.assertEquals(expectedPrisonerSupportNeeds, prisonerSupportNeedRepository.findAll().sortedBy { it.id })

    val expectedPrisonerSupportNeedUpdates = listOf(
      PrisonerSupportNeedUpdateEntity(id = 1, prisonerSupportNeedId = 101, createdBy = "test", createdDate = LocalDateTime.parse("2025-01-09T12:00:00"), updateText = "This is an update 9", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 101, prisonerSupportNeedId = 101, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-01T09:36:32.713421"), updateText = "This is an update 1", status = SupportNeedStatus.NOT_STARTED, isPrison = false, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 102, prisonerSupportNeedId = 101, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-01T09:36:32.713421"), updateText = "Deleted update", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = true, deleted = true, deletedDate = LocalDateTime.parse("2024-02-01T09:36:32.713421")),
      PrisonerSupportNeedUpdateEntity(id = 103, prisonerSupportNeedId = 101, createdBy = "User B", createdDate = LocalDateTime.parse("2024-02-03T09:36:32.713421"), updateText = "This is an update 2", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 104, prisonerSupportNeedId = 101, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-02T09:36:32.713421"), updateText = "This is an update 3", status = SupportNeedStatus.MET, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 105, prisonerSupportNeedId = 103, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-02T09:36:32.713421"), updateText = "This is an update 4", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 106, prisonerSupportNeedId = 103, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-05T09:36:32.713421"), updateText = "This is an update 5", status = SupportNeedStatus.NOT_STARTED, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 107, prisonerSupportNeedId = 104, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-03T10:36:32.713421"), updateText = "This is an update 6", status = SupportNeedStatus.MET, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 108, prisonerSupportNeedId = 104, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-03T09:36:32.713421"), updateText = "This is an update 7", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 109, prisonerSupportNeedId = 105, createdBy = "User B", createdDate = LocalDateTime.parse("2024-02-11T09:36:32.713421"), updateText = "This is an update 8", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = false, deleted = false, deletedDate = null),
    )
    Assertions.assertEquals(expectedPrisonerSupportNeedUpdates, prisonerSupportNeedUpdateRepository.findAll().sortedBy { it.id })
  }

  @Test
  fun `patch support needs - no prisoner found`() {
    val nomsId = "G4161UF"
    val prisonerNeedId = "101"
    authedWebTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/need/$prisonerNeedId")
      .bodyValue(SupportNeedsUpdateRequest(text = "", isPrisonResponsible = false, isProbationResponsible = false, status = SupportNeedStatus.IN_PROGRESS))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `patch support needs - unauthorised`() {
    val nomsId = "G4161UF"
    val prisonerNeedId = "101"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/need/$prisonerNeedId")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `patch support needs - forbidden`() {
    val nomsId = "G4161UF"
    val prisonerNeedId = "101"
    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/need/$prisonerNeedId")
      .bodyValue(SupportNeedsUpdateRequest(text = "", isPrisonResponsible = false, isProbationResponsible = false, status = SupportNeedStatus.IN_PROGRESS))
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }
}
