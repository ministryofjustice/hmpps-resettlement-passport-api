package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import dev.forkhandles.result4k.Result
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.DocumentService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.VirusScanResult
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService
import java.time.LocalDateTime

@RestController
@Validated
@RequestMapping("/resettlement-passport/prisoner/{nomsId}", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class DocumentStorageResourceController(
  private val uploadService: DocumentService,
  private val auditService: AuditService,
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
  fun handleDocumentUploadByNomsId(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestPart
    file: MultipartFile,
    @RequestParam(defaultValue = "LICENCE_CONDITIONS", required = false)
    category: DocumentCategory,
    @RequestPart(required = false)
    originalFilename: String?,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): Result<DocumentsEntity, VirusScanResult.VirusFound> {
    auditService.audit(AuditAction.UPLOAD_DOCUMENT, nomsId, auth, null)
    return uploadService.processDocument(nomsId, file, originalFilename, category)
  }

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
  fun getDocumentByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @PathVariable("documentId")
    @Parameter(required = true)
    documentId: Long,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): ResponseEntity<InputStreamResource> {
    auditService.audit(AuditAction.GET_DOCUMENT, nomsId, auth, null)
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
  fun getLatestDocumentByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @RequestParam(defaultValue = "LICENCE_CONDITIONS", required = false)
    category: DocumentCategory,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): ResponseEntity<InputStreamResource> {
    auditService.audit(AuditAction.GET_DOCUMENT_LATEST, nomsId, auth, null)
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
  fun listDocuments(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @RequestParam(required = false)
    category: DocumentCategory?,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): Collection<DocumentResponse> {
    auditService.audit(AuditAction.GET_DOCUMENTS_LIST, nomsId, auth, null)
    return uploadService.listDocuments(nomsId, category)
      .map { DocumentResponse(it.id!!, it.originalDocumentFileName, it.creationDate, it.category) }
  }

  @DeleteMapping("/documents/latest")
  @Operation(
    summary = "Delete document  for a prisoner",
    description = "Delete both original and converted latest document for prisoner Id .",
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
  fun deleteDocumentsByNomsId(
    @PathVariable("nomsId")
    @Parameter(required = true)
    nomsId: String,
    @RequestParam(defaultValue = "LICENCE_CONDITIONS", required = false)
    category: DocumentCategory,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ) {
    auditService.audit(AuditAction.DELETE_DOCUMENT, nomsId, auth, null)
    return uploadService.deleteUploadDocumentByNomisId(nomsId, category)
  }
  data class DocumentResponse(
    val id: Long,
    val fileName: String,
    val creationDate: LocalDateTime,
    val category: DocumentCategory,
  )
}
