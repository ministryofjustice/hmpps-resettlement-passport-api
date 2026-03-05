package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.boot.info.BuildProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.kotlinModule
import java.math.BigDecimal
import java.time.Duration

@EnableCaching
@Configuration
class CachingConfiguration(private val buildProperties: BuildProperties) {
  private val jacksonJsonRedisSerializer by lazy {
    GenericJacksonJsonRedisSerializer.builder().customize {
      val subtypeValidator = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType("uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi")
        .allowIfSubType(Collection::class.java)
        .allowIfSubType(Map::class.java)
        .allowIfSubType(BigDecimal::class.java)
        .build()

      it.addModule(kotlinModule())
        .activateDefaultTyping(subtypeValidator, DefaultTyping.NON_FINAL_AND_RECORDS, JsonTypeInfo.As.PROPERTY)
    }.build()
  }
  private val byteArraySerializer by lazy { RedisSerializer.byteArray() }

  @Bean
  fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager = RedisCacheManager.builder(connectionFactory)
    .withCacheConfiguration("prisoner-search-api-find-prisoners-by-search-term", getCacheConfiguration(Duration.ofMinutes(10)))
    .withCacheConfiguration("prisoner-search-api-find-prisoners-personal-details", getCacheConfiguration(Duration.ofMinutes(5)))
    .withCacheConfiguration("interventions-api-fetch-probation-case-referrals", getCacheConfiguration(Duration.ofHours(1)))
    .withCacheConfiguration("allocation-manager-api-get-poms-by-noms-id", getCacheConfiguration(Duration.ofHours(2)))
    .withCacheConfiguration("arn-api-get-risk-scores-by-crn", getCacheConfiguration(Duration.ofHours(4)))
    .withCacheConfiguration("arn-api-get-rosh-data-by-crn", getCacheConfiguration(Duration.ofHours(4)))
    .withCacheConfiguration("education-employment-api-get-readiness-profile-by-noms-id", getCacheConfiguration(Duration.ofHours(2)))
    .withCacheConfiguration("key-worker-api-get-key-worker-name", getCacheConfiguration(Duration.ofHours(2)))
    .withCacheConfiguration("prison-api-get-prisoner-image-data", getCacheConfigurationByteArray(Duration.ofMinutes(30)))
    .withCacheConfiguration("prison-api-find-prisoner-image-details", getCacheConfiguration(Duration.ofMinutes(30)))
    .withCacheConfiguration("resettlement-passport-delius-api-get-crn", getCacheConfiguration(Duration.ofHours(1)))
    .withCacheConfiguration("resettlement-passport-delius-api-get-mappa-data-by-noms-id", getCacheConfiguration(Duration.ofHours(4)))
    .withCacheConfiguration("resettlement-passport-delius-api-get-com-by-noms-id", getCacheConfiguration(Duration.ofHours(2)))
    .withCacheConfiguration("resettlement-passport-delius-api-fetch-accommodation", getCacheConfiguration(Duration.ofHours(1)))
    .withCacheConfiguration("resettlement-passport-delius-api-get-personal-details", getCacheConfiguration(Duration.ofHours(1)))
    .withCacheConfiguration("curious-api-get-learner-education-by-noms-id", getCacheConfiguration(Duration.ofHours(1)))
    .withCacheConfiguration("prisoner-search-api-match-prisoners", getCacheConfiguration(Duration.ofMinutes(10)))
    .build()

  private fun getCacheConfiguration(
    ttl: Duration,
    redisSerializer: RedisSerializer<*> = jacksonJsonRedisSerializer,
  ): RedisCacheConfiguration {
    val valueSerializationPair = RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer)

    return RedisCacheConfiguration.defaultCacheConfig()
      .serializeValuesWith(valueSerializationPair)
      .prefixCacheNameWith("${buildProperties.version}-")
      .entryTtl(ttl)
  }

  private fun getCacheConfigurationByteArray(ttl: Duration) = getCacheConfiguration(ttl, byteArraySerializer)
}
