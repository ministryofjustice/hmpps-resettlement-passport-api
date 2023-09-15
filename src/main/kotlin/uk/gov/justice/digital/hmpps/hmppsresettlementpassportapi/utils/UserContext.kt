package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.utils

import org.springframework.stereotype.Component

@Component
class UserContext {
  companion object {
    var authToken: Any? = ThreadLocal<String>()
      get() = field.toString()
      set(aToken) {
        authToken = aToken
      }
  }

}
