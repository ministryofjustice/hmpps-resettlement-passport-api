package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.google.common.io.Resources
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlMergeMode
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.AllocationManagerApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.ArnApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CaseNotesApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CiagApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CvlApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.EducationEmploymentApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.InterventionsServiceApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.KeyWorkerApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PoPUserApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PrisonRegisterApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PrisonerSearchApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.ResettlementPassportDeliusApiMockServer

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = ["classpath:testdata/sql/clear-all-data.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
abstract class IntegrationTestBase : TestBase() {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    @JvmField
    val prisonRegisterApiMockServer = PrisonRegisterApiMockServer()

    @JvmField
    val hmppsAuthMockServer = HmppsAuthMockServer()

    @JvmField
    val cvlApiMockServer = CvlApiMockServer()

    @JvmField
    val arnApiMockServer = ArnApiMockServer()

    @JvmField
    val prisonerSearchApiMockServer = PrisonerSearchApiMockServer()

    @JvmField
    val prisonApiMockServer = PrisonApiMockServer()

    @JvmField
    val caseNotesApiMockServer = CaseNotesApiMockServer()

    @JvmField
    val keyWorkerApiMockServer = KeyWorkerApiMockServer()

    @JvmField
    val allocationManagerApiMockServer = AllocationManagerApiMockServer()

    @JvmField
    val deliusApiMockServer = ResettlementPassportDeliusApiMockServer()

    @JvmField
    val educationEmploymentApiMockServer = EducationEmploymentApiMockServer()

    @JvmField
    val ciagApiMockServer = CiagApiMockServer()

    @JvmField
    val interventionsServiceApiMockServer = InterventionsServiceApiMockServer()

    @JvmField
    val popUserApiMockServer = PoPUserApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGrantToken()
      prisonRegisterApiMockServer.start()
      cvlApiMockServer.start()
      arnApiMockServer.start()
      prisonerSearchApiMockServer.start()
      prisonApiMockServer.start()
      caseNotesApiMockServer.start()
      keyWorkerApiMockServer.start()
      allocationManagerApiMockServer.start()
      deliusApiMockServer.start()
      educationEmploymentApiMockServer.start()
      ciagApiMockServer.start()
      interventionsServiceApiMockServer.start()
      popUserApiMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      prisonRegisterApiMockServer.stop()
      hmppsAuthMockServer.stop()
      cvlApiMockServer.stop()
      arnApiMockServer.stop()
      prisonerSearchApiMockServer.stop()
      prisonApiMockServer.stop()
      caseNotesApiMockServer.stop()
      keyWorkerApiMockServer.stop()
      allocationManagerApiMockServer.stop()
      deliusApiMockServer.stop()
      educationEmploymentApiMockServer.stop()
      ciagApiMockServer.stop()
      interventionsServiceApiMockServer.stop()
    }
  }

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }
  protected fun setAuthorisation(
    user: String = "RESETTLEMENTPASSPORT_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf(),
    authSource: String = "none",
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, roles, scopes, authSource)
}

fun readFile(file: String): String = Resources.getResource(file).readText()
