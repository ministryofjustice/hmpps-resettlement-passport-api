package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = false)
@EnableJpaRepositories("uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository")
class ResourceServerConfiguration {

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    return http
      .csrf { it.disable() } // crst not needed an rest api
      .authorizeExchange {
        it.pathMatchers(
          "/webjars/**", "/favicon.ico", "/csrf",
          "/health/**", "/info", "prometheus", "/h2-console/**", "/prototype/**",
          "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
        ).permitAll()
          .anyExchange().authenticated()
      }
      .oauth2ResourceServer { it.jwt().jwtAuthenticationConverter(AuthAwareTokenConverter()) }
      .build()
  }
}
