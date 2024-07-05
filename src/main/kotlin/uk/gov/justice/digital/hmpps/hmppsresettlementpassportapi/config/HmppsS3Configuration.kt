package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import uk.gov.justice.hmpps.sqs.Provider
import uk.gov.justice.hmpps.sqs.findProvider
import java.net.URI

@Configuration
@EnableConfigurationProperties(HmppsS3Properties::class)
class HmppsS3Configuration(
  private val hmppsS3Properties: HmppsS3Properties,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  fun s3Client(): S3Client =
    with(hmppsS3Properties) {
      when (findProvider(provider)) {
        Provider.AWS -> awsS3Client()
        Provider.LOCALSTACK -> localstackS3Client()
      }
    }

  private fun awsS3Client() =
    with(hmppsS3Properties) {
      log.info("Creating AWS S3Client with DefaultCredentialsProvider and region '$region'")

      S3Client.builder()
        .credentialsProvider(DefaultCredentialsProvider.builder().build())
        .region(Region.of(region))
        .build()
    }

  private fun localstackS3Client(): S3Client =
    with(hmppsS3Properties) {
      log.info("Creating localstack S3Client with StaticCredentialsProvider, localstackUrl '$localstackUrl' and region '$region'")

      val client = S3Client.builder()
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("any", "any")))
        // Using localhost for other AWS clients works but throws dns errors with the S3Client, seemingly due to how the full URLs including the bucket are generated.
        // There are two solutions, force path style or use the localhost IP instead. The latter is closer to the default
        // .forcePathStyle(true)
        // .endpointOverride(URI.create(localstackUrl))
        .endpointOverride(URI.create(localstackUrl.replace("localhost", "127.0.0.1")))
        .region(Region.of(region))
        .build()

      buckets.values.onEach {
        try {
          log.info("Checking for S3 bucket '${it.bucketName}'")

          val headBucketRequest = HeadBucketRequest.builder()
            .bucket(it.bucketName)
            .build()

          client.headBucket(headBucketRequest)

          log.info("S3 bucket '${it.bucketName}' found")
        } catch (e: NoSuchBucketException) {
          log.info("Creating S3 bucket '${it.bucketName}' as it was not found")

          val bucketRequest = CreateBucketRequest.builder()
            .bucket(it.bucketName)
            .build()

          client.createBucket(bucketRequest)
        }
      }

      return client
    }
}
