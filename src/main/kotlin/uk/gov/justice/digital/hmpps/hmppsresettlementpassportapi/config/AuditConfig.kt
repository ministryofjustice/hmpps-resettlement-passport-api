package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
class AuditConfig {
  @Bean
  fun auditorAware(): AuditorAware<String> = UsernameAuditorAware()
}

private class UsernameAuditorAware : AuditorAware<String> {
  override fun getCurrentAuditor(): Optional<String> {
    val username = SecurityContextHolder.getContext()?.authentication?.name
    return Optional.ofNullable(username)
  }
}
