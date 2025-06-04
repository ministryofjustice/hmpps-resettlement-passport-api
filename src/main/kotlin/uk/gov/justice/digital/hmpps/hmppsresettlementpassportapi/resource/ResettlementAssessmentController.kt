package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.aop.READ_ONLY_MODE_DISABLED
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.aop.RequiresFeature
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.AssessmentSkipRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.LatestResettlementAssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNextPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentSubmitResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettlementAssessmentService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditDetails
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.ResettlementAssessmentStrategy

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class ResettlementAssessmentController(
  private val resettlementAssessmentStrategy: ResettlementAssessmentStrategy,
  private val resettlementAssessmentService: ResettlementAssessmentService,
  private val auditService: AuditService,
) {
  @PostMapping("/{nomsId}/resettlement-assessment/{pathway}/next-page", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Returns next page of resettlement assessment", description = "Returns next page of resettlement assessment")
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
  fun postGetNextAssessmentPage(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("pathway")
    pathway: Pathway,
    @RequestParam("assessmentType")
    assessmentType: ResettlementAssessmentType,
    @RequestParam("currentPage")
    currentPage: String?,
    @RequestParam("version")
    version: Int = 1,
    @RequestBody
    resettlementAssessment: ResettlementAssessmentRequest,
  ): ResettlementAssessmentNextPage = ResettlementAssessmentNextPage(nextPageId = resettlementAssessmentStrategy.getNextPageId(resettlementAssessment, nomsId, pathway, assessmentType, currentPage, version))

  @GetMapping("/{nomsId}/resettlement-assessment/{pathway}/page/{pageId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Returns a page of resettlement assessment with any existing answers filled in", description = "Returns a page of resettlement assessment with any existing answers filled in")
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
  fun getAssessmentPage(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("pathway")
    pathway: Pathway,
    @PathVariable("pageId")
    pageId: String,
    @RequestParam("assessmentType")
    assessmentType: ResettlementAssessmentType,
    @RequestParam("version")
    version: Int = 1,
  ): ResettlementAssessmentResponsePage = resettlementAssessmentStrategy.getPageFromId(nomsId, pathway, pageId, assessmentType, version)

  @GetMapping("/{nomsId}/resettlement-assessment/summary", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns summary of prisoner's resettlement assessment",
    description = "Returns summary of prisoner's resettlement assessment",
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

      ApiResponse(
        responseCode = "404",
        description = "Cannot find prisoner or pathway status entry to update",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getResettlementAssessmentSummaryByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @RequestParam("assessmentType", required = false, defaultValue = "BCST2")
    assessmentType: ResettlementAssessmentType,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): List<PrisonerResettlementAssessment> {
    auditService.audit(AuditAction.GET_ASSESSMENT_SUMMARY, nomsId, auth, buildDetails(assessmentType, null))
    return resettlementAssessmentService.getResettlementAssessmentSummaryByNomsId(nomsId, assessmentType)
  }

  @PostMapping("/{nomsId}/resettlement-assessment/{pathway}/complete", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Completes a resettlement assessment for the given nomsId and pathway", description = "Completes a resettlement assessment for the given nomsId and pathway")
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
  fun postCompleteAssessmentByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("pathway")
    pathway: Pathway,
    @RequestBody
    resettlementAssessmentCompleteRequest: ResettlementAssessmentCompleteRequest,
    @RequestParam("assessmentType")
    assessmentType: ResettlementAssessmentType,
    @RequestParam("declaration")
    declaration: Boolean = false,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): ResponseEntity<Void> {
    auditService.audit(AuditAction.COMPLETE_ASSESSMENT, nomsId, auth, buildDetails(assessmentType, pathway))
    resettlementAssessmentStrategy.completeAssessment(nomsId, pathway, assessmentType, resettlementAssessmentCompleteRequest, auth, declaration)
    return ResponseEntity.ok().build()
  }

  @PostMapping("/{nomsId}/resettlement-assessment/{pathway}/validate", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Validates the given resettlement assessment", description = "Validates the given resettlement assessment")
  @RequiresFeature(READ_ONLY_MODE_DISABLED)
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Report information is valid",
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
        description = "Report information is invalid",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getValidateAssessmentByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("pathway")
    pathway: Pathway,
    @RequestBody
    resettlementAssessmentCompleteRequest: ResettlementAssessmentCompleteRequest,
    @RequestParam("assessmentType")
    assessmentType: ResettlementAssessmentType,
  ): ResponseEntity<Void> {
    resettlementAssessmentStrategy.validateAssessment(nomsId, pathway, assessmentType, resettlementAssessmentCompleteRequest)
    return ResponseEntity.ok().build()
  }

  @PostMapping("/{nomsId}/resettlement-assessment/submit", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Submit a completed resettlement assessment for the given nomsId", description = "Submit a completed resettlement assessment for the given nomsId")
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
  fun postSubmitAssessmentByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestParam("assessmentType")
    assessmentType: ResettlementAssessmentType,
    @RequestParam("useNewDeliusCaseNoteFormat")
    useNewDeliusCaseNoteFormat: Boolean = false,
    @RequestParam("useNewDpsCaseNoteFormat")
    useNewDpsCaseNoteFormat: Boolean = false,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
    @RequestParam("supportNeedsLegacyProfile")
    supportNeedsLegacyProfile: Boolean = true,
  ): ResettlementAssessmentSubmitResponse {
    auditService.audit(AuditAction.SUBMIT_ASSESSMENT, nomsId, auth, buildDetails(assessmentType, null))
    return resettlementAssessmentService.submitResettlementAssessmentByNomsId(nomsId, assessmentType, useNewDeliusCaseNoteFormat, useNewDpsCaseNoteFormat, auth, resettlementAssessmentStrategy, supportNeedsLegacyProfile)
  }

  @GetMapping("/{nomsId}/resettlement-assessment/{pathway}/latest", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns the latest submitted version of prisoner's resettlement assessment",
    description = "Returns the latest submitted version of prisoner's resettlement assessment",
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
      ApiResponse(
        responseCode = "404",
        description = "Cannot find prisoner or pathway status entry to update",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getResettlementAssessmentByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("pathway")
    @Parameter(required = true)
    pathway: Pathway,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): LatestResettlementAssessmentResponse {
    auditService.audit(AuditAction.GET_ASSESSMENT, nomsId, auth, buildDetails(null, pathway))
    return resettlementAssessmentService.getLatestResettlementAssessmentByNomsIdAndPathway(nomsId, pathway, resettlementAssessmentStrategy)
  }

  @PostMapping("/{nomsId}/resettlement-assessment/skip", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Skip an assessment")
  @RequiresFeature(READ_ONLY_MODE_DISABLED)
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
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
  fun postSkipAssessmentByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestParam("assessmentType", defaultValue = "BCST2", required = false)
    assessmentType: ResettlementAssessmentType,
    @RequestBody
    request: AssessmentSkipRequest,
  ): ResponseEntity<Void> {
    resettlementAssessmentService.skipAssessment(nomsId, assessmentType, request)
    return ResponseEntity.noContent().build()
  }

  @GetMapping("/{nomsId}/resettlement-assessment/{pathway}/version", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns the question set version of the latest resettlement assessment or null if no assessment exists.",
    description = "Returns the question set version of the latest resettlement assessment or null if no assessment exists.",
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

      ApiResponse(
        responseCode = "404",
        description = "Cannot find prisoner",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getLatestResettlementAssessmentVersionByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("pathway")
    @Parameter(required = true)
    pathway: Pathway,
    @RequestParam("assessmentType")
    assessmentType: ResettlementAssessmentType,
  ) = resettlementAssessmentStrategy.getLatestResettlementAssessmentVersion(nomsId, assessmentType, pathway)
}

private fun buildDetails(assessmentType: ResettlementAssessmentType?, pathway: Pathway?): String = Json.encodeToString(AuditDetails(assessmentType, pathway))
