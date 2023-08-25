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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MappaData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RiskScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RoshData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.RiskApiService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_READ_WRITE')")
class RiskResourceController(
  private val riskApiService: RiskApiService,
) {

  @GetMapping("{prisonerId}/risk/scores", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Get risk score for a prisoner",
    description = "All risk score details for the given Prisoner Id.",
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
        responseCode = "404",
        description = "No data found for risk scores",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getRiskScoresByNomsId(
    @PathVariable("prisonerId")
    @Parameter(required = true)
    prisonerId: String,
  ): RiskScore? = riskApiService.getRiskScoresByNomsId(prisonerId)

  @GetMapping("{prisonerId}/risk/rosh", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Get RoSH data for a prisoner",
    description = "RoSH data for the given Prisoner Id.",
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
        responseCode = "404",
        description = "No data found for RoSH",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getRoshDataByNomsId(
    @PathVariable("prisonerId")
    @Parameter(required = true)
    prisonerId: String,
  ): RoshData? = riskApiService.getRoshDataByNomsId(prisonerId)

  @GetMapping("{prisonerId}/risk/mappa", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Get MAPPA data for a prisoner",
    description = "MAPPA data for the given Prisoner Id.",
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
        responseCode = "404",
        description = "No data found for MAPPA",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getMappaDataByNomsId(
    @PathVariable("prisonerId")
    @Parameter(required = true)
    prisonerId: String,
  ): MappaData? = riskApiService.getMappaDataByNomsId(prisonerId)
}
