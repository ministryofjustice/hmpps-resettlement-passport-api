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
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.aop.READ_ONLY_MODE_DISABLED
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.aop.RequiresFeature
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplication
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplicationResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.BankApplicationService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class BankApplicationResourceController(private val bankApplicationService: BankApplicationService, private val auditService: AuditService) {
  @GetMapping("/{nomsId}/bankapplication", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get bank application by noms Id", description = "Bank application based on noms Id")
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
  fun getBankApplicationByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
  ) = bankApplicationService.getBankApplicationByNomsId(nomsId)

  @PostMapping("/{nomsId}/bankapplication", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create assessment", description = "Create assessment", deprecated = true)
  @RequiresFeature(READ_ONLY_MODE_DISABLED)
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
  fun postBankApplicationByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestBody
    bankApplication: BankApplication,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): BankApplicationResponse {
    auditService.audit(AuditAction.CREATE_BANK_APPLICATION, nomsId, auth, null)
    return bankApplicationService.createBankApplication(bankApplication, nomsId)
  }

  @DeleteMapping("/{nomsId}/bankapplication/{bankApplicationId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create assessment", description = "Create assessment", deprecated = true)
  @RequiresFeature(READ_ONLY_MODE_DISABLED)
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
  fun deleteAssessmentByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("bankApplicationId")
    @Parameter(required = true)
    bankApplicationId: Long,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ) {
    val bankApplication = bankApplicationService.getBankApplicationByIdAndNomsId(bankApplicationId, nomsId)
    auditService.audit(AuditAction.DELETE_BANK_APPLICATION, nomsId, auth, null)
    bankApplicationService.deleteBankApplication(bankApplication)
  }

  @PatchMapping("/{nomsId}/bankapplication/{bankApplicationId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create assessment", description = "Create assessment", deprecated = true)
  @RequiresFeature(READ_ONLY_MODE_DISABLED)
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
  fun patchBankApplicationByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("bankApplicationId")
    @Parameter(required = true)
    bankApplicationId: String,
    @RequestBody
    bankApplication: BankApplication,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): BankApplicationResponse {
    auditService.audit(AuditAction.UPDATE_BANK_APPLICATION, nomsId, auth, null)
    return bankApplicationService.patchBankApplication(nomsId, bankApplicationId, bankApplication)
  }
}
