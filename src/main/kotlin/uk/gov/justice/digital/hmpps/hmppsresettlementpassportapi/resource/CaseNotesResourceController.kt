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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseNotesApiService

@RestController
@Validated
@RequestMapping("/resettlement-passport/case-notes", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class CaseNotesResourceController(
  private val caseNotesService: CaseNotesApiService,
) {

  @GetMapping("/{prisonerId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get all case notes for Resettlement Passport", description = "Get all case notes for Resettlement passport of type is RESET")
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
  suspend fun getCaseNotesForPrisoner(
    @PathVariable("prisonerId")
    @Parameter(required = true)
    prisonerId: String,
    @Schema(example = "0", required = true)
    @Parameter(required = true, description = "Zero-based page index (0..N)")
    page: Int,
    @Schema(example = "10", required = true)
    @Parameter(required = true, description = "The size of the page to be returned")
    size: Int,
    @Schema(example = "pathway,DESC")
    @Parameter(required = true, description = "Sorting criteria in the format: property,(asc|desc) property supported are occurenceDateTime and pathway")
    sort: String,
  ): CaseNotesList = caseNotesService.getCaseNotesByNomisId(prisonerId, page, size, sort)
}
