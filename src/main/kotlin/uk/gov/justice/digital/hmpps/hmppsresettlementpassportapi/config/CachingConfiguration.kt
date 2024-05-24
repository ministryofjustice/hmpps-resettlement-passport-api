package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.observation.ObservationRegistry
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@EnableCaching
@Configuration
class CachingConfiguration {

  @Bean
  fun cacheManager(connectionFactory: RedisConnectionFactory, observationRegistry: ObservationRegistry): RedisCacheManager {
    return RedisCacheManager.builder(connectionFactory)
      .withCacheConfiguration("prisoner-search-api-find-prisoners-by-search-term", getCacheConfiguration(Duration.ofMinutes(10)))
      .withCacheConfiguration("prisoner-search-api-find-prisoners-personal-details", getCacheConfiguration(Duration.ofMinutes(5)))
      .withCacheConfiguration("interventions-api-fetch-probation-case-referrals", getCacheConfiguration(Duration.ofHours(1)))
      .build()
  }

  private fun getCacheConfiguration(ttl: Duration): RedisCacheConfiguration {
    val customObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()).activateDefaultTyping(jacksonObjectMapper().polymorphicTypeValidator, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY)
    val jackson2JsonRedisSerializer = GenericJackson2JsonRedisSerializer(customObjectMapper)
    return RedisCacheConfiguration.defaultCacheConfig()
      .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
      .prefixCacheNameWith(this.javaClass.packageName + ".")
      .entryTtl(ttl)
      .disableCachingNullValues()
  }
}
