package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test

class CaseNotesIntegrationTest : IntegrationTestBase() {

  @Test
  fun `Get All CaseNotes for a Prisoner  happy path`() {
    val expectedOutput = readFile("testdata/expectation/case-notes.json")
    // TODO "REPORTS" Need to be replace with "GEN" and searchSubTerm to be "RESET"
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "REPORTS", "REP_IEP", 200)
    caseNotesApiMockServer.stubGetCaseNotesNewList("G4274GN", 500, 0, 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC")
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
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get CaseNotes forbidden`() {
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get CaseNotes  Internal Error`() {
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "REPORTS", "", 500)
    caseNotesApiMockServer.stubGetCaseNotesNewList("G4274GN", 500, 0, 500)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=occurenceDateTime,DESC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get All CaseNotes for a Prisoner  happy path sort by Pathway`() {
    val expectedOutput = readFile("testdata/expectation/case-notes-sort-pathway.json")
    // TODO "REPORTS" Need to be replace with "GEN" and searchSubTerm to be "RESET"
    caseNotesApiMockServer.stubGetCaseNotesOldList("G4274GN", 500, 0, "REPORTS", "REP_IEP", 200)
    caseNotesApiMockServer.stubGetCaseNotesNewList("G4274GN", 500, 0, 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?page=0&size=10&sort=pathway,ASC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }
}
