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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.LicenceConditionApiService

@RestController
@Validated
@RequestMapping("/resettlement-passport", produces = [MediaType.APPLICATION_JSON_VALUE])
class LicenceConditionsResourceController(
  private val licenceConditionService: LicenceConditionApiService,
) {

  @GetMapping("/{offenderId}/licence-condition", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get all Licence conditions available", description = "All Licence Conditions Details for the given Prisoner Id.")
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
        description = "Incorrect information provided to fetch Licence details",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getLicenceConditionByNomisId(
    @PathVariable("offenderId")
    @Parameter(required = true)
    offenderId: String,
  ): LicenceConditions? {

    val licence = licenceConditionService.getLicenceByNomisId(offenderId)?:throw NoDataWithCodeFoundException("Offender ", offenderId)
    if (licence != null) {
      return licenceConditionService.getLicenceConditionsByLicenceId(licence.licenceId)
    };
    return null;

  }
}
