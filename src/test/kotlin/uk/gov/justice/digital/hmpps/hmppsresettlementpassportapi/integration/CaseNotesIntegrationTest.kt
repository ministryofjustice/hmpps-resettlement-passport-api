package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class CaseNotesIntegrationTest : IntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get All CaseNotes for a Prisoner  happy path`() {
    val expectedOutput = readFile("testdata/expectation/case-notes.json")
    caseNotesApiMockServer.stubGetCaseNotesNewList("G4274GN", 500, 0, "RESET", 200)
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=15&sort=occurenceDateTime,DESC&days=0&pathwayType=All")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get CaseNotes unauthorized`() {
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC&days=0")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get CaseNotes forbidden`() {
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC&days=0")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get CaseNotes  Internal Error`() {
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 500)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC&days=0&pathwayType=All")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get All CaseNotes for a Prisoner  happy path sort by Pathway`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-sort-pathway.json")
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 200)
    caseNotesApiMockServer.stubGetCaseNotesNewList("G4274GN", 500, 0, "RESET", 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=15&sort=pathway,ASC&days=0")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get Pathway specific CaseNotes for a Prisoner happy path`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-specific-pathway.json")
    caseNotesApiMockServer.stubGetCaseNotesSpecificPathway("G4274GN", 500, 0, "RESET", "ACCOM", 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC&days=0&pathwayType=ACCOMMODATION")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get Pathway specific CaseNotes for a Prisoner Invalid Pathway`() {
    caseNotesApiMockServer.stubGetCaseNotesSpecificPathway("G4274GN", 500, 0, "RESET", "ACCOM", 404)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC&days=0&pathwayType=unknown")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(400)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  fun `Get All CaseNotes for a Prisoner when NomsId not found`() {
    caseNotesApiMockServer.stubGetCaseNotesNewList("G4274GN", 500, 0, "RESET", 404)
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 404)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC&days=0&pathwayType=All")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  @Test
  fun `Get All CaseNotes CreatedBy List for a Prisoner  happy path`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-createdby-only.json")
    caseNotesApiMockServer.stubGetCaseNotesSpecificPathway("G4274GN", 500, 0, "RESET", "ACCOM", 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN/creators/ACCOMMODATION")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get All CaseNotes CreatedBy List for a Prisoner  Invalid pathway`() {
    caseNotesApiMockServer.stubGetCaseNotesSpecificPathway("G4274GN", 500, 0, "RESET", "ACCOM", 404)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN/creators/UNKNOWN")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(400)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  fun `Get All CaseNotes CreatedBy List for a Prisoner when NomsId not found`() {
    caseNotesApiMockServer.stubGetCaseNotesSpecificPathway("G4274GN", 500, 0, "RESET", "ACCOM", 404)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN/creators/ACCOMMODATION")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get Pathway specific and Created By specific CaseNotes for a Prisoner happy path`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-specific-pathway-and-userid.json")
    caseNotesApiMockServer.stubGetCaseNotesSpecificPathway("G4274GN", 500, 0, "RESET", "ACCOM", 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC&days=0&pathwayType=ACCOMMODATION&createdByUserId=487354")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get All CaseNotes for a Prisoner have new subType  happy path`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-new-subtype.json")
    caseNotesApiMockServer.stubGetCaseNotesNewSubTypeList("G4274GN", 500, 0, "RESET", 200)
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=15&sort=occurenceDateTime,DESC&days=0&pathwayType=All")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }
}
