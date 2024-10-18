package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.google.common.io.Resources
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.cache.CacheManager
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlMergeMode
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.AllocationManagerApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.ArnApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CaseNotesApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CuriousApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CvlApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.EducationEmploymentApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.InterventionsServiceApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.KeyWorkerApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PoPUserApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PrisonerSearchApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.ResettlementPassportDeliusApiMockServer

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = ["classpath:testdata/sql/clear-all-data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
abstract class IntegrationTestBase : TestBase() {

  @Autowired
  lateinit var webTestClient: WebTestClient
  protected val authedWebTestClient: WebTestClient by lazy {
    webTestClient
      .mutateWith { builder, _, _ ->
        builder.defaultHeader(
          HttpHeaders.AUTHORIZATION,
          "Bearer ${jwtAuthHelper.createJwt(subject = "test", roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"))}",
        )
      }
  }

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  lateinit var cacheManager: CacheManager

  @BeforeEach
  fun beforeEach() {
    cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
  }

  companion object {

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
    val interventionsServiceApiMockServer = InterventionsServiceApiMockServer()

    @JvmField
    val popUserApiMockServer = PoPUserApiMockServer()

    @JvmField
    val curiousApiMockServer = CuriousApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGrantToken()
      cvlApiMockServer.start()
      arnApiMockServer.start()
      prisonerSearchApiMockServer.start()
      prisonApiMockServer.start()
      caseNotesApiMockServer.start()
      keyWorkerApiMockServer.start()
      allocationManagerApiMockServer.start()
      deliusApiMockServer.start()
      educationEmploymentApiMockServer.start()
      interventionsServiceApiMockServer.start()
      popUserApiMockServer.start()
      curiousApiMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
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
      interventionsServiceApiMockServer.stop()
      curiousApiMockServer.stop()
    }

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      registry.add("api.base.url.oauth") { "http://localhost:${hmppsAuthMockServer.port()}" }
      registry.add("api.base.url.prisoner-search") { "http://localhost:${prisonerSearchApiMockServer.port()}" }
      registry.add("api.base.url.cvl") { "http://localhost:${cvlApiMockServer.port()}" }
      registry.add("api.base.url.arn") { "http://localhost:${arnApiMockServer.port()}" }
      registry.add("api.base.url.prison") { "http://localhost:${prisonApiMockServer.port()}" }
      registry.add("api.base.url.case-notes") { "http://localhost:${caseNotesApiMockServer.port()}" }
      registry.add("api.base.url.key-worker") { "http://localhost:${keyWorkerApiMockServer.port()}" }
      registry.add("api.base.url.allocation-manager") { "http://localhost:${allocationManagerApiMockServer.port()}" }
      registry.add("api.base.url.resettlement-passport-delius") { "http://localhost:${deliusApiMockServer.port()}" }
      registry.add("api.base.url.education-employment") { "http://localhost:${educationEmploymentApiMockServer.port()}" }
      registry.add("api.base.url.interventions-service") { "http://localhost:${interventionsServiceApiMockServer.port()}" }
      registry.add("api.base.url.pop-user-service") { "http://localhost:${popUserApiMockServer.port()}" }
      registry.add("api.base.url.curious-service") { "http://localhost:${curiousApiMockServer.port()}" }
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
