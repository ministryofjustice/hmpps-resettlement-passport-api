package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.slf4j.LoggerFactory
import org.springframework.cache.Cache
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.cache.interceptor.SimpleCacheErrorHandler
import org.springframework.context.annotation.Configuration

@Configuration
class CustomCachingConfigurer : CachingConfigurer {
  override fun errorHandler(): CacheErrorHandler = CustomCacheErrorHandler()

  class CustomCacheErrorHandler : SimpleCacheErrorHandler() {

    companion object {
      private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun handleCacheGetError(exception: RuntimeException, cache: Cache, key: Any) {
      log.warn("Error handling cache ${cache.name} get $key", exception)
    }

    override fun handleCachePutError(exception: RuntimeException, cache: Cache, key: Any, value: Any?) {
      log.warn("Error handling cache ${cache.name} put $key", exception)
    }
  }
}
