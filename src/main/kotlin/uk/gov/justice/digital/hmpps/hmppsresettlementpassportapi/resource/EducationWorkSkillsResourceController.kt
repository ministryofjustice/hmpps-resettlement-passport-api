package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.WorkReadinessStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.EducationWorkSkillsService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class EducationWorkSkillsResourceController(
  private val educationWorkSkillsService: EducationWorkSkillsService,
) {
  @GetMapping("/{nomsId}/work-readiness")
  @Operation(summary = "Get work readiness data", description = "Get work readiness data status and data Education Employment API")
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
    ],
  )
  @WithSpan(kind = SpanKind.SERVER)
  fun getWorkReadinessData(
    @Schema(example = "ABC1234", required = true)
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
  ): WorkReadinessStatus = educationWorkSkillsService.getWorkReadinessData(nomsId)
}
