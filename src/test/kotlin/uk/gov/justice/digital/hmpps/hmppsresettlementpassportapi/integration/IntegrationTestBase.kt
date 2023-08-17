package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.ArnApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CommunityApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CvlApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.OffenderSearchApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.PrisonApiMockServer

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
    @JvmField
    val prisonApiMockServer = PrisonApiMockServer()

    @JvmField
    val hmppsAuthMockServer = HmppsAuthMockServer()

    @JvmField
    val cvlApiMockServer = CvlApiMockServer()

    @JvmField
    val communityApiMockServer = CommunityApiMockServer()

    @JvmField
    val arnApiMockServer = ArnApiMockServer()

    @JvmField
    val offenderSearchApiMockServer = OffenderSearchApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGrantToken()
      prisonApiMockServer.start()
      cvlApiMockServer.start()
      communityApiMockServer.start()
      arnApiMockServer.start()
      offenderSearchApiMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      prisonApiMockServer.stop()
      hmppsAuthMockServer.stop()
      cvlApiMockServer.stop()
      communityApiMockServer.stop()
      arnApiMockServer.stop()
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
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, roles, scopes)

  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  fun readFile(file: String): String = this.javaClass.getResource(file).readText()
}
