package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AppointmentsList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.AppointmentsService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService
import java.time.LocalDate

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class AppointmentsResourceController(
  private val appointmentsService: AppointmentsService,
  private val auditService: AuditService,
) {

  @GetMapping("/{nomsId}/appointments")
  @Operation(summary = "Get all appointments", description = "All Appointments for the prisoner")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successful Operation",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getAppointments(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @Schema(example = "false", required = true)
    @Parameter(required = true, description = "All or Future only appointment flag")
    @RequestParam(defaultValue = "true")
    futureOnly: Boolean,
    @RequestParam(defaultValue = "true", required = false)
    includePreRelease: Boolean,
  ): AppointmentsList = if (futureOnly) {
    appointmentsService.getAppointmentsByNomsId(nomsId, LocalDate.now(), LocalDate.now().plusDays(365), includePreRelease)
  } else {
    appointmentsService.getAppointmentsByNomsId(nomsId, LocalDate.now().minusDays(365), LocalDate.now().plusDays(365), includePreRelease)
  }

  @PostMapping("/{nomsId}/appointments", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create appointment", description = "Create appointment")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successful Operation",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect information provided",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun postAppointmentsByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestBody
    appointment: CreateAppointment,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): ResponseEntity<Void> {
    auditService.audit(AuditAction.CREATE_APPOINTMENT, nomsId, auth, null)
    return appointmentsService.createAppointment(appointment, nomsId, auth)
  }
}
