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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.aop.READ_ONLY_MODE_DISABLED
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.aop.RequiresFeature
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedsRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedsUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.SupportNeedsService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class SupportNeedsResourceController(
  private val supportNeedsService: SupportNeedsService,
  private val auditService: AuditService,
) {
  @GetMapping("/{nomsId}/needs/summary")
  @Operation(summary = "Get summary of support needs", description = "Get summary of support needs")
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
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner not found in database",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getSupportNeedsSummary(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
  ) = supportNeedsService.getNeedsSummaryByNomsId(nomsId)

  @GetMapping("/{nomsId}/needs/{pathway}/summary")
  @Operation(summary = "Get summary of support needs for a specific pathway", description = "Get summary of support needs for a specific pathway")
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
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner not found in database",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getSupportNeedsSummaryByPathway(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("pathway")
    @Parameter(required = true)
    pathway: Pathway,
  ) = supportNeedsService.getPathwayNeedsSummaryByNomsId(nomsId, pathway)

  @GetMapping("/{nomsId}/needs/{pathway}/updates")
  @Operation(summary = "Get all the support need updates for a specific pathway", description = "Get summary of support needs for a specific pathway")
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
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner not found in database",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getSupportNeedUpdatesByPathway(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("pathway")
    @Parameter(required = true)
    pathway: Pathway,
    @RequestParam("page")
    page: Int = 0,
    @RequestParam("size")
    size: Int = 10,
    @RequestParam("sort")
    @Parameter(example = "createdDate,DESC|createdDate,ASC")
    sort: String = "createdDate,DESC",
    @RequestParam("filterByPrisonerSupportNeedId")
    prisonerSupportNeedId: Long? = null,
  ) = supportNeedsService.getPathwayUpdatesByNomsId(nomsId, pathway, page, size, sort, prisonerSupportNeedId)

  @GetMapping("/{nomsId}/needs/{pathway}")
  @Operation(summary = "Get available support needs for a specific pathway", description = "Get available support needs for a specific pathway")
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
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner not found in database",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getSupportNeedsByPathway(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("pathway")
    @Parameter(required = true)
    pathway: Pathway,
  ) = supportNeedsService.getPathwayNeedsByNomsId(nomsId, pathway)

  @GetMapping("/{nomsId}/prisoner-need/{prisonerNeedId}")
  @Operation(summary = "Get details of a prisoner need including updates", description = "Get details of a prisoner need including updates")
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
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner or Prisoner Need not found in database",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getPrisonerNeedById(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("prisonerNeedId")
    @Parameter(required = true)
    prisonerNeedId: Long,
  ) = supportNeedsService.getPrisonerNeedById(nomsId, prisonerNeedId)

  @PostMapping("/{nomsId}/needs")
  @Operation(summary = "POST new support needs and updates for a prisoner", description = "POST new support needs and updates for a prisoner", deprecated = true)
  @RequiresFeature(READ_ONLY_MODE_DISABLED)
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
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner or Prisoner Need not found in database",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun postPrisonerNeedsById(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @RequestBody
    prisonerNeedsRequest: PrisonerNeedsRequest,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): ResponseEntity<Void> {
    auditService.audit(AuditAction.SUBMIT_SUPPORT_NEEDS, nomsId, auth)
    supportNeedsService.postSupportNeeds(nomsId, prisonerNeedsRequest, auth)
    return ResponseEntity.ok().build()
  }

  @PatchMapping("/{nomsId}/need/{prisonerNeedId}")
  @Operation(summary = "Update an existing support need", description = "Update the status of an existing support need", deprecated = true)
  @RequiresFeature(READ_ONLY_MODE_DISABLED)
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
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner or Prisoner Need not found in database",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun patchSupportNeedById(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("prisonerNeedId")
    @Parameter(required = true)
    prisonerNeedId: Long,
    @RequestBody
    supportNeedsUpdateRequest: SupportNeedsUpdateRequest,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): ResponseEntity<Void> {
    auditService.audit(AuditAction.UPDATE_SUPPORT_NEED, nomsId, auth)
    supportNeedsService.patchPrisonerNeedById(nomsId, prisonerNeedId, supportNeedsUpdateRequest, auth)
    return ResponseEntity.ok().build()
  }
}
