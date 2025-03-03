package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.TodoEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.TodoService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService
import java.time.LocalDate
import java.util.UUID

@RestController
@Validated
@RequestMapping("/resettlement-passport/person", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class TodoResourceController(private val todoService: TodoService, private val auditService: AuditService) {
  @PostMapping("/{nomsId}/todo", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create todo entry")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Entry added to person's todo list",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @ResponseStatus(HttpStatus.CREATED)
  fun createTodo(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestBody
    createRequest: TodoRequest,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): TodoEntity {
    auditService.audit(AuditAction.CREATE_TODO, nomsId, auth, null)
    return todoService.createEntry(nomsId, createRequest)
  }

  @GetMapping("/{nomsId}/todo", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get todo list for a person")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "List of items in person's todo lsit",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getTodoList(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestParam(required = false)
    sortField: String? = null,
    @RequestParam(required = false)
    sortDirection: Sort.Direction? = null,
  ) = todoService.getList(nomsId, sortField, sortDirection)

  @DeleteMapping("/{nomsId}/todo/{id}")
  @Operation(summary = "Delete a todo list item for a person")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Item is deleted",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person or item not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteItem(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("id") id: UUID,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ) {
    auditService.audit(AuditAction.DELETE_TODO, nomsId, auth, null)
    return todoService.deleteItem(nomsId, id)
  }

  @PutMapping("/{nomsId}/todo/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Update a todo list item for a person")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Item is updated",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person or item not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun updateItem(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("id") id: UUID,
    @RequestBody
    request: TodoRequest,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): TodoEntity {
    auditService.audit(AuditAction.UPDATE_TODO, nomsId, auth, null)
    return todoService.updateItem(nomsId, id, request)
  }

  @PatchMapping("/{nomsId}/todo/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Patch a todo list item for a person")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Item is updated",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person or item not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun patchItem(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("id") id: UUID,
    @RequestBody
    request: TodoPatchRequest,
    @Schema(hidden = true)
    @RequestHeader("Authorization")
    auth: String,
  ): TodoEntity {
    auditService.audit(AuditAction.COMPLETE_TODO, nomsId, auth, null)
    return todoService.patchItem(nomsId, id, request)
  }

  @GetMapping("/{nomsId}/todo/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get a single todo list item for a person")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Item is updated",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person or item not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getItem(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("id") id: UUID,
  ): TodoEntity = todoService.getOne(nomsId, id)
}

data class TodoRequest(
  val urn: String,
  val title: String,
  val notes: String? = null,
  val dueDate: LocalDate? = null,
)

data class TodoPatchRequest(
  val urn: String,
  val completed: Boolean,
)
