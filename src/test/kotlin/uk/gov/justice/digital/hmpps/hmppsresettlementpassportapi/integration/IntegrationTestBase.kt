package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.google.common.io.Resources
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.cache.CacheManager
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlMergeMode
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.HmppsS3Properties
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.AllocationManagerApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.ArnApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CaseNotesApiMockServer
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
@Sql(scripts = ["classpath:testdata/sql/clear-all-data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
abstract class IntegrationTestBase : TestBase() {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  lateinit var cacheManager: CacheManager

  @Autowired
  private lateinit var hmppsS3Properties: HmppsS3Properties

  @Autowired
  protected lateinit var s3Client: S3Client

  @BeforeEach
  fun beforeEach() {
    cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
  }

  companion object {

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

  internal fun bucketName() = hmppsS3Properties.buckets["document-management"]!!.bucketName

  internal fun putDocumentInS3(documentKey: String, fileResourcePath: String): ByteArray {
    val request = PutObjectRequest.builder()
      .bucket(bucketName())
      .key(documentKey)
      .build()
    val fileBytes = ClassPathResource(fileResourcePath).contentAsByteArray
    s3Client.putObject(request, RequestBody.fromBytes(fileBytes))
    return fileBytes
  }

  internal fun deleteAllDocumentsInS3() {
    val listObjectsResponse = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName()).build())

    for (s3Object in listObjectsResponse.contents()) {
      val request = DeleteObjectRequest.builder()
        .bucket(bucketName())
        .key(s3Object.key())
        .build()
      s3Client.deleteObject(request)
    }
  }
}

fun readFile(file: String): String = Resources.getResource(file).readText()
