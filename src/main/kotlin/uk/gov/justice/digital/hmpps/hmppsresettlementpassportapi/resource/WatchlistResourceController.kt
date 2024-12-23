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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.WatchlistService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class WatchlistResourceController(
  private val watchlistService: WatchlistService,
) {

  @PostMapping("/{nomsId}/watch", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create watchlist", description = "Create watchlist")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Prisoner successfully added to watchlist",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun postWatchlistByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ) = watchlistService.createWatchlist(nomsId, auth)

  @DeleteMapping("/{nomsId}/watch", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Delete watchlist", description = "Delete watchlist")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Prisoner successfully removed from watchlist",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun deleteWatchlistByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ) = watchlistService.deleteWatchlist(nomsId, auth)
}
