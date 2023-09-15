package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.utils

import lombok.extern.slf4j.Slf4j
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.SecurityUserContext
import java.io.IOException
import javax.servlet.*

@Slf4j
@Component
@Order(1)
class UserMdcFilter @Autowired constructor(private val securityUserContext: SecurityUserContext) : Filter {
    override fun init(filterConfig: FilterConfig) {
        // Initialise - no functionality
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val currentUsername = securityUserContext.getCurrentUsername()
        try {
            currentUsername.ifPresent { u: String? -> MDC.put(USER_ID_HEADER, u) }
            chain.doFilter(request, response)
        } finally {
            currentUsername.ifPresent { u: String? -> MDC.remove(USER_ID_HEADER) }
        }
    }

    override fun destroy() {
        // Destroy - no functionality
    }

    companion object {
        private const val USER_ID_HEADER = "userId"
    }
}
