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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ProfileReset
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettlementAssessmentResetService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class ProfileResetController(private val resettlementAssessmentResetService: ResettlementAssessmentResetService, private val auditService: AuditService) {
  @PostMapping("/{prisonerId}/reset-profile")
  @Operation(summary = "Reset a profile", description = "Resets a prisoner's profile by removing any resettlement assessments and resetting statuses to NOT_STARTED. Also sends a case note with reason to DPS.")
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
        responseCode = "404",
        description = "Prisoner cannot be found in database",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun resetProfile(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("prisonerId")
    @Parameter(required = true)
    prisonerId: String,
    @Parameter
    supportNeedsEnabled: Boolean = false,
    @RequestBody
    profileReset: ProfileReset,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): ResponseEntity<Void> {
    auditService.audit(AuditAction.RESET_PROFILE, prisonerId, auth, null)
    resettlementAssessmentResetService.resetProfile(prisonerId, profileReset, auth, supportNeedsEnabled)
    return ResponseEntity.ok().build()
  }
}
