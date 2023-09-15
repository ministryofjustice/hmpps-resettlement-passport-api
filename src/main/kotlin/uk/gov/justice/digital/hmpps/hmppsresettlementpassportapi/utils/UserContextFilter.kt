package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.utils

import lombok.extern.slf4j.Slf4j
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.Filter
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.http.HttpServletRequest

@Component
@Slf4j
@Order(4)
class UserContextFilter : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpServletRequest = servletRequest as HttpServletRequest
        val authToken = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)
        UserContext.authToken = authToken
        filterChain.doFilter(httpServletRequest, servletResponse)
    }

    override fun init(filterConfig: FilterConfig) {}
    override fun destroy() {}
}
