package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentNextPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentSubmitRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.IResettlementAssessmentStrategy

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class ResettlementAssessmentController(private val resettlementAssessmentStrategies: List<IResettlementAssessmentStrategy>) {
  @PostMapping("/{nomsId}/resettlement-assessment/{pathway}/next-page", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Returns next page of resettlement assessment", description = "Returns next page of resettlement assessment")
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
    @RequestBody
    resettlementAssessment: ResettlementAssessmentRequest,
  ): ResettlementAssessmentNextPage {
    val assessmentStrategy = resettlementAssessmentStrategies.first { it.appliesTo(pathway) }
    return ResettlementAssessmentNextPage(nextPageId = assessmentStrategy.getNextPageId(resettlementAssessment, nomsId, pathway, assessmentType, currentPage))
  }

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
  ): ResettlementAssessmentResponsePage {
    val assessmentStrategy = resettlementAssessmentStrategies.first { it.appliesTo(pathway) }
    return assessmentStrategy.getPageFromId(nomsId, pathway, pageId, assessmentType)
  }

  @PostMapping("/{nomsId}/resettlement-assessment/submit", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Submits a resettlement assessment for the given nomsId and pathway", description = "Submits a resettlement assessment for the given nomsId and pathway")
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
    @RequestBody
    resettlementAssessmentSubmit: ResettlementAssessmentSubmitRequest,
  ): ResponseEntity<Void> {
    resettlementAssessmentStrategies.first { it.appliesTo(resettlementAssessmentSubmit.pathway) }.submitAssessment(nomsId, resettlementAssessmentSubmit)
    return ResponseEntity.ok().build()
  }
}
