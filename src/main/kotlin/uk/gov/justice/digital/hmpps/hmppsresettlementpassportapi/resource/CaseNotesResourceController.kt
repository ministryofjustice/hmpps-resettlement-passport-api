package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseNotesService

@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
@RestController
@Validated
@RequestMapping("/resettlement-passport/case-notes", produces = [MediaType.APPLICATION_JSON_VALUE])
class CaseNotesResourceController(
  private val caseNotesService: CaseNotesService,
) {

  @GetMapping("/{nomsId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Get all case notes for Resettlement Passport",
    description = "Get all case notes for Resettlement passport of type is RESET",
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
    ],
  )
  @WithSpan(kind = SpanKind.SERVER)
  fun getCaseNotesForPrisoner(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @Schema(example = "0")
    @Parameter(description = "Zero-based page index (0..N)")
    @RequestParam(value = "page", defaultValue = "0")
    page: Int,
    @Schema(example = "10")
    @Parameter(description = "The size of the page to be returned")
    @RequestParam(value = "size", defaultValue = "10")
    size: Int,
    @Schema(example = "occurrenceDateTime,DESC")
    @Parameter(description = "Sorting criteria in the format: property,(ASC|DESC) property supported are occurrenceDateTime and pathway.")
    @RequestParam(value = "sort", defaultValue = "occurrenceDateTime,DESC")
    sort: String,
    @Schema(example = "21")
    @Parameter(description = "Get Case notes from created date older than given days and till current date")
    @RequestParam(value = "days", defaultValue = "0")
    days: Int = 0,
    @Schema(example = "21")
    @Parameter(description = "Get Case notes for a specific pathway or all, property supported are All, ACCOMMODATION, ATTITUDES_THINKING_AND_BEHAVIOUR, CHILDREN_FAMILIES_AND_COMMUNITY, DRUGS_AND_ALCOHOL, EDUCATION_SKILLS_AND_WORK, FINANCE_AND_ID, HEALTH")
    @RequestParam(value = "pathwayType", defaultValue = "All")
    pathwayType: CaseNoteType,
    @Schema(example = "12345")
    @Parameter(description = "Get Case notes created by given author UserId for a specific pathway ")
    @RequestParam(value = "createdByUserId", defaultValue = "0")
    createdByUserId: Int,
  ): CaseNotesList =
    caseNotesService.getCaseNotesByNomsId(nomsId, page, size, sort, days, pathwayType, createdByUserId)

  @GetMapping("/{nomsId}/creators/{pathway}")
  @Operation(
    summary = "Get all case notes created by user list",
    description = "Get all case notes created by user list for the given pathway in Resettlement Passport",
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
    ],
  )
  @WithSpan(kind = SpanKind.SERVER)
  fun getCaseNotesCreators(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("pathway")
    @Parameter(
      required = true,
      description = "Get case notes creators for a specific pathway or all case notes, supported values are All, ACCOMMODATION, ATTITUDES_THINKING_AND_BEHAVIOUR, CHILDREN_FAMILIES_AND_COMMUNITY, DRUGS_AND_ALCOHOL, EDUCATION_SKILLS_AND_WORK, FINANCE_AND_ID, HEALTH",
    )
    pathway: CaseNoteType,
  ): List<CaseNotesMeta> = caseNotesService.getCaseNotesCreatorsByPathway(nomsId, pathway)
}
