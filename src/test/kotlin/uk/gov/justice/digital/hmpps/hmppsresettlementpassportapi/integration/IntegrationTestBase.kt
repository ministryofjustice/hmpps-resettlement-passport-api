package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlMergeMode
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ReadOnlyModeTestMockConfig
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.CustomJwtAuthorisationHelper
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.AllocationManagerApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.ArnApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CaseNotesApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CuriousApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CvlApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.EducationEmploymentApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.InterventionsServiceApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.KeyWorkerApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.ManageUsersApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PoPUserApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PrisonerSearchApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.ResettlementPassportDeliusApiMockServer
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelperConfig
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@Sql(scripts = ["classpath:testdata/sql/clear-all-data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Import(SarIntegrationTestHelperConfig::class)
abstract class IntegrationTestBase : TestBase() {

  @Autowired
  lateinit var webTestClient: WebTestClient
  protected val authedWebTestClient: WebTestClient by lazy {
    webTestClient.mutateWith { builder, _, _ ->
      builder.defaultHeaders(jwtAuthorisationHelper.setAuthorisationHeader(username = "test", roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
    }
  }

  @Autowired
  protected lateinit var jwtAuthorisationHelper: CustomJwtAuthorisationHelper

  @Autowired
  protected lateinit var sarIntegrationTestHelper: SarIntegrationTestHelper

  @Autowired
  lateinit var cacheManager: CacheManager

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  @Qualifier("audit-sqs-client")
  lateinit var sqsClient: SqsAsyncClient
  lateinit var auditQueueUrl: String

  @field:Autowired
  lateinit var objectMapper: ObjectMapper

  @BeforeEach
  fun beforeEach() {
    cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
    auditQueueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("audit-queue").build()).get().queueUrl()
    sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(auditQueueUrl).build())
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

    @JvmField
    val manageUsersApiMockServer = ManageUsersApiMockServer()

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
      manageUsersApiMockServer.start()
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
      manageUsersApiMockServer.stop()
    }

    @Suppress("unused")
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
      registry.add("api.base.url.manage-users-service") { "http://localhost:${manageUsersApiMockServer.port()}" }
    }

    fun readFile(file: String): String = this::class.java.getResource("/$file")!!.readText()
  }

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  /**
   * @param user username
   * @param roles roles granted to the given user
   * @param scopes list of scopes, e.g. `read`, `write`
   * @param authSource Source of authentication, default `nomis`
   * @param name user's full name
   */
  protected fun setAuthorisation(
    user: String = "RESETTLEMENTPASSPORT_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf(),
    authSource: String = AuthSource.NOMIS.source,
    name: String? = null,
  ): (HttpHeaders) -> Unit = jwtAuthorisationHelper.setAuthorisationHeader(
    username = user,
    scope = scopes,
    roles = roles,
    authSource = authSource,
    name = name,
  )
}

@Import(ReadOnlyModeTestMockConfig::class)
abstract class ReadOnlyIntegrationTestBase : IntegrationTestBase()

fun readFile(file: String): String = IntegrationTestBase.readFile(file)
