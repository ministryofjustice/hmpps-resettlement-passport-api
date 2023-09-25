package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AppointmentsList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.AppointmentsApiService
import java.time.LocalDate

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class AppointmentsController(
  private val appointmentsService: AppointmentsApiService,
) {

  @GetMapping("/{nomisId}/appointments")
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
  suspend fun getAppointments(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomisId")
    @Parameter(required = true)
    nomisId: String,
    @Schema(example = "0", required = true)
    @Parameter(required = true, description = "Zero-based page index (0..N)")
    page: Int,
    @Schema(example = "10", required = true)
    @Parameter(required = true, description = "The size of the page to be returned")
    size: Int,
  ): AppointmentsList = appointmentsService.getAppointmentsByNomisId(nomisId, LocalDate.now().minusDays(365), LocalDate.now().plusDays(365), page, size)
}
