package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.IdApplicationPatch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.IdApplicationPost
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.SupportNeedsIntegrationTest.Companion.fakeNow
import java.math.BigDecimal

class IdApplicationIntegrationReadOnlyModeTest : ReadOnlyIntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-id-application.sql")
  fun `Create id application- forbidden`() {
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/123/idapplication")
      .bodyValue(
        IdApplicationPost(
          idType = "Birth certificate",
          applicationSubmittedDate = fakeNow,
          isPriorityApplication = false,
          costOfApplication = BigDecimal(10.50),
          haveGro = true,
          isUkNationalBornOverseas = false,
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-id-application.sql")
  fun `Update id application- forbidden`() {
    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/123/idapplication/1")
      .bodyValue(
        IdApplicationPatch(
          status = "Accepted",
          dateIdReceived = fakeNow,
          addedToPersonalItemsDate = fakeNow,
          isAddedToPersonalItems = true,
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-id-application.sql")
  fun `Delete id application- forbidden`() {
    webTestClient.delete()
      .uri("/resettlement-passport/prisoner/123/idapplication/1")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden
  }
}
