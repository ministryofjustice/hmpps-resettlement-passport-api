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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerCountMetricsByReleaseDate
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.MetricsService

@RestController
@Validated
@RequestMapping("/resettlement-passport/metrics", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class MetricsResourceController(
  private val metricsService: MetricsService,
) {

  @GetMapping("/prisoner-counts", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get metrics by prison Id", description = "Metrics data based on prison Id")
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
      ApiResponse(
        responseCode = "404",
        description = "No metrics found for given prison",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getMetrics(
    @Schema(example = "MDI", required = true, minLength = 3, maxLength = 6)
    @RequestParam("prisonId")
    @Parameter(required = true)
    prisonId: String,
  ): PrisonerCountMetricsByReleaseDate = metricsService.getMetricsByPrisonId(prisonId)
}
