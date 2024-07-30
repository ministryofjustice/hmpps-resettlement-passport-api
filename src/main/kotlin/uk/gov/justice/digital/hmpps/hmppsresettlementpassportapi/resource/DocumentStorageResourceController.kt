package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.DocumentService

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner/{nomsId}", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class DocumentStorageResourceController(
  private val uploadService: DocumentService,
) {
  @PostMapping(
    "/documents/upload",
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
  @WithSpan(kind = SpanKind.SERVER)
  fun handleDocumentUploadByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestParam("file")
    file: MultipartFile,
    @RequestParam(defaultValue = "LICENCE_CONDITIONS", required = false)
    category: DocumentCategory,
  ) = uploadService.processDocument(nomsId, file, category)

  @GetMapping("/documents/{documentId}/download", produces = [MediaType.APPLICATION_PDF_VALUE])
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
  @WithSpan(kind = SpanKind.SERVER)
  fun getDocumentByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("documentId")
    @Parameter(required = true)
    documentId: Long,
  ): ResponseEntity<InputStreamResource> {
    val stream = uploadService.getDocumentByNomisIdAndDocumentId(nomsId, documentId)
    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_PDF)
      .body(InputStreamResource(stream))
  }

  @GetMapping("/documents/latest/download")
  @Operation(
    summary = "Get document  for a prisoner",
    description = "Get Latest Original Document for prisoner Id .",
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
        description = "No data found for prisoner id",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @WithSpan(kind = SpanKind.SERVER)
  fun getLatestDocumentByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @RequestParam(defaultValue = "LICENCE_CONDITIONS", required = false)
    category: DocumentCategory,
  ): ResponseEntity<InputStreamResource> {
    val stream = uploadService.getLatestDocumentByCategory(nomsId, category)
    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_PDF)
      .body(InputStreamResource(stream))
  }

  @GetMapping("/documents")
  @Operation(
    summary = "Get document metadata for a prisoner",
    description = "List of available documents for a prisoner",
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
    ],
  )
  @WithSpan(kind = SpanKind.SERVER)
  fun listDocuments(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @RequestParam(required = true)
    category: DocumentCategory,
  ): Collection<DocumentResponse> = uploadService.listDocuments(nomsId, category)
    .map { DocumentResponse(it.id!!, it.originalDocumentFileName) }

  data class DocumentResponse(
    val id: Long,
    val fileName: String,
  )
}
