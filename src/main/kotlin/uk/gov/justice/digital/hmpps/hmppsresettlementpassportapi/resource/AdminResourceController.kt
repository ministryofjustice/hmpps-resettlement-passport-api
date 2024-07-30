package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.AdminService

@RestController
class AdminResourceController(private val adminService: AdminService) {

  @ProtectedByIngress
  @PutMapping("/retry-failed-delius-case-notes", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Retry failed delius case notes")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successful Operation",
      ),
    ],
  )
  @WithSpan(kind = SpanKind.SERVER)
  fun retryFailedDeliusCaseNotes(): ResponseEntity<Void> {
    adminService.retryFailedDeliusCaseNotes()
    return ResponseEntity.ok().build()
  }
}
