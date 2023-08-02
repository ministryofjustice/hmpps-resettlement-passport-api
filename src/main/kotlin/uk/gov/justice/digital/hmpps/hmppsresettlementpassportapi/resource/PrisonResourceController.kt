package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PrisonApiService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisons", produces = [MediaType.APPLICATION_JSON_VALUE])
class PrisonResourceController(
  private val prisonService: PrisonApiService,
) {

  @GetMapping("/all")
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
  suspend fun getPrisons(): MutableList<uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prison> = prisonService.getPrisonsList()

  @GetMapping("/active")
  @Operation(summary = "Get all active prisons", description = "All active prisons")
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
  suspend fun getPrisonsActive(): MutableList<uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prison> = prisonService.getActivePrisonsList()
}
