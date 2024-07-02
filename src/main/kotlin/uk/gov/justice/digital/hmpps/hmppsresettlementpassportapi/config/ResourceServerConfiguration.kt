package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@EnableJpaRepositories("uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi")
class ResourceServerConfiguration {

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      csrf { disable() }
      authorizeHttpRequests {
        listOf(
          "/webjars/**", "/favicon.ico", "/csrf",
          "/health/**", "/info", "/prometheus", "/h2-console/**", "/prototype/**",
          "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/retry-failed-delius-case-notes",
        ).forEach { authorize(it, permitAll) }
        authorize("/queue-admin/retry-all-dlqs", hasRole("RESETTLEMENT_PASSPORT_EDIT"))
        authorize(anyRequest, authenticated)
      }
      oauth2ResourceServer { jwt { jwtAuthenticationConverter = AuthAwareTokenConverter() } }
    }
    return http.build()
  }
}
