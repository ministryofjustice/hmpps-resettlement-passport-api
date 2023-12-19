package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplication
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointmentAddress
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import java.time.LocalDateTime

class AppointmentsIntegrationTest : IntegrationTestBase() {
  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get All Appointments happy path`() {
    val expectedOutput = readFile("testdata/expectation/appointments.json")
    val nomsId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get All Appointments unauthorized`() {
    val nomsId = "G1458GV"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get All Appointments forbidden`() {
    val nomsId = "G1458GV"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get All Appointments  Internal Error`() {
    val nomsId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 500)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get All Appointments  negative Page number`() {
    val nomsId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=-1&size=50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("No Data found")
  }

  @Test
  fun `Get All Prisoners  negative Page size`() {
    val nomsId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=-50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("No Data found")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get All Prisoners with no page and no size as Internal Error`() {
    val nomsId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 500)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get Appointments when nomsId not found`() {
    val prisonId = "abc"

    offenderSearchApiMockServer.stubGetPrisonersList(prisonId, "", 500, 0, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=0&size=10&sort=xxxxx")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoners-2.sql")
  fun `Create Appointment happy path`() {
    val nomsId = "G1458GV"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .bodyValue(
        CreateAppointment(appointmentType= Category.DRUGS_AND_ALCOHOL, appointmentTitle = "AA", organisation = "AA", contact = "Hannah Smith", location= CreateAppointmentAddress(buildingName = "Cloth Hall", buildingNumber = "N/A", streetName = "Cloth Hall Street", county = "West Yorkshire", district = "", town = "Huddersfield", postcode = "HD3 5BX"), dateAndTime = LocalDateTime.parse("2023-08-17T12:00:01"), appointmentDuration = 120, notes= "Remember to bring ID"),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(200)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoners-2.sql")
  fun `Create Appointment invalid input`() {
    val nomsId = "G1458GV"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """{
          "appointmentTyp": "DRUGS_AND_ALCOHOL",
          "appointmentTitle": "rehab",
          "organisation": "AA",
          "contact": "Emily",
          "location": {
          "buildingName": "",
          "buildingNumber": "8",
          "streetName": "Hayes Court",
          "district": "",
          "town": "Huddersfield",
          "county": "West Yorks",
          "postcode": "HD1 4ST"
        },
          "dateAndTime": "2023-12-14T18:13:00",
          "appointmentDuration": 2,
          "notes": "Notes for testing"
        }"""
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(400)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
      .jsonPath("developerMessage").toString().contains("Incorrect information provided")
  }

  @Test
  fun `Post appointment- forbidden`() {
    val nomsId = "G1458GV"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .bodyValue(
        CreateAppointment(appointmentType= Category.DRUGS_AND_ALCOHOL, appointmentTitle = "AA", organisation = "AA", contact = "Hannah Smith", location= CreateAppointmentAddress(buildingName = "Cloth Hall", buildingNumber = "N/A", streetName = "Cloth Hall Street", county = "West Yorkshire", district = "", town = "Huddersfield", postcode = "HD3 5BX"), dateAndTime = LocalDateTime.parse("2023-08-17T12:00:01"), appointmentDuration = 120, notes= "Remember to bring ID"),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(403)
      .jsonPath("developerMessage").toString().contains("Forbidden, requires an appropriate role")
  }

  @Test
  fun `Post appointment- unauthorized`() {
    val nomsId = "G1458GV"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .bodyValue(
        CreateAppointment(appointmentType= Category.DRUGS_AND_ALCOHOL, appointmentTitle = "AA", organisation = "AA", contact = "Hannah Smith", location= CreateAppointmentAddress(buildingName = "Cloth Hall", buildingNumber = "N/A", streetName = "Cloth Hall Street", county = "West Yorkshire", district = "", town = "Huddersfield", postcode = "HD3 5BX"), dateAndTime = LocalDateTime.parse("2023-08-17T12:00:01"), appointmentDuration = 120, notes= "Remember to bring ID"),
      )
      .exchange()
      .expectStatus().isEqualTo(401)
  }

}
