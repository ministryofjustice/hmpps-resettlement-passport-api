package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "hmpps.s3")
data class HmppsS3Properties(
  val provider: String = "aws",
  val region: String = "eu-west-2",
  val localstackUrl: String = "http://localhost:4566",
  val buckets: Map<String, BucketConfig> = mapOf(),
) {
  data class BucketConfig(
    val bucketName: String,
    val bucketArn: String = "",
  )
}
