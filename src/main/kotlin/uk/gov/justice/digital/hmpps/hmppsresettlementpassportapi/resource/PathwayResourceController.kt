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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatusAndCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PathwayAndStatusService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PathwayPatchService

@RestController
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
@Validated
class PathwayResourceController(
  private val pathwayAndStatusService: PathwayAndStatusService,
  private val pathwayPatchService: PathwayPatchService,
) {

  @PatchMapping("/{nomsId}/pathway")
  @Operation(
    summary = "Patch a pathway status",
    description = "Patch a pathway with a new status for a given prisoner",
  )
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
        description = "Incorrect information in request body. Check schema and try again.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Cannot find prisoner or pathway status entry to update",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @Deprecated("Use PATCH endpoint /resettlement-passport/prisoner/{nomsId}/pathway-with-case-note to update a pathway status.")
  fun patchPathwayStatus(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @RequestBody
    pathwayAndStatus: PathwayAndStatus,
  ) = pathwayAndStatusService.updatePathwayStatus(nomsId, pathwayAndStatus)

  @PatchMapping("/{nomsId}/pathway-with-case-note")
  @Operation(
    summary = "Patch a pathway status and add a case note",
    description = "Patch a pathway with a new status and add a case note for a given prisoner",
  )
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
        description = "Incorrect information in request body. Check schema and try again.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Cannot find prisoner or pathway status entry to update",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun patchPathwayStatusWithCaseNote(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @RequestBody
    pathwayStatusAndCaseNote: PathwayStatusAndCaseNote,
    @RequestHeader("Authorization")
    auth: String,
  ) = pathwayPatchService.updatePathwayStatusWithCaseNote(nomsId, pathwayStatusAndCaseNote, auth)
}
