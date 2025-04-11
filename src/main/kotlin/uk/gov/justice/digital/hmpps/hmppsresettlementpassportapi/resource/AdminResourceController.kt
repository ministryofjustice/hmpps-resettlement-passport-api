package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.AdminService

@RestController
class AdminResourceController(private val adminService: AdminService) {

  @ProtectedByIngress
  @PostMapping("/send-metrics", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Send metrics to app insights")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successful Operation",
      ),
    ],
  )
  fun sendMetricsToAppInsights(): ResponseEntity<Void> {
    adminService.sendMetricsToAppInsights()
    return ResponseEntity.ok().build()
  }
}
