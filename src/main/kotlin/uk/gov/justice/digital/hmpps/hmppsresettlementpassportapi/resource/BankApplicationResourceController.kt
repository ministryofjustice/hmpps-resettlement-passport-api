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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.bankapplicatonapi.BankApplicationDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.bankapplicatonapi.BankApplicationResponseDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.BankApplicationApiService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class BankApplicationResourceController(private val bankApplicationApiService: BankApplicationApiService) {
  @GetMapping("/{prisonerId}/bankapplication", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get bank application by nomis Id", description = "Bank application based on nomis Id")
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
  suspend fun getBankApplicationByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("prisonerId")
    @Parameter(required = true)
    prisonerId: String,
  ) = bankApplicationApiService.getBankApplicationByNomsId(prisonerId)

  @PostMapping("/{prisonerId}/bankapplication", produces = [MediaType.APPLICATION_JSON_VALUE])
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
  suspend fun postBankApplicationByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("prisonerId")
    prisonerId: String,
    @RequestBody
    bankApplicationDTO: BankApplicationDTO,
  ) = bankApplicationApiService.createBankApplication(bankApplicationDTO, prisonerId)

  @DeleteMapping("/{prisonerId}/bankapplication/{bankApplicationId}", produces = [MediaType.APPLICATION_JSON_VALUE])
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
    @PathVariable("bankApplicationId")
    @Parameter(required = true)
    bankApplicationId: String,
  ) {
    val bankApplication = bankApplicationApiService.getBankApplicationById(bankApplicationId.toLong()).get()
    if (bankApplication.prisoner.nomsId != prisonerId) {
      throw NoDataWithCodeFoundException(
        "BankApplication",
        bankApplicationId,
      )
    }
    bankApplicationApiService.deleteBankApplication(bankApplication)
  }

  @PatchMapping("/{prisonerId}/bankapplication/{bankApplicationId}", produces = [MediaType.APPLICATION_JSON_VALUE])
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
    @PathVariable("bankApplicationId")
    @Parameter(required = true)
    bankApplicationId: String,
    @RequestBody
    bankApplicationDTO: BankApplicationDTO,
  ): BankApplicationResponseDTO {
    val bankApplication = bankApplicationApiService.getBankApplicationById(bankApplicationId.toLong()).get()
    if (bankApplication.prisoner.nomsId != prisonerId) {
      throw NoDataWithCodeFoundException(
        "BankApplication",
        bankApplicationId,
      )
    }
    bankApplicationApiService.updateBankApplication(existingBankApplication = bankApplication, bankApplicationDTO)
    return bankApplicationApiService.getBankApplicationByNomsId(prisonerId)
      ?: throw ResourceNotFoundException("Bank application for prisoner: $prisonerId not found after update")
  }
}
