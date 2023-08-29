package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.LicenceConditionApiService

@RestController
@Validated
@RequestMapping(
  "/resettlement-passport/prisoner",
  produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.IMAGE_JPEG_VALUE],
)
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class LicenceConditionResourceController(
  private val licenceConditionApiService: LicenceConditionApiService,
) {

  @GetMapping("/{prisonerId}/licence-condition", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Get all Licence conditions available",
    description = "All Licence Conditions Details for the given Prisoner Id.",
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
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect information provided to fetch Licence details",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No data found to fetch Licence details",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getLicenceConditionByNomisId(
    @PathVariable("prisonerId")
    @Parameter(required = true)
    prisonerId: String,
  ): LicenceConditions? {
    val licence = licenceConditionApiService.getLicenceByNomisId(prisonerId) ?: throw NoDataWithCodeFoundException(
      "Prisoner",
      prisonerId,
    )
    return licenceConditionApiService.getLicenceConditionsByLicenceId(licence.licenceId)
  }

  @GetMapping(
    "/{prisonerId}/licence-condition/id/{licenceId}/condition/{conditionId}/image",
    produces = [MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_JSON_VALUE],
  )
  @Operation(
    summary = "Get an image related to a licence condition",
    description = "Gets a jpeg image related to a licence condition, usually an exclusion zone",
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
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
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
        description = "Cannot find an image related to this condition",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  suspend fun getLicenceConditionImage(
    @PathVariable("prisonerId")
    @Parameter(required = true)
    prisonerId: String,
    @PathVariable("licenceId")
    @Parameter(required = true)
    licenceId: String,
    @PathVariable("conditionId")
    @Parameter(required = true)
    conditionId: String,
  ): Flow<ByteArray> = licenceConditionApiService.getImageFromLicenceIdAndConditionId(licenceId, conditionId)
}
