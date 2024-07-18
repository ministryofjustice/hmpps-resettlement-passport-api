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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.DocumentService

@RestController
@Validated
@RequestMapping("/resettlement-passport/documents", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class DocumentStorageResourceController(
  private val uploadService: DocumentService,
) {
  @PostMapping(
    "/{nomsId}/upload",
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
  )
  @Operation(summary = "Upload Document", description = "Upload Documents")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Document successfully added",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun handleDocumentUploadByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestParam("file")
    file: MultipartFile,
    @RequestParam(defaultValue = "LICENCE_CONDITIONS", required = false)
    category: String,
  ) = uploadService.processDocument(nomsId, file, category)

  @GetMapping("{nomsId}/download/{documentId}")
  @Operation(
    summary = "Get document  for a prisoner",
    description = "Get Document for a given document Id and prisoner Id .",
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
        responseCode = "404",
        description = "No data found for given document id and prisoner id",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getDocumentByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("documentId")
    @Parameter(required = true)
    documentId: Long,
  ): ByteArray = uploadService.getDocumentByNomisIdAndDocumentId(nomsId, documentId)

  @GetMapping("{nomsId}/html/{documentId}", produces = [MediaType.TEXT_HTML_VALUE])
  @Operation(
    summary = "Get document  for a prisoner",
    description = "Get Document for a given document Id and prisoner Id .",
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
        responseCode = "404",
        description = "No data found for given document id and prisoner id",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getDocumentHtmlByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("documentId")
    @Parameter(required = true)
    documentId: Long,
  ): String = uploadService.getHtmlByNomisIdAndDocumentId(nomsId, documentId)
}
