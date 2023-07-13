package uk.gov.justice.digital.hmpps.hmppsresettlementpassportprototypeapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kotlinx.coroutines.flow.Flow
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi.OffendersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.OffenderSearchApiService

@RestController
@Validated
@RequestMapping("/hmpps", produces = [MediaType.APPLICATION_JSON_VALUE])
class OffenderSearchResourceController(
  private val offenderSearchService: OffenderSearchApiService,
) {

  @GetMapping("/prison/{prisonId}/prisoners", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get all prisoners by search term", description = "All prisoners data based on search term")
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
  suspend fun getPrisonerbyTerm(
    @PathVariable("prisonId")
    @Parameter(required = true)
    prisonId: String,
    @RequestParam(value = "term", required = false, defaultValue = "")
    @Parameter(description = "The primary search term. Whe absent all prisoners will be returned at the prison", example = "john smith")
    term: String,
    @ParameterObject
    @PageableDefault(sort = ["lastName", "firstName", "prisonerNumber"], direction = Sort.Direction.ASC)
    pageable: Pageable,
  ): Flow<OffendersList> = offenderSearchService.getOffendersBySearchTerm(prisonId, term, pageable)
}
