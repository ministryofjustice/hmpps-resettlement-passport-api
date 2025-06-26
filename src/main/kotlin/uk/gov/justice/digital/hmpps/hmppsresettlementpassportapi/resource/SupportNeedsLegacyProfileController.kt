package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.SupportNeedsLegacyProfileService

@RestController
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class SupportNeedsLegacyProfileController(private val supportNeedsLegacyProfileService: SupportNeedsLegacyProfileService) {

    @PostMapping("/add-legacy-support-needs", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Add pathway-level legacy support needs to all prisoners with supportNeedsLegacyProfile flag set (if needed)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
            ),
        ],
    )
    fun addLegacySupportNeeds(): ResponseEntity<Void> {
        supportNeedsLegacyProfileService.addLegacySupportNeeds()
        return ResponseEntity.ok().build()
    }
}
