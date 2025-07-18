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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.IdApplicationPatch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.IdApplicationPost
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.IdApplicationService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class IdApplicationResourceController(private val idApplicationService: IdApplicationService, private val auditService: AuditService) {

  @PostMapping("/{nomsId}/idapplication", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create id application", description = "Create id application", deprecated = true)
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
  fun postIdApplicationByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestBody
    idApplicationPost: IdApplicationPost,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): IdApplicationEntity {
    auditService.audit(AuditAction.CREATE_ID_APPLICATION, nomsId, auth, null)
    return idApplicationService.createIdApplication(idApplicationPost, nomsId)
  }

  @DeleteMapping("/{nomsId}/idapplication/{idApplicationId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create Id Application", description = "Create Id Application for a prisoner", deprecated = true)
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
  fun deleteIdApplicationByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("idApplicationId")
    @Parameter(required = true)
    idApplicationId: String,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ) {
    auditService.audit(AuditAction.DELETE_ID_APPLICATION, nomsId, auth, null)
    val idApplication = idApplicationService.getIdApplicationByNomsIdAndIdApplicationID(nomsId, idApplicationId.toLong())
    if (idApplication != null) {
      if (idApplication.id != idApplicationId.toLong()) {
        throw NoDataWithCodeFoundException(
          "IdApplication",
          idApplicationId,
        )
      }
    }
    if (idApplication != null) {
      idApplicationService.deleteIdApplication(idApplication)
    }
  }

  @PatchMapping("/{nomsId}/idapplication/{idApplicationId}", produces = [MediaType.APPLICATION_JSON_VALUE])
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
  fun patchIdApplicationByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("idApplicationId")
    @Parameter(required = true)
    idApplicationId: String,
    @RequestBody
    idApplicationPatchDTO: IdApplicationPatch,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): IdApplicationEntity? {
    auditService.audit(AuditAction.UPDATE_ID_APPLICATION, nomsId, auth, null)
    val idApplication = idApplicationService.getIdApplicationByNomsIdAndIdApplicationID(nomsId, idApplicationId.toLong())
      ?: throw NoDataWithCodeFoundException(
        "IdApplication",
        idApplicationId,
      )
    return idApplicationService.updateIdApplication(idApplication, idApplicationPatchDTO)
  }

  @GetMapping("/{nomsId}/idapplication/all", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get all id applications by noms Id", description = "All Id application based on noms Id")
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
  fun getAllIdApplicationsByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
  ) = idApplicationService.getAllIdApplicationsByNomsId(nomsId)
}
