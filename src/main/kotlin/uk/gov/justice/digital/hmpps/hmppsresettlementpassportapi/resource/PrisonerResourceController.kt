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
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoner
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PrisonerService

@RestController
@Validated
@RequestMapping("/resettlement-passport", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.IMAGE_JPEG_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class PrisonerResourceController(
  private val prisonerService: PrisonerService,
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
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect input options provided",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @WithSpan(kind = SpanKind.SERVER)
  fun getPrisonersByPrisonId(
    @Schema(example = "MDI", required = true, minLength = 3, maxLength = 6)
    @PathVariable("prisonId")
    @Parameter(required = true)
    prisonId: String,
    @Schema(example = "James South ", required = false)
    @Parameter(description = "The primary search term. Whe absent all prisoners will be returned at the prison")
    term: String?,
    @Schema(example = "0", required = true)
    @Parameter(required = true, description = "Zero-based page index (0..N)")
    @RequestParam(value = "page", defaultValue = "0")
    page: Int,
    @Schema(example = "10", required = true)
    @Parameter(required = true, description = "The size of the page to be returned")
    @RequestParam(value = "size", defaultValue = "10")
    size: Int,
    @Schema(example = "releaseDate,ASC | releaseDate,DESC")
    @Parameter(
      required = true,
      description = "Sorting criteria in the format: property,(asc|desc) property supported are name, releaseDate, paroleEligibilityDate, pathwayStatus (only when pathwayView is set), releaseOnTemporaryLicenceDate and prisonerNumber",
    )
    @RequestParam(value = "sort", defaultValue = "releaseDate,ASC")
    sort: String,
    @Schema(example = "21")
    @Parameter(description = "Prisoners released (release date) within the given days from current date")
    @RequestParam(value = "days", defaultValue = "0")
    days: Int = 0,
    @Schema(example = "ACCOMMODATION")
    @Parameter(description = "Pathway to filter statuses to")
    @RequestParam(value = "pathwayView")
    pathwayView: Pathway?,
    @Schema(example = "IN_PROGRESS")
    @Parameter(description = "Status to filter the results on. This can only be used if pathwayView is provided.")
    @RequestParam(value = "pathwayStatus")
    pathwayStatus: Status?,
    @Parameter(description = "Filter on assessmentRequired. Cannot be used if pathwayView is provided.")
    @RequestParam(value = "assessmentRequired")
    assessmentRequired: Boolean?,
    @Parameter(description = "Prisoners in watchList.")
    @RequestParam(value = "watchList")
    watchList: Boolean?,
    @RequestHeader("Authorization")
    auth: String,
  ): PrisonersList = prisonerService.getPrisonersByPrisonId(term, prisonId, days, pathwayView, pathwayStatus, assessmentRequired, page, size, sort, watchList, auth)

  @GetMapping("/prisoner/{nomsId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get prisoner by noms Id", description = "Prisoner Details based on noms Id")
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
        description = "Incorrect information provided to perform prisoner match",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getPrisonerDetails(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @RequestHeader("Authorization")
    auth: String,
  ): Prisoner = prisonerService.getPrisonerDetailsByNomsId(nomsId, auth)

  @GetMapping(
    "/prisoner/{nomsId}/image/{id}",
    produces = [MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_JSON_VALUE],
  )
  @Operation(
    summary = "Get an image related to a prisoner noms Id",
    description = "Gets a jpeg image related to a  prisoner noms id, usually the latest image captured",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successful Operation",
        content = [
          Content(mediaType = MediaType.IMAGE_JPEG_VALUE),
        ],
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
        description = "Incorrect information provided to perform prisoner match",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getPrisonerImage(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("id")
    @Parameter(required = true)
    id: Int,
  ): ByteArray? = prisonerService.getPrisonerImageData(nomsId, id)
}
