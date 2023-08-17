package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoner
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.OffenderSearchApiService

@RestController
@Validated
@RequestMapping("/resettlement-passport", produces = [MediaType.APPLICATION_JSON_VALUE])
class OffenderSearchResourceController(
  private val offenderSearchService: OffenderSearchApiService,
) {

  @GetMapping("/prison/{prisonId}/prisoners", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get all prisoners by prison Id", description = "All prisoners data based on prison Id")
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
        responseCode = "400",
        description = "Incorrect information provided to perform prisoner match",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getPrisonersPrisonId(
    @Schema(example = "MDI", required = true, minLength = 3, maxLength = 6)
    @PathVariable("prisonId")
    @Parameter(required = true)
    prisonId: String,
    @Schema(example = "James South ", required = false)
    @Parameter(description = "The primary search term. Whe absent all prisoners will be returned at the prison")
    term: String,
    @Schema(example = "0", required = true)
    @Parameter(required = true, description = "Zero-based page index (0..N)")
    page: Int,
    @Schema(example = "10", required = true)
    @Parameter(required = true, description = "The size of the page to be returned")
    size: Int,
    @Schema(example = "releaseDate,ASC | releaseDate,DESC")
    @Parameter(required = true, description = "Sorting criteria in the format: property,(asc|desc) property supported are firstName, lastName, releaseDate and prisonerNumber")
    sort: String,
  ): PrisonersList = offenderSearchService.getPrisonersByPrisonId(false, term, prisonId, 0, page, size, sort)

  @GetMapping("/prison/{prisonId}/offenders", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get all prisoners by prison Id", description = "All prisoners data based on prison Id")
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
        responseCode = "400",
        description = "Incorrect information provided to perform prisoner match",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getOffendersbyPrisonId(
    @Schema(example = "MDI", required = true, minLength = 3, maxLength = 6)
    @PathVariable("prisonId")
    @Parameter(required = true)
    prisonId: String,
    @Schema(example = "90", required = true)
    @Parameter(description = "Number of days from today's date", required = true)
    days: Int,
    @Schema(example = "0", required = true)
    @Parameter(required = true, description = "Zero-based page index (0..N)")
    page: Int,
    @Schema(example = "10", required = true)
    @Parameter(required = true, description = "The size of the page to be returned")
    size: Int,
    @Schema(example = "releaseDate,ASC | releaseDate,DESC")
    @Parameter(required = true, description = "Sorting criteria in the format: property,(asc|desc) property supported are firstName, lastName, releaseDate and prisonerNumber")
    sort: String,
  ): PrisonersList = offenderSearchService.getPrisonersByPrisonId(true, "", prisonId, days.toLong(), page, size, sort)

  @GetMapping("/prisoner/{nomisId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get prisoner by nomis Id", description = "Prisoner Details based on nomis Id")
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
        responseCode = "400",
        description = "Incorrect information provided to perform prisoner match",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getPrisonerDetails(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomisId")
    @Parameter(required = true)
    nomisId: String,
  ): Prisoner = offenderSearchService.getPrisonerDetailsByNomisId(nomisId)
}
