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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.idapplicationapi.IdApplicationPatchDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.idapplicationapi.IdApplicationPostDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.IdApplicationApiService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class IdApplicationResourceController(private val idApplicationApiService: IdApplicationApiService) {

  @GetMapping("/{prisonerId}/idapplication", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get id application by nomis Id", description = "Id application based on nomis Id")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successful Operation",
      ),
      ApiResponse(
        description = "Not found",
        responseCode = "404",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
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
        description = "Incorrect information provided to perform assessment match",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun getIdApplicationByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("prisonerId")
    @Parameter(required = true)
    prisonerId: String,
  ) = idApplicationApiService.getIdApplicationByNomsId(prisonerId)

  @PostMapping("/{prisonerId}/idapplication", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create id application", description = "Create id application")
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
        description = "Incorrect information provided",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun postBankApplicationByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("prisonerId")
    prisonerId: String,
    @RequestBody
    idApplicationPostDTO: IdApplicationPostDTO,
  ) = idApplicationApiService.createIdApplication(idApplicationPostDTO, prisonerId)

  @DeleteMapping("/{prisonerId}/idapplication/{idApplicationId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create assessment", description = "Create assessment")
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
        description = "Incorrect information provided",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun deleteAssessmentByNomsId(
    @PathVariable("prisonerId")
    @Parameter(required = true)
    prisonerId: String,
    @PathVariable("idApplicationId")
    @Parameter(required = true)
    idApplicationId: String,
  ) {
    val idApplication = idApplicationApiService.getIdApplicationByNomsId(prisonerId)
    if (idApplication.id != idApplicationId.toLong()) {
      throw NoDataWithCodeFoundException(
        "IdApplication",
        idApplicationId,
      )
    }
    idApplicationApiService.deleteIdApplication(idApplication)
  }

  @PatchMapping("/{prisonerId}/idapplication/{idApplicationId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create assessment", description = "Create assessment")
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
        description = "Incorrect information provided",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  suspend fun patchBankApplicationByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("prisonerId")
    prisonerId: String,
    @PathVariable("idApplicationId")
    @Parameter(required = true)
    idApplicationId: String,
    @RequestBody
    idApplicationPatchDTO: IdApplicationPatchDTO,
  ): IdApplicationEntity {
    val idApplication = idApplicationApiService.getIdApplicationByNomsId(prisonerId)
    if (idApplication.id != idApplicationId.toLong()) {
      throw NoDataWithCodeFoundException(
        "IdApplication",
        idApplicationId,
      )
    }
    return idApplicationApiService.updateIdApplication(idApplication, idApplicationPatchDTO)
  }
}
