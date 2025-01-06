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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserOTP
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.KnowledgeBasedVerification
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.OneLoginData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PoPUserOTPService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PrisonerService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService

@RestController
@Validated
@RequestMapping("/resettlement-passport/popUser", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT') or hasAuthority('SCOPE_scope')")
class PopUserOTPResourceController(
  private val popUserOTPService: PoPUserOTPService,
  private val prisonerService: PrisonerService,
  private val auditService: AuditService,
) {

  @GetMapping("/{nomsId}/otp", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get OTP for NomsId", description = "Get Person on Probation User OTP")
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
  fun getPoPUserOTPByNomisId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
  ): PoPUserOTP? {
    val prisonerEntity = prisonerService.getPrisonerEntity(nomsId)
    return popUserOTPService.getOTPByPrisoner(prisonerEntity)
  }

  @PostMapping("/{nomsId}/otp", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Generate OTP for PoP User", description = "Generate OTP for Person On Probation User")
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
  fun createOTPByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): PoPUserOTP {
    auditService.audit(AuditAction.CREATE_PYF_USER_OTP, nomsId, auth, null)
    val prisonerEntity = prisonerService.getPrisonerEntity(nomsId)
    return popUserOTPService.createPoPUserOTP(prisonerEntity)
  }

  @DeleteMapping("/{nomsId}/otp", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Delete PoP User OTP", description = "Delete Person On Probation User OTP")
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
  fun deletePoPUserOTPByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ) {
    val prisonerEntity = prisonerService.getPrisonerEntity(nomsId)
    val popUserOTPEntity = popUserOTPService.getPoPUserOTPByPrisoner(prisonerEntity)
    auditService.audit(AuditAction.DELETE_PYF_USER_OTP, nomsId, auth, null)
    popUserOTPService.deletePoPUserOTP(popUserOTPEntity)
  }

  @PostMapping("/onelogin/verify", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Verify OTP for PoP User", description = "Verify OTP for Person On Probation User")
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
  fun verifyOTPByOneLoginURN(
    @RequestBody
    oneLoginUserData: OneLoginData,
  ): PoPUserResponse {
    return popUserOTPService.getPoPUserVerified(oneLoginUserData)
  }

  @PostMapping("/onelogin/verify-answers", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Verify knowledge answers for PoP User")
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
  fun verifyByKnowledgeAnswers(
    @RequestBody
    formData: KnowledgeBasedVerification,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): PoPUserResponse {
    auditService.audit(AuditAction.VERIFY_PYF_USER_BY_KNOWLEDGE_ANSWER, formData.nomsId, auth, null)
    return popUserOTPService.verifyFromKnowledgeQuestions(formData)
  }
}
