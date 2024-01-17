package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointmentAddress
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ContactType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DeliusContactRepository
import java.time.LocalDate
import java.time.LocalDateTime

class AppointmentsIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var deliusContactRepository: DeliusContactRepository

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get All Appointments happy path`() {
    val expectedOutput = readFile("testdata/expectation/appointments-1.json")
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

    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, "", 500, 0, 404)
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
  @Sql("classpath:testdata/sql/seed-pathway-statuses-6.sql")
  fun `Get All Appointments happy path - including database`() {
    val expectedOutput = readFile("testdata/expectation/appointments-2.json")
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
  @Sql("classpath:testdata/sql/seed-pathway-statuses-6.sql")
  fun `Get appointment by id`() {
    val expectedOutput = readFile("testdata/expectation/appointments-4.json")
    val nomsId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments/2")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get appointment by id - forbidden`() {
    val nomsId = "G1458GV"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments/2")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(403)
      .jsonPath("developerMessage").toString().contains("Forbidden, requires an appropriate role")
  }

  @Test
  fun `Get appointment by id - unauthorized`() {
    val nomsId = "G1458GV"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments/2")
      .exchange()
      .expectStatus().isEqualTo(401)
  }


  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-6.sql")
  fun `Get All Appointments happy path - only database`() {
    val expectedOutput = readFile("testdata/expectation/appointments-3.json")
    val nomsId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetAppointmentsFromCRNNoResults(crn)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoners-2.sql")
  fun `Create Appointment happy path`() {
    val fakeNow = LocalDateTime.parse("2023-12-17T12:00:01")
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val nomsId = "G1458GV"
    val expectedDeliusContact = listOf(
      DeliusContactEntity(
        id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "G1458GV", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), category = Category.DRUGS_AND_ALCOHOL, contactType = ContactType.APPOINTMENT, createdBy = "RESETTLEMENTPASSPORT_ADM", createdDate = fakeNow,
        notes = """
      ###
      Appointment Title: AA
      Contact: Hannah Smith
      Organisation: AA
      Location:
        Building Name: Cloth Hall
        Building Number: N/A
        Street Name: Cloth Hall Street
        District: 
        Town: Huddersfield
        County: West Yorkshire
        Postcode: HD3 5BX
      ###
      Remember to bring ID
      ###
        """.trimIndent(),
        appointmentDate = LocalDateTime.parse("2023-08-17T12:00:01"), appointmentDuration = 120,
      ),
    )
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/appointments?page=0&size=50")
      .bodyValue(
        CreateAppointment(appointmentType = Category.DRUGS_AND_ALCOHOL, appointmentTitle = "AA", organisation = "AA", contact = "Hannah Smith", location = CreateAppointmentAddress(buildingName = "Cloth Hall", buildingNumber = "N/A", streetName = "Cloth Hall Street", county = "West Yorkshire", district = "", town = "Huddersfield", postcode = "HD3 5BX"), dateAndTime = LocalDateTime.parse("2023-08-17T12:00:01"), appointmentDuration = 120, notes = "Remember to bring ID"),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(200)
    val actualDeliusContact = deliusContactRepository.findAll()
    Assertions.assertEquals(expectedDeliusContact, actualDeliusContact)
    unmockkAll()
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
        }""",
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
        CreateAppointment(appointmentType = Category.DRUGS_AND_ALCOHOL, appointmentTitle = "AA", organisation = "AA", contact = "Hannah Smith", location = CreateAppointmentAddress(buildingName = "Cloth Hall", buildingNumber = "N/A", streetName = "Cloth Hall Street", county = "West Yorkshire", district = "", town = "Huddersfield", postcode = "HD3 5BX"), dateAndTime = LocalDateTime.parse("2023-08-17T12:00:01"), appointmentDuration = 120, notes = "Remember to bring ID"),
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
        CreateAppointment(appointmentType = Category.DRUGS_AND_ALCOHOL, appointmentTitle = "AA", organisation = "AA", contact = "Hannah Smith", location = CreateAppointmentAddress(buildingName = "Cloth Hall", buildingNumber = "N/A", streetName = "Cloth Hall Street", county = "West Yorkshire", district = "", town = "Huddersfield", postcode = "HD3 5BX"), dateAndTime = LocalDateTime.parse("2023-08-17T12:00:01"), appointmentDuration = 120, notes = "Remember to bring ID"),
      )
      .exchange()
      .expectStatus().isEqualTo(401)
  }
}
