package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers

import io.jsonwebtoken.Jwts
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.*

/**
 * Copied from [JwtAuthorisationHelper], and add more claims (name, user_id, etc)
 */
@Component
class CustomJwtAuthorisationHelper : JwtAuthorisationHelper() {
  private val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

  @Bean
  @Primary
  @ConditionalOnWebApplication(type = SERVLET)
  override fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  @Bean
  @Primary
  @ConditionalOnWebApplication(type = REACTIVE)
  override fun reactiveJwtDecoder(): ReactiveJwtDecoder = NimbusReactiveJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  fun setAuthorisationHeader(
    clientId: String = "test-ui-client-id",
    username: String,
    scope: List<String> = listOf(),
    roles: List<String> = listOf(),
    authSource: String = AuthSource.NONE.source,
    name: String? = null, // user's display name
    userId: String? = "1234",
    uuid: String? = UUID.randomUUID().toString(),
  ): (HttpHeaders) -> Unit {
    val token = createJwtAccessToken(
      clientId = clientId,
      username = username,
      scope = scope,
      roles = roles,
      authSource = authSource,
      grantType = "authorization_code",
      customClaims = mutableMapOf(
        "name" to (name ?: username),
        "user_id" to (userId ?: username),
      ).apply {
        uuid?.let { this["uuid"] = it }
      },
    )
    return { it.setBearerAuth(token) }
  }

  override fun createJwtAccessToken(
    clientId: String,
    username: String?,
    scope: List<String>?,
    roles: List<String>?,
    expiryTime: Duration,
    jwtId: String,
    authSource: String,
    grantType: String,
  ): String = createJwtAccessToken(
    clientId = clientId,
    username = username,
    scope = scope,
    roles = roles,
    expiryTime = expiryTime,
    jwtId = jwtId,
    authSource = authSource,
    grantType = grantType,
    customClaims = null,
  )

  fun createJwtAccessToken(
    username: String,
    name: String? = null,
    authSource: String = AuthSource.NOMIS.source,
  ) = createJwtAccessToken(
    username = username,
    authSource = authSource,
    customClaims = name?.let { mapOf("name" to it) },
  )

  fun createJwtAccessToken(
    clientId: String = "test-client-id",
    username: String? = null,
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expiryTime: Duration = Duration.ofHours(2),
    jwtId: String = UUID.randomUUID().toString(),
    authSource: String = "none",
    grantType: String = "client_credentials",
    customClaims: Map<String, Any>? = null,
  ): String = mutableMapOf<String, Any>(
    "sub" to (username ?: clientId),
    "client_id" to clientId,
    "auth_source" to authSource,
    "grant_type" to grantType,
  ).apply {
    username?.let { this["user_name"] = username }
    scope?.let { this["scope"] = scope }
    roles?.let {
      // ensure that all roles have a ROLE_ prefix
      this["authorities"] = roles.map { "ROLE_${it.substringAfter("ROLE_")}" }
    }
    customClaims?.forEach { (claimName, claimValue) -> this.putIfAbsent(claimName, claimValue) }
  }
    .let {
      Jwts.builder()
        .id(jwtId)
        .subject(username ?: clientId)
        .claims(it.toMap())
        .expiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
        .signWith(keyPair.private, Jwts.SIG.RS256)
        .compact()
    }
}
