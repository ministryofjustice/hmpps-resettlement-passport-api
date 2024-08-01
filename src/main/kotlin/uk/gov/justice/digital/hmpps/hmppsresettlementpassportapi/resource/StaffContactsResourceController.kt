package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.StaffContactsService

@RestController
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class StaffContactsResourceController(private val staffContactsService: StaffContactsService) {

  @GetMapping("/{nomsId}/staff-contacts")
  @Operation(
    summary = "Staff contacts for prisoner",
    description = "Get details of the POM, COM and Key Worker for a prisoner",
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
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect information in request body. Check schema and try again.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Cannot find prisoner details",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getStaffContacts(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
  ) = staffContactsService.getStaffContacts(nomsId)
}
