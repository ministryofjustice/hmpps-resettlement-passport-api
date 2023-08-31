package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test

class CaseNotesIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Get All CaseNotes for a Prisoner happy path`() {
    val expectedOutput = readFile("testdata/expectation/case-notes.json")
    caseNotesApiMockServer.stubGetCaseNotesList("G4274GN", 10, 0, 200)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?size=10&page=0")
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
      .uri("/resettlement-passport/case-notes/G4274GN?size=10&page=0")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get CaseNotes forbidden`() {
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?size=10&page=0")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get CaseNotes  Internal Error`() {
    caseNotesApiMockServer.stubGetCaseNotesList("G4274GN", 10, 0, 500)
    webTestClient.get()
      .uri("/resettlement-passport/case-notes/G4274GN?size=10&page=0")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }
}
