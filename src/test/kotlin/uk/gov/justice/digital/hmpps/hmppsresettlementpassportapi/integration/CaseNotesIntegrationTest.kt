package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class CaseNotesIntegrationTest : IntegrationTestBase() {

  @AfterEach
  fun afterEach() {
    caseNotesApiMockServer.resetMappings()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get All CaseNotes for a Prisoner  happy path`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-all.json")
    caseNotesApiMockServer.stubGetCaseNotesNewList("G4274GN", 500, 0, "RESET", 200)
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=30&sort=occurenceDateTime,DESC&days=0&pathwayType=All")
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
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get CaseNotes  Internal Error from NOMIS API`() {
    // RP2-920 If the Case Notes API gives any errors we should return just the results from the database.
    val expectedOutput = readFile("testdata/expectation/case-notes-db-only.json")
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 500)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=20&sort=occurenceDateTime,DESC&days=0&pathwayType=All")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(200)
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get CaseNotes  Not Found from NOMIS API`() {
    // RP2-920 If the Case Notes API gives any errors we should return just the results from the database.
    val expectedOutput = readFile("testdata/expectation/case-notes-db-only.json")
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 404)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=20&sort=occurenceDateTime,DESC&days=0&pathwayType=All")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(200)
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-4.sql")
  fun `Get CaseNotes  No results found`() {
    // RP2-920 If there are no results we should return an empty list NOT an error
    val expectedOutput = readFile("testdata/expectation/case-notes-no-results.json")
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 404)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC&days=0&pathwayType=All")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(200)
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get All CaseNotes for a Prisoner  happy path sort by Pathway`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-sort-pathway-all.json")
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 200)
    caseNotesApiMockServer.stubGetCaseNotesNewList("G4274GN", 500, 0, "RESET", 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=30&sort=pathway,ASC&days=0")
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
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=20&sort=occurenceDateTime,DESC&days=0&pathwayType=ACCOMMODATION")
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
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=30&sort=occurenceDateTime,DESC&days=0&pathwayType=All")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-7.sql")
  fun `Get All CaseNotes for a Prisoner  happy path sort by Pathway - with duplicates`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-sort-pathway-all.json")
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "GEN", "RESET", 200)
    caseNotesApiMockServer.stubGetCaseNotesNewList("G4274GN", 500, 0, "RESET", 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=30&sort=pathway,ASC&days=0")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get All RESET CaseNotes for a Prisoner happy path - with BCST`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-with-bcst.json")
    caseNotesApiMockServer.stubGet("/case-notes/G4274GN?page=0&size=500&type=RESET", 200, "testdata/case-notes-api/case-notes-with-bcst.json")
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=30&sort=occurenceDateTime,DESC&days=0&pathwayType=All")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-5.sql")
  fun `Get specific pathway RESET CaseNotes for a Prisoner happy path - with BCST`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-with-accom-bcst.json")
    caseNotesApiMockServer.stubGetCaseNotesSpecificPathway("G4274GN", 500, 0, "RESET", "ACCOM", 200)
    caseNotesApiMockServer.stubGet("/case-notes/G4274GN?page=0&size=500&type=RESET&subType=BCST", 200, "testdata/case-notes-api/case-notes-bcst.json")

    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=30&sort=occurenceDateTime,DESC&days=0&pathwayType=ACCOMMODATION")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }
}
