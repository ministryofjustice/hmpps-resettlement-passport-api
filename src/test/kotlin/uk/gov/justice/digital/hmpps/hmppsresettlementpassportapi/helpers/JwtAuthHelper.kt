package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers

import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.Date
import java.util.UUID

private fun createKeyPair(): KeyPair {
  val gen = KeyPairGenerator.getInstance("RSA")
  gen.initialize(2048)
  return gen.generateKeyPair()
}

@Component
class JwtAuthHelper {
  private val keyPair: KeyPair = createKeyPair()

  @Bean
  fun jwtDecoder(): NimbusJwtDecoder = NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  fun setAuthorisation(
    user: String = "RESETTLEMENTPASSPORT_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf(),
    authSource: String,
  ): (HttpHeaders) -> Unit {
    val token = createJwt(
      subject = user,
      scope = scopes,
      expiryTime = Duration.ofHours(1L),
      roles = roles,
      authSource = authSource,
    )
    return { it.set(HttpHeaders.AUTHORIZATION, "Bearer $token") }
  }

  fun createJwt(
    subject: String?,
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expiryTime: Duration = Duration.ofHours(1),
    jwtId: String = UUID.randomUUID().toString(),
    authSource: String = "none",
  ): String = mutableMapOf<String, Any>()
    .also { subject?.let { subject -> it["user_name"] = subject } }
    .also { it["client_id"] = "hmpps-resettlementpassport-api" }
    .also { roles?.let { roles -> it["authorities"] = roles } }
    .also { scope?.let { scope -> it["scope"] = scope } }
    .also { authSource.let { authSource -> it["auth_source"] = authSource } }
    .also { subject?.let { subject -> it["name"] = subject } }
    .let {
      Jwts.builder()
        .id(jwtId)
        .subject(subject)
        .claims(it.toMap())
        .expiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
        .signWith(keyPair.private, Jwts.SIG.RS256)
        .compact()
    }
}
