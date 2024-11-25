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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseAllocationService

@RestController
@Validated
@RequestMapping("/resettlement-passport/workers", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('PSFR_RESETTLEMENT_WORKER')")
class CaseAllocationResourceController(private val caseAllocationService: CaseAllocationService) {
  @PostMapping("/cases", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Assign one or more cases to a staff", description = "Assign one or more cases to a probation service officer")
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
  fun postCaseAllocations(
    @Schema(required = true)
    @RequestBody
    caseAllocation: CaseAllocation,
  ) = caseAllocationService.assignCase(caseAllocation)

  @PatchMapping("/cases", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Unassign one or more cases to a staff",
    description = "Unassign one or more cases to a probation service officer",
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
        description = "Incorrect information provided",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun removeCaseAllocation(
    @Schema(required = true)
    @RequestBody
    caseAllocation: CaseAllocation,
  ) = caseAllocationService.unAssignCase(caseAllocation)

  @GetMapping("/cases/{staffId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get all cases by staff Id", description = "All Cases assign to the given staff")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successful Operation",
      ),
      ApiResponse(
        description = "Not found",
        responseCode = "404",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
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
        description = "Incorrect information provided to perform assessment match",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getAllCaseByStaffId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("staffId")
    @Parameter(required = true)
    staffId: Int,
  ) = caseAllocationService.getAllCaseAllocationByStaffId(staffId)

  @GetMapping("/{prisonId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get Workers list", description = "Get Workers for case assign in the given prison")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successful Operation",
      ),
      ApiResponse(
        description = "Not found",
        responseCode = "404",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
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
        description = "Incorrect information provided to perform assessment match",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getAllWorkers(
    @Schema(example = "MDI", required = true)
    @PathVariable("prisonId")
    @Parameter(required = true)
    prisonId: String,
  ) = caseAllocationService.getAllResettlementWorkers(prisonId)
}
