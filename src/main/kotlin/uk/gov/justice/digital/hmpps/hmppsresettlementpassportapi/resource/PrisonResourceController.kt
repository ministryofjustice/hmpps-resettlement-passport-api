package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi.Prison
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PrisonApiService

@RestController
@Validated
@RequestMapping("/hmpps", produces = [MediaType.APPLICATION_JSON_VALUE])
class PrisonResourceController(
  private val prisonService: PrisonApiService,
) {

  @GetMapping("/prisons")
  @Operation(summary = "Get all prisons", description = "All prisons")
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
  suspend fun getPrisons(): List<Prison> = prisonService.getPrisons(true)

  @GetMapping("/prisons/id/{prisonId}")
  @Operation(summary = "Get a prison", description = "A prison data")
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
  suspend fun getPrisonbyId(
    @PathVariable
    prisonId: String,
  ): Prison = prisonService.getPrisonById(true, prisonId)

  @GetMapping("/prisons/id/{prisonId}/videolink-conferencing-centre/email-address")
  @Operation(summary = "Get a prison Videolink Conferencing centre email address", description = "A prison Videlolink Conference center email address")
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
        responseCode = "404",
        description = "Data Not Found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "500",
        description = "Data Not Found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getPrisonVideolinkConferenceCentreEmailAddress(
    @PathVariable
    prisonId: String,
  ): String = prisonService.getPrisonVideolinkConferenceCentreEmailAddress(true, prisonId)
}
