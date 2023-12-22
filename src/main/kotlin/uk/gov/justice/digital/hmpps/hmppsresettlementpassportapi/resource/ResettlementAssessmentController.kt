package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class ResettlementAssessmentController {

}